package app.wallet.smart_wallet.dto.response;

import com.smartwallet.enums.SplitStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplitResponse {

    private Long userId;
    private String userName;
    private BigDecimal shareAmount;
    private SplitStatus status;
}
