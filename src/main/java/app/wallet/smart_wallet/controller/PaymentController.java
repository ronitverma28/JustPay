package app.wallet.smart_wallet.controller;

import app.wallet.smart_wallet.dto.request.ExpensePaymentRequest;
import app.wallet.smart_wallet.dto.request.ManualPaymentRequest;
import app.wallet.smart_wallet.dto.request.RazorpayVerifyRequest;
import app.wallet.smart_wallet.dto.request.RazorpayOrderRequest;
import app.wallet.smart_wallet.dto.response.RazorpayOrderResponse;
import app.wallet.smart_wallet.dto.response.ExpenseResponse;
import app.wallet.smart_wallet.dto.response.TransactionResponse;
import app.wallet.smart_wallet.service.PaymentService;
import app.wallet.smart_wallet.util.ApiResponse;
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
        return ResponseEntity.ok(ApiResponse.success("Razorpay order created", response));
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<ApiResponse<TransactionResponse>> verifyPayment(
        @Valid @RequestBody RazorpayVerifyRequest request
    ) {
        TransactionResponse response = paymentService.verifyRazorpayPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified and wallet updated", response));
    }

    @PostMapping(value = "/razorpay/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> webhook(
        @RequestBody String payload,
        @RequestHeader("X-Razorpay-Signature") String signature
    ) {
        String response = paymentService.handleRazorpayWebhook(payload, signature);
        return ResponseEntity.ok(ApiResponse.success("Webhook accepted", response));
    }

    @PostMapping("/expense/pay")
    public ResponseEntity<ApiResponse<ExpenseResponse>> payExpense(@Valid @RequestBody ExpensePaymentRequest request) {
        ExpenseResponse response = paymentService.payExpenseFromPool(request);
        return ResponseEntity.ok(ApiResponse.success("Expense paid successfully", response));
    }

    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<ExpenseResponse>> manualPayment(@Valid @RequestBody ManualPaymentRequest request) {
        ExpenseResponse response = paymentService.markExpensePaidManually(request);
        return ResponseEntity.ok(ApiResponse.success("Expense marked as paid manually", response));
    }
}
