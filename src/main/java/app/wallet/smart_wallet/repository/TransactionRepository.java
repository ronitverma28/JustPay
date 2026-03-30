package app.wallet.smart_wallet.repository;

import app.wallet.smart_wallet.entity.Transaction;
import app.wallet.smart_wallet.entity.enums.TransactionStatus;
import app.wallet.smart_wallet.entity.enums.TransactionType;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Transaction> findByReferenceIdAndType(String referenceId, TransactionType type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transaction t where t.referenceId = :referenceId and t.type = :type")
    Optional<Transaction> findForUpdateByReferenceIdAndType(
        @Param("referenceId") String referenceId,
        @Param("type") TransactionType type
    );

    boolean existsByReferenceIdAndTypeAndStatus(String referenceId, TransactionType type, TransactionStatus status);
}
