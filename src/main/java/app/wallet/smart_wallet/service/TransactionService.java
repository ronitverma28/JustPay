package app.wallet.smart_wallet.service;

import com.smartwallet.dto.TransactionResponse;
import com.smartwallet.entity.Pool;
import com.smartwallet.entity.Transaction;
import com.smartwallet.entity.User;
import com.smartwallet.enums.TransactionStatus;
import com.smartwallet.enums.TransactionType;
import com.smartwallet.repository.TransactionRepository;
import com.smartwallet.util.EntityMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

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
