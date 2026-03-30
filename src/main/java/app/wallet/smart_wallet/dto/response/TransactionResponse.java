package app.wallet.smart_wallet.dto.response;

import app.wallet.smart_wallet.entity.enums.TransactionStatus;
import app.wallet.smart_wallet.entity.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String referenceId;
    private Long poolId;
    private String poolName;
    private LocalDateTime createdAt;
}
