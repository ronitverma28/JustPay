package app.wallet.smart_wallet.dto.request;

import com.smartwallet.enums.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class CreateExpenseRequest {

    @NotNull(message = "poolId is required")
    @Positive(message = "poolId must be positive")
    private Long poolId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "splitType is required")
    private SplitType splitType;

    @Valid
    private List<CustomSplitRequest> customSplits;
}
