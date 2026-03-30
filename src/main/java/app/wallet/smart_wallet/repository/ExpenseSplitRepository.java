package app.wallet.smart_wallet.repository;

import com.smartwallet.entity.ExpenseSplit;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {

    List<ExpenseSplit> findByExpenseId(Long expenseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select es from ExpenseSplit es where es.expense.id = :expenseId")
    List<ExpenseSplit> findByExpenseIdForUpdate(@Param("expenseId") Long expenseId);
}
