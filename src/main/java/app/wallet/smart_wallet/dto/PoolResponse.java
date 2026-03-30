package app.wallet.smart_wallet.dto;

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
public class PoolResponse {

    private Long id;
    private String name;
    private UserSummaryDto createdBy;
    private List<UserSummaryDto> members;
    private BigDecimal poolBalance;
    private LocalDateTime createdAt;
}
