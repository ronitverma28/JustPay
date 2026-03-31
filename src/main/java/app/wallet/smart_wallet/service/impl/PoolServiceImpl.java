package app.wallet.smart_wallet.service.impl;

import app.wallet.smart_wallet.dto.response.PoolResponse;
import app.wallet.smart_wallet.dto.request.CreatePoolRequest;
import app.wallet.smart_wallet.entity.Pool;
import app.wallet.smart_wallet.entity.User;
import app.wallet.smart_wallet.entity.enums.Role;
import app.wallet.smart_wallet.exception.BadRequestException;
import app.wallet.smart_wallet.exception.ForbiddenOperationException;
import app.wallet.smart_wallet.exception.ResourceNotFoundException;
import app.wallet.smart_wallet.repository.PoolRepository;
import java.math.BigDecimal;
import java.util.HashSet;

import app.wallet.smart_wallet.service.PoolService;
import app.wallet.smart_wallet.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PoolServiceImpl implements PoolService {

    private final PoolRepository poolRepository;
    private final UserServiceImpl userServiceImpl;

    @Transactional
    public PoolResponse createPool(CreatePoolRequest request) {
        User currentUser = userServiceImpl.getCurrentAuthenticatedUser();
        ensureAdmin(currentUser);

        Pool pool = Pool.builder()
            .name(request.getName())
            .createdBy(currentUser)
            .members(new HashSet<>())
            .poolBalance(BigDecimal.ZERO)
            .build();
        pool.getMembers().add(currentUser);

        Pool savedPool = poolRepository.save(pool);
        return EntityMapper.toPoolResponse(savedPool);
    }

    @Transactional
    public PoolResponse addMember(Long poolId, Long userId) {
        User currentUser = userServiceImpl.getCurrentAuthenticatedUser();
        ensureAdmin(currentUser);

        Pool pool = poolRepository.findByIdForUpdate(poolId)
            .orElseThrow(() -> new ResourceNotFoundException("Pool not found with id: " + poolId));
        User userToAdd = userServiceImpl.getById(userId);

        boolean alreadyMember = pool.getMembers().stream().anyMatch(member -> member.getId().equals(userToAdd.getId()));
        if (alreadyMember) {
            throw new BadRequestException("User is already a member of this pool");
        }

        pool.getMembers().add(userToAdd);
        Pool updatedPool = poolRepository.save(pool);
        return EntityMapper.toPoolResponse(updatedPool);
    }

    @Transactional
    public PoolResponse removeMember(Long poolId, Long userId) {
        User currentUser = userServiceImpl.getCurrentAuthenticatedUser();
        ensureAdmin(currentUser);

        Pool pool = poolRepository.findByIdForUpdate(poolId)
            .orElseThrow(() -> new ResourceNotFoundException("Pool not found with id: " + poolId));

        if (pool.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("Pool creator cannot be removed from the pool");
        }

        boolean removed = pool.getMembers().removeIf(member -> member.getId().equals(userId));
        if (!removed) {
            throw new BadRequestException("User is not a member of this pool");
        }

        Pool updatedPool = poolRepository.save(pool);
        return EntityMapper.toPoolResponse(updatedPool);
    }

    @Transactional(readOnly = true)
    public PoolResponse getPoolDetails(Long poolId) {
        Pool pool = poolRepository.findById(poolId)
            .orElseThrow(() -> new ResourceNotFoundException("Pool not found with id: " + poolId));

        User currentUser = userServiceImpl.getCurrentAuthenticatedUser();
        boolean isMember = pool.getMembers().stream().anyMatch(member -> member.getId().equals(currentUser.getId()));
        if (currentUser.getRole() != Role.ADMIN && !isMember) {
            throw new ForbiddenOperationException("Only pool members can view pool details");
        }

        return EntityMapper.toPoolResponse(pool);
    }

    private void ensureAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Only ADMIN can manage pools");
        }
    }
}
