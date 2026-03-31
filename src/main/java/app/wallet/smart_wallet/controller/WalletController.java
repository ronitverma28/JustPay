package app.wallet.smart_wallet.controller;

import app.wallet.smart_wallet.dto.request.AddMoneyRequest;
import app.wallet.smart_wallet.dto.response.WalletResponse;
import app.wallet.smart_wallet.service.WalletService;
import app.wallet.smart_wallet.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet() {
        WalletResponse response = walletService.getCurrentWallet();
        return ResponseEntity.ok(ApiResponse.success("Wallet fetched successfully", response));
    }

    @PostMapping("/add-money")
    public ResponseEntity<ApiResponse<WalletResponse>> addMoney(@Valid @RequestBody AddMoneyRequest request) {
        WalletResponse response = walletService.addMoney(request);
        return ResponseEntity.ok(ApiResponse.success("Wallet credited successfully", response));
    }
}
