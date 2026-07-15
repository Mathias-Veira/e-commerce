package com.project.e_commerce.unit.user;

import com.project.e_commerce.application.UserUseCaseImpl;
import com.project.e_commerce.application.exceptions.user.LoginFailedException;
import com.project.e_commerce.application.exceptions.user.UserAlreadyExistsException;
import com.project.e_commerce.application.exceptions.user.UserNotFoundException;
import com.project.e_commerce.domain.Token;
import com.project.e_commerce.domain.User;
import com.project.e_commerce.domain.exceptions.UserNotValidException;
import com.project.e_commerce.domain.ports.out.UserRepository;
import com.project.e_commerce.infrastructure.configuration.JWTService;
import com.project.e_commerce.infrastructure.persistence.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.project.e_commerce.infrastructure.persistence.user.Role;

import java.util.Optional;
import java.util.Set;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTService jwtService;

    private UserUseCaseImpl userUseCaseImpl;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        userUseCaseImpl = new UserUseCaseImpl(userRepository, encoder, jwtService);
    }

    private User existingUser(int id, String email, String username, String hashedPassword) {
        return new User(id, email, username, hashedPassword);
    }

    // --- login ---

    @Test
    void login_invalidEmailFormat_throwsUserNotValidException_andNeverHitsRepository() {
        assertThrows(UserNotValidException.class, () -> userUseCaseImpl.login("not-an-email", "password1"));
        verify(userRepository, never()).getUserByEmail(any());
    }

    @Test
    void login_invalidPasswordFormat_throwsUserNotValidException_andNeverHitsRepository() {
        assertThrows(UserNotValidException.class, () -> userUseCaseImpl.login("user@example.com", "short"));
        verify(userRepository, never()).getUserByEmail(any());
    }

    @Test
    void login_emailNotRegistered_throwsUserNotFoundException() {
        when(userRepository.getUserByEmail("user@example.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userUseCaseImpl.login("user@example.com", "password1"));
    }

    @Test
    void login_correctCredentials_returnsTokenWithBothParts() {
        String hashed = encoder.encode("password1");
        when(userRepository.getUserByEmail("user@example.com"))
                .thenReturn(Optional.of(existingUser(1, "user@example.com", "user", hashed)));
        when(userRepository.getUserRolesByEmail("user@example.com")).thenReturn(Set.of(Role.USER));
        when(jwtService.generateToken("user@example.com", Set.of(Role.USER))).thenReturn("access-token");
        when(jwtService.generateRefreshToken("user@example.com")).thenReturn("refresh-token");

        Token result = userUseCaseImpl.login("user@example.com", "password1");

        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
    }

    @Test
    void login_wrongPassword_throwsLoginFailedException() {
        String hashed = encoder.encode("password1");
        when(userRepository.getUserByEmail("user@example.com"))
                .thenReturn(Optional.of(existingUser(1, "user@example.com", "user", hashed)));
        when(userRepository.getUserRolesByEmail("user@example.com")).thenReturn(Set.of(Role.USER));

        assertThrows(LoginFailedException.class, () -> userUseCaseImpl.login("user@example.com", "wrongPassword"));
    }

    // --- sendRefreshToken ---

    @Test
    void sendRefreshToken_notARefreshToken_throwsUserNotValidException() {
        when(jwtService.isRefreshtoken("bad-token")).thenReturn(false);

        assertThrows(UserNotValidException.class, () -> userUseCaseImpl.sendRefreshToken("bad-token"));
    }

    @Test
    void sendRefreshToken_invalidToken_throwsUserNotValidException() {
        when(jwtService.isRefreshtoken("bad-token")).thenReturn(true);
        when(jwtService.isValid("bad-token")).thenReturn(false);

        assertThrows(UserNotValidException.class, () -> userUseCaseImpl.sendRefreshToken("bad-token"));
    }

    @Test
    void sendRefreshToken_validRefreshToken_returnsNewTokenPair() {
        when(jwtService.isRefreshtoken("refresh-token")).thenReturn(true);
        when(jwtService.isValid("refresh-token")).thenReturn(true);
        when(jwtService.extractUserEmail("refresh-token")).thenReturn("user@example.com");
        when(userRepository.getUserRolesByEmail("user@example.com")).thenReturn(Set.of(Role.USER));
        when(jwtService.generateToken("user@example.com", Set.of(Role.USER))).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken("user@example.com")).thenReturn("new-refresh-token");

        Token result = userUseCaseImpl.sendRefreshToken("refresh-token");

        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
    }

    // --- addUser ---

    @Test
    void addUser_invalidEmailFormat_throwsUserNotValidException_andNeverPersists() {
        User user = new User(0, "not-an-email", "user", "password1");

        assertThrows(UserNotValidException.class, () -> userUseCaseImpl.addUser(user));
        verify(userRepository, never()).addUser(any());
    }

    @Test
    void addUser_invalidUsernameFormat_throwsUserNotValidException() {
        User user = new User(0, "user@example.com", "a", "password1");

        assertThrows(UserNotValidException.class, () -> userUseCaseImpl.addUser(user));
        verify(userRepository, never()).addUser(any());
    }

    @Test
    void addUser_invalidPasswordFormat_throwsUserNotValidException() {
        User user = new User(0, "user@example.com", "user", "short");

        assertThrows(UserNotValidException.class, () -> userUseCaseImpl.addUser(user));
        verify(userRepository, never()).addUser(any());
    }

    @Test
    void addUser_emailAlreadyRegistered_throwsUserAlreadyExistsException() {
        User user = new User(0, "user@example.com", "user", "password1");
        when(userRepository.getUserByEmail("user@example.com"))
                .thenReturn(Optional.of(existingUser(1, "user@example.com", "other", encoder.encode("x"))));

        assertThrows(UserAlreadyExistsException.class, () -> userUseCaseImpl.addUser(user));
        verify(userRepository, never()).addUser(any());
    }

    @Test
    void addUser_validUser_hashesPasswordBeforePersisting() {
        User user = new User(0, "user@example.com", "user", "password1");
        when(userRepository.getUserByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.addUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        userUseCaseImpl.addUser(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).addUser(captor.capture());
        String persistedPassword = captor.getValue().getUserPassword();

        assertNotEquals("password1", persistedPassword);
        assertTrue(encoder.matches("password1", persistedPassword));
    }

    @Test
    void addUser_validUser_returnsUserFromRepository() {
        User user = new User(0, "user@example.com", "user", "password1");
        User saved = existingUser(42, "user@example.com", "user", "irrelevant-hash");
        when(userRepository.getUserByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.addUser(any())).thenReturn(saved);

        User result = userUseCaseImpl.addUser(user);

        assertEquals(42, result.getUserId());
    }

    // --- updateUser ---

    @Test
    void updateUser_userDoesNotExist_throwsUserNotFoundException() {
        User user = new User(99, "user@example.com", "user", "password1");
        when(userRepository.getUserById(99)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userUseCaseImpl.updateUser(user));
    }

    @Test
    void updateUser_invalidEmailFormat_throwsUserNotValidException() {
        User existing = existingUser(1, "old@example.com", "user", encoder.encode("password1"));
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existing));

        User update = new User(1, "not-an-email", "user", "password1");

        assertThrows(UserNotValidException.class, () -> userUseCaseImpl.updateUser(update));
    }

    @Test
    void updateUser_invalidUsernameFormat_throwsUserNotValidException() {
        User existing = existingUser(1, "user@example.com", "user", encoder.encode("password1"));
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existing));

        User update = new User(1, "user@example.com", "a", "password1");

        assertThrows(UserNotValidException.class, () -> userUseCaseImpl.updateUser(update));
    }

    @Test
    void updateUser_invalidPasswordFormat_throwsUserNotValidException() {
        User existing = existingUser(1, "user@example.com", "user", encoder.encode("password1"));
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existing));

        User update = new User(1, "user@example.com", "user", "short");

        assertThrows(UserNotValidException.class, () -> userUseCaseImpl.updateUser(update));
    }

    @Test
    void updateUser_emailAlreadyUsedByAnotherUser_throwsUserAlreadyExistsException() {
        User existing = existingUser(1, "user@example.com", "user", encoder.encode("password1"));
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existing));
        when(userRepository.getUserByEmail("taken@example.com"))
                .thenReturn(Optional.of(existingUser(2, "taken@example.com", "other", encoder.encode("x"))));

        User update = new User(1, "taken@example.com", "user", "password1");

        assertThrows(UserAlreadyExistsException.class, () -> userUseCaseImpl.updateUser(update));
    }

    @Test
    void updateUser_sameEmailAsOwnExistingUser_isAllowed() {
        User existing = existingUser(1, "user@example.com", "user", encoder.encode("password1"));
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existing));
        when(userRepository.addUser(any())).thenAnswer(invocation -> invocation.getArgument(0));
        User update = new User(1, "user@example.com", "renamed", "newPassword1");

        assertDoesNotThrow(() -> userUseCaseImpl.updateUser(update));
    }

    @Test
    void updateUser_samePasswordAsBefore_isNoOp_keepsExistingHashWithoutRehashing() {
        String hashed = encoder.encode("password1");
        User existing = existingUser(1, "user@example.com", "user", hashed);
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existing));
        when(userRepository.addUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Sending back the same plaintext password just to change the username
        // must not be treated as an invalid/duplicate password change.
        User update = new User(1, "user@example.com", "renamed", "password1");

        User result = assertDoesNotThrow(() -> userUseCaseImpl.updateUser(update));

        assertEquals(hashed, result.getUserPassword());
    }

    @Test
    void updateUser_newPasswordDifferent_rehashesAndPersists() {
        String hashed = encoder.encode("password1");
        User existing = existingUser(1, "user@example.com", "user", hashed);
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existing));
        when(userRepository.addUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User update = new User(1, "user@example.com", "user", "newPassword1");
        userUseCaseImpl.updateUser(update);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).addUser(captor.capture());
        String persistedPassword = captor.getValue().getUserPassword();

        assertNotEquals("newPassword1", persistedPassword);
        assertTrue(encoder.matches("newPassword1", persistedPassword));
    }

    @Test
    void updateUser_preservesUserId_evenIfCallerSendsDifferentId() {
        User existing = existingUser(1, "user@example.com", "user", encoder.encode("password1"));
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existing));
        when(userRepository.addUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User update = new User(1, "user@example.com", "renamed", "newPassword1");
        User result = userUseCaseImpl.updateUser(update);

        assertEquals(1, result.getUserId());
    }

    // --- getUserById ---

    @Test
    void getUserById_existingId_returnsUser() {
        User existing = existingUser(1, "user@example.com", "user", "hashed");
        when(userRepository.getUserById(1)).thenReturn(Optional.of(existing));

        User result = userUseCaseImpl.getUserById(1);

        assertEquals(existing, result);
    }

    @Test
    void getUserById_nonExistingId_throwsUserNotFoundException() {
        when(userRepository.getUserById(404)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userUseCaseImpl.getUserById(404));
    }
}
