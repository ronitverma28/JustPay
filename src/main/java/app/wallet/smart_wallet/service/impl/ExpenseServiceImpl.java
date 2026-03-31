package app.wallet.smart_wallet.service.impl;

import app.wallet.smart_wallet.dto.request.CreateExpenseRequest;
import app.wallet.smart_wallet.dto.request.CustomSplitRequest;
import app.wallet.smart_wallet.dto.response.ExpenseResponse;
import app.wallet.smart_wallet.entity.Expense;
import app.wallet.smart_wallet.entity.ExpenseSplit;
import app.wallet.smart_wallet.entity.Pool;
import app.wallet.smart_wallet.entity.User;
import app.wallet.smart_wallet.entity.enums.Role;
import app.wallet.smart_wallet.entity.enums.SplitType;
import app.wallet.smart_wallet.exception.BadRequestException;
import app.wallet.smart_wallet.exception.ForbiddenOperationException;
import app.wallet.smart_wallet.exception.ResourceNotFoundException;
import app.wallet.smart_wallet.repository.ExpenseRepository;
import app.wallet.smart_wallet.repository.PoolRepository;
import app.wallet.smart_wallet.service.ExpenseService;
import app.wallet.smart_wallet.service.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import app.wallet.smart_wallet.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final PoolRepository poolRepository;
    private final UserService userService;

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Pool pool = poolRepository.findByIdForUpdate(request.getPoolId())
            .orElseThrow(() -> new ResourceNotFoundException("Pool not found with id: " + request.getPoolId()));

        validatePoolAccess(currentUser, pool);

        Expense expense = Expense.builder()
            .pool(pool)
            .createdBy(currentUser)
            .amount(request.getAmount().setScale(2, RoundingMode.HALF_UP))
            .description(request.getDescription().trim())
            .splitType(request.getSplitType())
            .splits(new ArrayList<>())
            .build();

        List<ExpenseSplit> splits = request.getSplitType() == SplitType.EQUAL
            ? createEqualSplits(expense, pool, expense.getAmount())
            : createCustomSplits(expense, pool, request.getCustomSplits(), expense.getAmount());

        expense.setSplits(splits);
        Expense savedExpense = expenseRepository.save(expense);

        return EntityMapper.toExpenseResponse(savedExpense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByPool(Long poolId) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Pool pool = poolRepository.findById(poolId)
            .orElseThrow(() -> new ResourceNotFoundException("Pool not found with id: " + poolId));

        validatePoolAccess(currentUser, pool);

        return expenseRepository.findByPoolIdOrderByCreatedAtDesc(poolId)
            .stream()
            .map(EntityMapper::toExpenseResponse)
            .collect(Collectors.toList());
    }

    private List<ExpenseSplit> createEqualSplits(Expense expense, Pool pool, BigDecimal totalAmount) {
        List<User> members = pool.getMembers()
            .stream()
            .sorted(Comparator.comparing(User::getId))
            .collect(Collectors.toList());

        if (members.isEmpty()) {
            throw new BadRequestException("Pool has no members to split the expense");
        }

        BigDecimal baseShare = totalAmount.divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.DOWN);
        BigDecimal distributed = baseShare.multiply(BigDecimal.valueOf(members.size()));
        BigDecimal remainder = totalAmount.subtract(distributed);

        List<ExpenseSplit> splits = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            BigDecimal shareAmount = baseShare;
            if (i == 0) {
                shareAmount = shareAmount.add(remainder);
            }
            splits.add(ExpenseSplit.builder()
                .expense(expense)
                .user(members.get(i))
                .shareAmount(shareAmount)
                .build());
        }

        return splits;
    }

    private List<ExpenseSplit> createCustomSplits(
        Expense expense,
        Pool pool,
        List<CustomSplitRequest> customSplits,
        BigDecimal totalAmount
    ) {
        if (customSplits == null || customSplits.isEmpty()) {
            throw new BadRequestException("customSplits is required when splitType is CUSTOM");
        }

        Set<Long> poolMemberIds = pool.getMembers().stream().map(User::getId).collect(Collectors.toSet());
        Map<Long, BigDecimal> memberShareMap = new LinkedHashMap<>();

        for (CustomSplitRequest splitRequest : customSplits) {
            if (memberShareMap.containsKey(splitRequest.getUserId())) {
                throw new BadRequestException("Duplicate user in customSplits: " + splitRequest.getUserId());
            }
            if (!poolMemberIds.contains(splitRequest.getUserId())) {
                throw new BadRequestException("User " + splitRequest.getUserId() + " is not a pool member");
            }
            memberShareMap.put(
                splitRequest.getUserId(),
                splitRequest.getShareAmount().setScale(2, RoundingMode.HALF_UP)
            );
        }

        BigDecimal customTotal = memberShareMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (customTotal.compareTo(totalAmount) != 0) {
            throw new BadRequestException("Sum of custom splits must exactly match expense amount");
        }

        List<ExpenseSplit> splits = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : memberShareMap.entrySet()) {
            User user = userService.getById(entry.getKey());
            splits.add(ExpenseSplit.builder()
                .expense(expense)
                .user(user)
                .shareAmount(entry.getValue())
                .build());
        }

        return splits;
    }

    private void validatePoolAccess(User user, Pool pool) {
        boolean isMember = pool.getMembers().stream().anyMatch(member -> member.getId().equals(user.getId()));
        if (user.getRole() != Role.ADMIN && !isMember) {
            throw new ForbiddenOperationException("Only pool members can access this operation");
        }
    }
}
