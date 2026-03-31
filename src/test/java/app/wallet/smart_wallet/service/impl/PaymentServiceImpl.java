package app.wallet.smart_wallet.service.impl;

import app.wallet.smart_wallet.config.RazorpayProperties;
import app.wallet.smart_wallet.dto.request.ExpensePaymentRequest;
import app.wallet.smart_wallet.dto.request.ManualPaymentRequest;
import app.wallet.smart_wallet.dto.request.RazorpayOrderRequest;
import app.wallet.smart_wallet.dto.request.RazorpayVerifyRequest;
import app.wallet.smart_wallet.dto.response.ExpenseResponse;
import app.wallet.smart_wallet.dto.response.ExpenseSplitResponse;
import app.wallet.smart_wallet.dto.response.RazorpayOrderResponse;
import app.wallet.smart_wallet.dto.response.TransactionResponse;
import app.wallet.smart_wallet.entity.*;
import app.wallet.smart_wallet.entity.enums.*;
import app.wallet.smart_wallet.exception.*;
import app.wallet.smart_wallet.repository.*;
import app.wallet.smart_wallet.util.EntityMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl {

    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final PoolRepository poolRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final UserServiceImpl userServiceImpl;
    private final TransactionServiceImpl transactionServiceImpl;
    private final ObjectMapper objectMapper;

    @Transactional
    public RazorpayOrderResponse createRazorpayOrder(RazorpayOrderRequest request) {
        User currentUser = userServiceImpl.getCurrentAuthenticatedUser();

        Pool pool = null;
        if (request.getPoolId() != null) {
            pool = poolRepository.findById(request.getPoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Pool not found with id: " + request.getPoolId()));
            validatePoolAccess(currentUser, pool);
        }

        long amountInPaise;
        try {
            amountInPaise = request.getAmount()
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();
        } catch (ArithmeticException ex) {
            throw new BadRequestException("Invalid amount format");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", razorpayProperties.getCurrency());
            options.put("receipt", "receipt_" + UUID.randomUUID().toString().replace("-", ""));

            Order order = razorpayClient.orders.create(options);
            String orderId = order.get("id").toString();

            Transaction transaction = Transaction.builder()
                .user(currentUser)
                .pool(pool)
                .amount(request.getAmount().setScale(2, RoundingMode.HALF_UP))
                .type(TransactionType.ADD_MONEY)
                .status(TransactionStatus.INITIATED)
                .referenceId(orderId)
                .build();
            transactionRepository.save(transaction);

            return RazorpayOrderResponse.builder()
                .orderId(orderId)
                .keyId(razorpayProperties.getKeyId())
                .amount(transaction.getAmount())
                .currency(razorpayProperties.getCurrency())
                .status(order.has("status") ? String.valueOf(order.get("status")) : "created")
                .build();
        } catch (RazorpayException ex) {
            throw new BadRequestException("Failed to create Razorpay order: " + ex.getMessage());
        }
    }

    @Transactional
    public TransactionResponse verifyRazorpayPayment(RazorpayVerifyRequest request) {
        User currentUser = userServiceImpl.getCurrentAuthenticatedUser();

        Transaction transaction = transactionRepository.findForUpdateByReferenceIdAndType(
                request.getRazorpayOrderId(),
                TransactionType.ADD_MONEY
            )
            .orElseThrow(() -> new ResourceNotFoundException("Order not found in transaction log"));

        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            throw new DuplicateTransactionException("Payment has already been processed for this order");
        }

        if (transaction.getStatus() == TransactionStatus.FAILED) {
            throw new BadRequestException("This payment was already marked as failed");
        }

        if (!transaction.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("You cannot verify payment for another user");
        }

        boolean isValid = verifyPaymentSignature(
            request.getRazorpayOrderId(),
            request.getRazorpayPaymentId(),
            request.getRazorpaySignature()
        );

        if (!isValid) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new PaymentVerificationException("Razorpay signature verification failed");
        }

        creditWalletAndPool(transaction);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        return EntityMapper.toTransactionResponse(transaction);
    }

    @Transactional
    public String handleRazorpayWebhook(String payload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new BadRequestException("Missing X-Razorpay-Signature header");
        }

        boolean isWebhookValid;
        try {
            isWebhookValid = Utils.verifyWebhookSignature(payload, signatureHeader, razorpayProperties.getWebhookSecret());
        } catch (RazorpayException ex) {
            throw new PaymentVerificationException("Webhook verification failed");
        }

        if (!isWebhookValid) {
            throw new PaymentVerificationException("Invalid webhook signature");
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText();

            if ("payment.captured".equals(event)) {
                String orderId = root.path("payload").path("payment").path("entity").path("order_id").asText(null);
                if (orderId != null && !orderId.isBlank()) {
                    transactionRepository.findForUpdateByReferenceIdAndType(orderId, TransactionType.ADD_MONEY)
                        .ifPresent(transaction -> {
                            if (transaction.getStatus() == TransactionStatus.SUCCESS) {
                                return;
                            }
                            creditWalletAndPool(transaction);
                            transaction.setStatus(TransactionStatus.SUCCESS);
                            transactionRepository.save(transaction);
                        });
                }
            }

            if ("payment.failed".equals(event)) {
                String orderId = root.path("payload").path("payment").path("entity").path("order_id").asText(null);
                if (orderId != null && !orderId.isBlank()) {
                    transactionRepository.findForUpdateByReferenceIdAndType(orderId, TransactionType.ADD_MONEY)
                        .ifPresent(transaction -> {
                            if (transaction.getStatus() == TransactionStatus.INITIATED) {
                                transaction.setStatus(TransactionStatus.FAILED);
                                transactionRepository.save(transaction);
                            }
                        });
                }
            }

            return "Webhook processed";
        } catch (Exception ex) {
            throw new BadRequestException("Invalid webhook payload");
        }
    }

    @Transactional
    public ExpenseResponse payExpenseFromPool(ExpensePaymentRequest request) {
        User currentUser = userServiceImpl.getCurrentAuthenticatedUser();

        Expense expense = expenseRepository.findByIdForUpdate(request.getExpenseId())
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + request.getExpenseId()));

        if (expense.getStatus() == ExpenseStatus.PAID) {
            throw new DuplicateTransactionException("Expense is already paid");
        }

        Pool pool = poolRepository.findByIdForUpdate(expense.getPool().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Pool not found"));
        validatePoolAccess(currentUser, pool);

        List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseIdForUpdate(expense.getId());
        if (splits.isEmpty()) {
            throw new BadRequestException("No expense splits found");
        }

        if (pool.getPoolBalance().compareTo(expense.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient pool balance to pay this expense");
        }

        Map<Long, Wallet> lockedWallets = new HashMap<>();
        for (ExpenseSplit split : splits) {
            if (split.getStatus() == SplitStatus.PAID) {
                throw new BadRequestException("Expense has inconsistent split state and cannot be paid again");
            }

            Wallet wallet = walletRepository.findByUserIdForUpdate(split.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + split.getUser().getId()));

            if (wallet.getBalance().compareTo(split.getShareAmount()) < 0) {
                throw new InsufficientBalanceException(
                    "Insufficient wallet balance for user " + split.getUser().getId()
                );
            }
            lockedWallets.put(split.getUser().getId(), wallet);
        }

        pool.setPoolBalance(pool.getPoolBalance().subtract(expense.getAmount()));
        poolRepository.save(pool);

        for (ExpenseSplit split : splits) {
            Wallet wallet = lockedWallets.get(split.getUser().getId());
            wallet.setBalance(wallet.getBalance().subtract(split.getShareAmount()));
            walletRepository.save(wallet);

            split.setStatus(SplitStatus.PAID);
            transactionServiceImpl.createTransaction(
                split.getUser(),
                pool,
                split.getShareAmount(),
                TransactionType.EXPENSE,
                TransactionStatus.SUCCESS,
                "EXPENSE_" + expense.getId() + "_" + split.getUser().getId() + "_" + System.nanoTime()
            );
        }

        expenseSplitRepository.saveAll(splits);
        expense.setStatus(ExpenseStatus.PAID);
        expenseRepository.save(expense);

        transactionServiceImpl.createTransaction(
            currentUser,
            pool,
            expense.getAmount(),
            TransactionType.PAYMENT,
            TransactionStatus.SUCCESS,
            "PAY_EXPENSE_" + expense.getId() + "_" + UUID.randomUUID()
        );

        expense.setSplits(splits);
        return mapExpenseResponse(expense);
    }

    @Transactional
    public ExpenseResponse markExpensePaidManually(ManualPaymentRequest request) {
        User currentUser = userServiceImpl.getCurrentAuthenticatedUser();

        Expense expense = expenseRepository.findByIdForUpdate(request.getExpenseId())
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + request.getExpenseId()));

        if (expense.getStatus() == ExpenseStatus.PAID) {
            throw new DuplicateTransactionException("Expense is already paid");
        }

        boolean canMarkManually = currentUser.getRole() == Role.ADMIN
            || expense.getCreatedBy().getId().equals(currentUser.getId());
        if (!canMarkManually) {
            throw new ForbiddenOperationException("Only ADMIN or expense creator can mark expense manually");
        }

        List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseIdForUpdate(expense.getId());
        for (ExpenseSplit split : splits) {
            split.setStatus(SplitStatus.PAID);
        }

        expenseSplitRepository.saveAll(splits);
        expense.setStatus(ExpenseStatus.PAID);
        expenseRepository.save(expense);

        transactionServiceImpl.createTransaction(
            currentUser,
            expense.getPool(),
            expense.getAmount(),
            TransactionType.PAYMENT,
            TransactionStatus.SUCCESS,
            "MANUAL_EXPENSE_" + expense.getId() + "_" + UUID.randomUUID()
        );

        expense.setSplits(splits);
        return mapExpenseResponse(expense);
    }

    private void validatePoolAccess(User user, Pool pool) {
        boolean isMember = pool.getMembers().stream().anyMatch(member -> member.getId().equals(user.getId()));
        if (user.getRole() != Role.ADMIN && !isMember) {
            throw new ForbiddenOperationException("Only pool members can access this operation");
        }
    }

    private boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        String payload = orderId + "|" + paymentId;
        try {
            return Utils.verifySignature(payload, signature, razorpayProperties.getKeySecret());
        } catch (RazorpayException ex) {
            return false;
        }
    }

    private void creditWalletAndPool(Transaction transaction) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(transaction.getUser().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for transaction user"));

        wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
        walletRepository.save(wallet);

        if (transaction.getPool() != null) {
            Pool pool = poolRepository.findByIdForUpdate(transaction.getPool().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pool not found for transaction"));
            pool.setPoolBalance(pool.getPoolBalance().add(transaction.getAmount()));
            poolRepository.save(pool);
        }
    }

    private ExpenseResponse mapExpenseResponse(Expense expense) {
        List<ExpenseSplitResponse> splitResponses = expense.getSplits().stream()
            .sorted(Comparator.comparing(ExpenseSplit::getId))
            .map(split -> ExpenseSplitResponse.builder()
                .userId(split.getUser().getId())
                .userName(split.getUser().getName())
                .shareAmount(split.getShareAmount())
                .status(split.getStatus())
                .build())
            .collect(Collectors.toList());

        return ExpenseResponse.builder()
            .id(expense.getId())
            .poolId(expense.getPool().getId())
            .poolName(expense.getPool().getName())
            .createdBy(expense.getCreatedBy().getId())
            .createdByName(expense.getCreatedBy().getName())
            .amount(expense.getAmount())
            .description(expense.getDescription())
            .splitType(expense.getSplitType())
            .status(expense.getStatus())
            .createdAt(expense.getCreatedAt())
            .splits(splitResponses)
            .build();
    }
}
