package app.wallet.smart_wallet.repository;

import app.wallet.smart_wallet.entity.Pool;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PoolRepository extends JpaRepository<Pool, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pool p where p.id = :poolId")
    Optional<Pool> findByIdForUpdate(@Param("poolId") Long poolId);

    @Query("select p from Pool p join p.members m where p.id = :poolId and m.id = :userId")
    Optional<Pool> findPoolByIdAndMember(@Param("poolId") Long poolId, @Param("userId") Long userId);

    boolean existsByIdAndMembers_Id(Long poolId, Long userId);
}
