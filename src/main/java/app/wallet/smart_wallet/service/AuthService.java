package app.wallet.smart_wallet.service;

import app.wallet.smart_wallet.dto.request.LoginRequest;
import app.wallet.smart_wallet.dto.request.UserRequest;
import app.wallet.smart_wallet.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(UserRequest request);

    AuthResponse login(LoginRequest request);
}