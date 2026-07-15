package com.project.e_commerce.domain.ports.out;

import com.project.e_commerce.domain.User;
import com.project.e_commerce.infrastructure.persistence.user.Role;

import java.util.Optional;
import java.util.Set;

public interface UserRepository {

    User addUser(User user);
    Optional<User> getUserById(int userId);
    Optional<User> getUserByEmail(String userEmail);
    Set<Role> getUserRolesByEmail(String userEmail);
}
