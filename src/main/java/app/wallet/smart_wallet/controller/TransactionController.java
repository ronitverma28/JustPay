package app.wallet.smart_wallet.controller;

import app.wallet.smart_wallet.dto.response.TransactionResponse;
import app.wallet.smart_wallet.service.TransactionService;
import app.wallet.smart_wallet.util.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions() {
        List<TransactionResponse> response = transactionService.getMyTransactions();
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", response));
    }
}
