package com.project.e_commerce.infrastructure.persistence.user;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.project.e_commerce.domain.User;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getUserId(),
                entity.getUserEmail(),
                entity.getUserName(),
                entity.getUserPassword()
        );
    }

    public static UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserEntity(
                user.getUserId(),
                user.getUserName(),
                user.getUserPassword(),
                user.getUserEmail(),
                new HashSet<>(Set.of(Role.USER))
        );
    }

    public static List<User> toDomainList(List<UserEntity> entities) {
        return entities.stream().map(UserMapper::toDomain).toList();
    }

    public static List<UserEntity> toEntityList(List<User> users) {
        return users.stream().map(UserMapper::toEntity).toList();
    }
}
