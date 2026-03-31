package app.wallet.smart_wallet.service;

import app.wallet.smart_wallet.dto.request.*;
import app.wallet.smart_wallet.dto.response.*;

public interface PaymentService {

    RazorpayOrderResponse createRazorpayOrder(RazorpayOrderRequest request);

    TransactionResponse verifyRazorpayPayment(RazorpayVerifyRequest request);

    String handleRazorpayWebhook(String payload, String signatureHeader);

    ExpenseResponse payExpenseFromPool(ExpensePaymentRequest request);

    ExpenseResponse markExpensePaidManually(ManualPaymentRequest request);
}