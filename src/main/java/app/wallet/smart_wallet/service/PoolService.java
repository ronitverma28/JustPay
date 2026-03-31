package app.wallet.smart_wallet.service;

import app.wallet.smart_wallet.dto.request.CreatePoolRequest;
import app.wallet.smart_wallet.dto.response.PoolResponse;

public interface PoolService {

    PoolResponse createPool(CreatePoolRequest request);

    PoolResponse addMember(Long poolId, Long userId);

    PoolResponse removeMember(Long poolId, Long userId);

    PoolResponse getPoolDetails(Long poolId);
}