package app.wallet.smart_wallet.service;

import app.wallet.smart_wallet.dto.request.AddMoneyRequest;
import app.wallet.smart_wallet.dto.response.WalletResponse;

public interface WalletService {

    WalletResponse getCurrentWallet();

    WalletResponse addMoney(AddMoneyRequest request);
}