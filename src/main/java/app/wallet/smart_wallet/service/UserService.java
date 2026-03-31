package app.wallet.smart_wallet.service;

import app.wallet.smart_wallet.entity.User;

public interface UserService {

    User getCurrentAuthenticatedUser();

    User getById(Long userId);
}