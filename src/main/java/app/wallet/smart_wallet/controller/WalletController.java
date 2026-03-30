package app.wallet.smart_wallet.controller;

import com.smartwallet.dto.AddMoneyRequest;
import com.smartwallet.dto.ApiResponse;
import com.smartwallet.dto.WalletResponse;
import com.smartwallet.service.WalletService;
import com.smartwallet.util.ApiResponseUtil;
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
        return ResponseEntity.ok(ApiResponseUtil.success("Wallet fetched successfully", response));
    }

    @PostMapping("/add-money")
    public ResponseEntity<ApiResponse<WalletResponse>> addMoney(@Valid @RequestBody AddMoneyRequest request) {
        WalletResponse response = walletService.addMoney(request);
        return ResponseEntity.ok(ApiResponseUtil.success("Wallet credited successfully", response));
    }
}
