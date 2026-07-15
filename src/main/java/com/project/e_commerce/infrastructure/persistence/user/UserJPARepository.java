package com.project.e_commerce.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJPARepository extends JpaRepository<UserEntity, Integer> {
    @Query("SELECT u FROM UserEntity u where u.userEmail = :user_email")
    Optional<UserEntity> findUserByEmail(@Param(value = "user_email") String email);
}
