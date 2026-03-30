package app.wallet.smart_wallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddMemberRequest {

    @NotNull(message = "userId is required")
    @Positive(message = "userId must be positive")
    private Long userId;
}
