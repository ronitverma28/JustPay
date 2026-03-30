package app.wallet.smart_wallet.service.impl;

import app.wallet.smart_wallet.dto.request.AddMoneyRequest;
import app.wallet.smart_wallet.dto.response.WalletResponse;
import app.wallet.smart_wallet.entity.Pool;
import app.wallet.smart_wallet.entity.User;
import app.wallet.smart_wallet.entity.Wallet;
import app.wallet.smart_wallet.entity.enums.Role;
import app.wallet.smart_wallet.entity.enums.TransactionStatus;
import app.wallet.smart_wallet.entity.enums.TransactionType;
import app.wallet.smart_wallet.exception.ForbiddenOperationException;
import app.wallet.smart_wallet.exception.ResourceNotFoundException;
import app.wallet.smart_wallet.repository.PoolRepository;
import app.wallet.smart_wallet.repository.WalletRepository;
import app.wallet.smart_wallet.util.EntityMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl {

    private final WalletRepository walletRepository;
    private final PoolRepository poolRepository;
    private final UserServiceImpl userServiceImpl;
    private final TransactionServiceImpl transactionServiceImpl;

    @Transactional(readOnly = true)
    public WalletResponse getCurrentWallet() {
        User currentUser = userServiceImpl.getCurrentAuthenticatedUser();
        Wallet wallet = walletRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user"));
        return EntityMapper.toWalletResponse(wallet);
    }

    @Transactional
    public WalletResponse addMoney(AddMoneyRequest request) {
        User currentUser = userServiceImpl.getCurrentAuthenticatedUser();
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
        transactionServiceImpl.createTransaction(
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
