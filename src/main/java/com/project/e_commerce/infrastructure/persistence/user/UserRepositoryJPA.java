package com.project.e_commerce.infrastructure.persistence.user;

import com.project.e_commerce.domain.User;
import com.project.e_commerce.domain.ports.out.UserRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryJPA implements UserRepository {
    private final UserJPARepository userJPARepository;

    public UserRepositoryJPA(UserJPARepository userJPARepository) {
        this.userJPARepository = userJPARepository;
    }

    @Override
    public User addUser(User user) {
        return UserMapper.toDomain(userJPARepository.save(UserMapper.toEntity(user)));
    }

    @Override
    public Optional<User> getUserById(int userId) {
        Optional<UserEntity> optionalUser = userJPARepository.findById(userId);
        return optionalUser.map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> getUserByEmail(String userEmail) {
        Optional<UserEntity> optionalUser = userJPARepository.findUserByEmail(userEmail);
        return optionalUser.map(UserMapper::toDomain);
    }

    @Override
    public Set<Role> getUserRolesByEmail(String userEmail) {
        Optional<UserEntity> optionalUser = userJPARepository.findUserByEmail(userEmail);
        if(optionalUser.isEmpty()) return Set.of();
        return optionalUser.get().getRoles().stream().collect(Collectors.toSet());
    }
}
