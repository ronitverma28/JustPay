package app.wallet.smart_wallet.repository;

import app.wallet.smart_wallet.entity.Expense;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByPoolIdOrderByCreatedAtDesc(Long poolId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Expense e where e.id = :expenseId")
    Optional<Expense> findByIdForUpdate(@Param("expenseId") Long expenseId);
}
