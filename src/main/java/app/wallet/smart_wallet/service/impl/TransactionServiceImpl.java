package app.wallet.smart_wallet.service.impl;

import app.wallet.smart_wallet.dto.response.TransactionResponse;
import app.wallet.smart_wallet.entity.Pool;
import app.wallet.smart_wallet.entity.Transaction;
import app.wallet.smart_wallet.entity.User;
import app.wallet.smart_wallet.entity.enums.TransactionStatus;
import app.wallet.smart_wallet.entity.enums.TransactionType;
import app.wallet.smart_wallet.repository.TransactionRepository;
import app.wallet.smart_wallet.service.TransactionService;
import app.wallet.smart_wallet.service.UserService;
import app.wallet.smart_wallet.util.EntityMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Transactional
    public Transaction createTransaction(
        User user,
        Pool pool,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        String referenceId
    ) {
        Transaction transaction = Transaction.builder()
            .user(user)
            .pool(pool)
            .amount(amount)
            .type(type)
            .status(status)
            .referenceId(referenceId)
            .build();
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
            .stream()
            .map(EntityMapper::toTransactionResponse)
            .collect(Collectors.toList());
    }
}
