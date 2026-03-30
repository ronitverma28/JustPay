package app.wallet.smart_wallet.dto;

import com.smartwallet.enums.ExpenseStatus;
import com.smartwallet.enums.SplitType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;
    private Long poolId;
    private String poolName;
    private Long createdBy;
    private String createdByName;
    private BigDecimal amount;
    private String description;
    private SplitType splitType;
    private ExpenseStatus status;
    private LocalDateTime createdAt;
    private List<ExpenseSplitResponse> splits;
}
