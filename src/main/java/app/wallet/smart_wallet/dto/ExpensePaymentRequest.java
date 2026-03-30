package app.wallet.smart_wallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ExpensePaymentRequest {

    @NotNull(message = "expenseId is required")
    @Positive(message = "expenseId must be positive")
    private Long expenseId;
}
