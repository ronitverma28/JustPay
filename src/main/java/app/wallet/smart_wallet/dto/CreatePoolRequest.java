package app.wallet.smart_wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePoolRequest {

    @NotBlank(message = "Pool name is required")
    @Size(max = 120, message = "Pool name must be at most 120 characters")
    private String name;
}
