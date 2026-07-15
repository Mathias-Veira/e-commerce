package com.project.e_commerce.domain.ports.in;

import com.project.e_commerce.domain.Token;
import com.project.e_commerce.domain.User;

public interface UserUseCase {
    Token login(String userEmail, String userPassword);
    Token sendRefreshToken(String refreshToken);
    User addUser(User user);
    User updateUser(User user);
    User getUserById(int userId);
}
