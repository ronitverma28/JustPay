package app.wallet.smart_wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RazorpayVerifyRequest {

    @NotBlank(message = "razorpayOrderId is required")
    private String razorpayOrderId;

    @NotBlank(message = "razorpayPaymentId is required")
    private String razorpayPaymentId;

    @NotBlank(message = "razorpaySignature is required")
    private String razorpaySignature;
}
