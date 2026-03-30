package app.wallet.smart_wallet.service.impl;


import app.wallet.smart_wallet.dto.request.LoginRequest;
import app.wallet.smart_wallet.dto.request.UserRequest;
import app.wallet.smart_wallet.dto.response.AuthResponse;
import app.wallet.smart_wallet.entity.User;
import app.wallet.smart_wallet.entity.Wallet;
import app.wallet.smart_wallet.entity.enums.Role;
import app.wallet.smart_wallet.exception.BadRequestException;
import app.wallet.smart_wallet.repository.UserRepository;
import app.wallet.smart_wallet.repository.WalletRepository;
import app.wallet.smart_wallet.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        Role role = request.getRole() == null ? Role.MEMBER : request.getRole();

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail().toLowerCase())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .build();

        User savedUser = userRepository.save(user);

        Wallet wallet = Wallet.builder()
            .user(savedUser)
            .build();
        walletRepository.save(wallet);

        return buildAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            .authorities("ROLE_" + user.getRole().name())
            .build();

        String token = jwtService.generateToken(userDetails);
        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .userId(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }
}
