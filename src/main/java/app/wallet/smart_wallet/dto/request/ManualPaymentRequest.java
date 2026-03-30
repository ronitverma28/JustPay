package app.wallet.smart_wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ManualPaymentRequest {

    @NotNull(message = "expenseId is required")
    @Positive(message = "expenseId must be positive")
    private Long expenseId;
}
