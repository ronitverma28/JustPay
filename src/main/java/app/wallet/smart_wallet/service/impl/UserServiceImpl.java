package app.wallet.smart_wallet.service.impl;

import app.wallet.smart_wallet.entity.User;
import app.wallet.smart_wallet.exception.BadRequestException;
import app.wallet.smart_wallet.exception.ResourceNotFoundException;
import app.wallet.smart_wallet.repository.UserRepository;
import app.wallet.smart_wallet.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        if (email == null) {
            throw new BadRequestException("Authenticated user context is missing");
        }
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Transactional(readOnly = true)
    public User getById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
