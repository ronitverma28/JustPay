package app.wallet.smart_wallet.service;

import app.wallet.smart_wallet.dto.response.TransactionResponse;
import app.wallet.smart_wallet.entity.Pool;
import app.wallet.smart_wallet.entity.Transaction;
import app.wallet.smart_wallet.entity.User;
import app.wallet.smart_wallet.entity.enums.TransactionStatus;
import app.wallet.smart_wallet.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    Transaction createTransaction(
            User user,
            Pool pool,
            BigDecimal amount,
            TransactionType type,
            TransactionStatus status,
            String referenceId
    );

    List<TransactionResponse> getMyTransactions();
}