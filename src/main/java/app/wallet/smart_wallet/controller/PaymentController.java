package app.wallet.smart_wallet.controller;

import com.smartwallet.dto.ApiResponse;
import com.smartwallet.dto.ExpensePaymentRequest;
import com.smartwallet.dto.ExpenseResponse;
import com.smartwallet.dto.ManualPaymentRequest;
import com.smartwallet.dto.RazorpayOrderRequest;
import com.smartwallet.dto.RazorpayOrderResponse;
import com.smartwallet.dto.RazorpayVerifyRequest;
import com.smartwallet.dto.TransactionResponse;
import com.smartwallet.service.PaymentService;
import com.smartwallet.util.ApiResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/razorpay/order")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createOrder(@Valid @RequestBody RazorpayOrderRequest request) {
        RazorpayOrderResponse response = paymentService.createRazorpayOrder(request);
        return ResponseEntity.ok(ApiResponseUtil.success("Razorpay order created", response));
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<ApiResponse<TransactionResponse>> verifyPayment(
        @Valid @RequestBody RazorpayVerifyRequest request
    ) {
        TransactionResponse response = paymentService.verifyRazorpayPayment(request);
        return ResponseEntity.ok(ApiResponseUtil.success("Payment verified and wallet updated", response));
    }

    @PostMapping(value = "/razorpay/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> webhook(
        @RequestBody String payload,
        @RequestHeader("X-Razorpay-Signature") String signature
    ) {
        String response = paymentService.handleRazorpayWebhook(payload, signature);
        return ResponseEntity.ok(ApiResponseUtil.success("Webhook accepted", response));
    }

    @PostMapping("/expense/pay")
    public ResponseEntity<ApiResponse<ExpenseResponse>> payExpense(@Valid @RequestBody ExpensePaymentRequest request) {
        ExpenseResponse response = paymentService.payExpenseFromPool(request);
        return ResponseEntity.ok(ApiResponseUtil.success("Expense paid successfully", response));
    }

    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<ExpenseResponse>> manualPayment(@Valid @RequestBody ManualPaymentRequest request) {
        ExpenseResponse response = paymentService.markExpensePaidManually(request);
        return ResponseEntity.ok(ApiResponseUtil.success("Expense marked as paid manually", response));
    }
}
