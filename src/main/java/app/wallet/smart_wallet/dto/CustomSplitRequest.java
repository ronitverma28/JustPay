package app.wallet.smart_wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CustomSplitRequest {

    @NotNull(message = "userId is required")
    @Positive(message = "userId must be positive")
    private Long userId;

    @NotNull(message = "shareAmount is required")
    @DecimalMin(value = "0.01", message = "shareAmount must be greater than 0")
    private BigDecimal shareAmount;
}
