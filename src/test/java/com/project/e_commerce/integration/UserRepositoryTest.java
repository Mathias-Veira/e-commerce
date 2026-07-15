package com.project.e_commerce.integration;

import com.project.e_commerce.domain.User;
import com.project.e_commerce.domain.ports.out.UserRepository;
import com.project.e_commerce.infrastructure.persistence.user.UserRepositoryJPA;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(UserRepositoryJPA.class)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String email, String name, String password) {
        User user = new User();
        user.setUserEmail(email);
        user.setUserName(name);
        user.setUserPassword(password);
        return user;
    }

    // ── addUser ───────────────────────────────────────────────────────────────

    @Test
    void addUser_ShouldPersistUserAndReturnWithGeneratedId() {
        User saved = userRepository.addUser(buildUser("john@example.com", "John Doe", "$2a$12$hashedpassword"));

        assertThat(saved.getUserId()).isPositive();
        assertThat(saved.getUserEmail()).isEqualTo("john@example.com");
        assertThat(saved.getUserName()).isEqualTo("John Doe");
        assertThat(saved.getUserPassword()).isEqualTo("$2a$12$hashedpassword");
    }

    @Test
    void addUser_ShouldBeRetrievableAfterPersisting() {
        User saved = userRepository.addUser(buildUser("persist@example.com", "Persist User", "hashed"));

        assertThat(userRepository.getUserById(saved.getUserId())).isPresent();
    }

    @Test
    void addUser_EachUserShouldReceiveUniqueId() {
        User first = userRepository.addUser(buildUser("first@example.com", "First", "pass1"));
        User second = userRepository.addUser(buildUser("second@example.com", "Second", "pass2"));

        assertThat(first.getUserId()).isNotEqualTo(second.getUserId());
    }

    @Test
    void addUser_ShouldPersistAllFieldsWithoutModification() {
        String email = "exact@example.com";
        String name = "Exact Name";
        String password = "$2a$12$exactHashedPassword123";

        User saved = userRepository.addUser(buildUser(email, name, password));
        User retrieved = userRepository.getUserById(saved.getUserId()).orElseThrow();

        assertThat(retrieved.getUserEmail()).isEqualTo(email);
        assertThat(retrieved.getUserName()).isEqualTo(name);
        assertThat(retrieved.getUserPassword()).isEqualTo(password);
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        User saved = userRepository.addUser(buildUser("find@example.com", "Find Me", "pass"));

        Optional<User> result = userRepository.getUserById(saved.getUserId());

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(saved.getUserId());
        assertThat(result.get().getUserEmail()).isEqualTo("find@example.com");
        assertThat(result.get().getUserName()).isEqualTo("Find Me");
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        Optional<User> result = userRepository.getUserById(999999);

        assertThat(result).isEmpty();
    }

    @Test
    void getUserById_WithEmptyDatabase_ShouldReturnEmpty() {
        Optional<User> result = userRepository.getUserById(1);

        assertThat(result).isEmpty();
    }

    @Test
    void getUserById_ShouldReturnCorrectUserAmongMultiple() {
        User user1 = userRepository.addUser(buildUser("user1@example.com", "User One", "pass1"));
        User user2 = userRepository.addUser(buildUser("user2@example.com", "User Two", "pass2"));

        Optional<User> result = userRepository.getUserById(user1.getUserId());

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(user1.getUserId());
        assertThat(result.get().getUserEmail()).isEqualTo("user1@example.com");
        assertThat(result.get().getUserEmail()).isNotEqualTo(user2.getUserEmail());
    }

    // ── getUserByEmail ────────────────────────────────────────────────────────

    @Test
    void getUserByEmail_WhenEmailExists_ShouldReturnUser() {
        userRepository.addUser(buildUser("jane@example.com", "Jane Doe", "hashed"));

        Optional<User> result = userRepository.getUserByEmail("jane@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getUserEmail()).isEqualTo("jane@example.com");
        assertThat(result.get().getUserName()).isEqualTo("Jane Doe");
    }

    @Test
    void getUserByEmail_WhenEmailDoesNotExist_ShouldReturnEmpty() {
        Optional<User> result = userRepository.getUserByEmail("nonexistent@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void getUserByEmail_WithEmptyDatabase_ShouldReturnEmpty() {
        Optional<User> result = userRepository.getUserByEmail("any@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void getUserByEmail_WithUpperCaseEmail_ShouldReturnEmpty() {
        userRepository.addUser(buildUser("case@example.com", "Case User", "pass"));

        Optional<User> result = userRepository.getUserByEmail("CASE@EXAMPLE.COM");

        assertThat(result).isEmpty();
    }

    @Test
    void getUserByEmail_WithPartialEmail_ShouldReturnEmpty() {
        userRepository.addUser(buildUser("partial@example.com", "Partial User", "pass"));

        Optional<User> result = userRepository.getUserByEmail("partial");

        assertThat(result).isEmpty();
    }

    @Test
    void getUserByEmail_ShouldReturnCorrectUserAmongMultiple() {
        userRepository.addUser(buildUser("alpha@example.com", "Alpha", "pass1"));
        userRepository.addUser(buildUser("beta@example.com", "Beta", "pass2"));
        userRepository.addUser(buildUser("gamma@example.com", "Gamma", "pass3"));

        Optional<User> result = userRepository.getUserByEmail("beta@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getUserEmail()).isEqualTo("beta@example.com");
        assertThat(result.get().getUserName()).isEqualTo("Beta");
    }

    @Test
    void getUserByEmail_AfterUpdate_ShouldReflectNewEmail() {
        User saved = userRepository.addUser(buildUser("old@example.com", "User", "pass"));
        saved.setUserEmail("new@example.com");
        userRepository.addUser(saved);

        assertThat(userRepository.getUserByEmail("new@example.com")).isPresent();
    }
}
