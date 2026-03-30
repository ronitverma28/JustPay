package app.wallet.smart_wallet.service;

import com.smartwallet.dto.AddMoneyRequest;
import com.smartwallet.dto.WalletResponse;
import com.smartwallet.entity.Pool;
import com.smartwallet.entity.User;
import com.smartwallet.entity.Wallet;
import com.smartwallet.enums.Role;
import com.smartwallet.enums.TransactionStatus;
import com.smartwallet.enums.TransactionType;
import com.smartwallet.exception.ForbiddenOperationException;
import com.smartwallet.exception.ResourceNotFoundException;
import com.smartwallet.repository.PoolRepository;
import com.smartwallet.repository.WalletRepository;
import com.smartwallet.util.EntityMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final PoolRepository poolRepository;
    private final UserService userService;
    private final TransactionService transactionService;

    @Transactional(readOnly = true)
    public WalletResponse getCurrentWallet() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Wallet wallet = walletRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user"));
        return EntityMapper.toWalletResponse(wallet);
    }

    @Transactional
    public WalletResponse addMoney(AddMoneyRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Wallet wallet = walletRepository.findByUserIdForUpdate(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user"));

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        Pool pool = null;

        if (request.getPoolId() != null) {
            pool = poolRepository.findByIdForUpdate(request.getPoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Pool not found with id: " + request.getPoolId()));
            boolean isMember = pool.getMembers().stream().anyMatch(member -> member.getId().equals(currentUser.getId()));
            if (currentUser.getRole() != Role.ADMIN && !isMember) {
                throw new ForbiddenOperationException("Only pool members can add money to this pool");
            }
            pool.setPoolBalance(pool.getPoolBalance().add(request.getAmount()));
            poolRepository.save(pool);
        }

        walletRepository.save(wallet);
        String referenceId = "WALLET_TOPUP_" + UUID.randomUUID();
        transactionService.createTransaction(
            currentUser,
            pool,
            request.getAmount(),
            TransactionType.ADD_MONEY,
            TransactionStatus.SUCCESS,
            referenceId
        );

        return EntityMapper.toWalletResponse(wallet);
    }
}
