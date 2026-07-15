package com.project.e_commerce.application;

import com.project.e_commerce.application.exceptions.user.LoginFailedException;
import com.project.e_commerce.application.exceptions.user.UserAlreadyExistsException;
import com.project.e_commerce.application.exceptions.user.UserNotFoundException;
import com.project.e_commerce.domain.Token;
import com.project.e_commerce.domain.exceptions.UserNotValidException;
import com.project.e_commerce.domain.ports.in.UserUseCase;
import com.project.e_commerce.domain.ports.out.UserRepository;
import com.project.e_commerce.infrastructure.configuration.JWTService;
import com.project.e_commerce.infrastructure.persistence.user.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.e_commerce.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserUseCaseImpl implements UserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JWTService jwtService;

    public UserUseCaseImpl(UserRepository userRepository, PasswordEncoder encoder,JWTService jwtService) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @Override
    public Token login(String userEmail, String userPassword) {
        if (!User.validateEmail(userEmail) || !User.validatePassword(userPassword)) {
            throw new UserNotValidException("Login was not successful");
        }
        User user = userRepository.getUserByEmail(userEmail).orElseThrow(()-> new UserNotFoundException("User with that email does not exist"));
        Set<Role> roles = userRepository.getUserRolesByEmail(userEmail);
        if(roles.isEmpty()) throw new UserNotFoundException("User does not exist");
        if(!encoder.matches(userPassword,user.getUserPassword()))throw new LoginFailedException("Login was not successful");
        return new Token(jwtService.generateToken(user.getUserEmail(),roles), jwtService.generateRefreshToken(user.getUserEmail()) );
    }

    @Override
    public Token sendRefreshToken(String refreshToken) {
        if(!jwtService.isRefreshtoken(refreshToken) || !jwtService.isValid(refreshToken)) throw new UserNotValidException("Not a refresh token");
        String userEmail = jwtService.extractUserEmail(refreshToken);
        Set<Role> roles = userRepository.getUserRolesByEmail(userEmail);
        if(roles.isEmpty()) throw new UserNotFoundException("User does not exist");
        return new Token(jwtService.generateToken(userEmail,roles), jwtService.generateRefreshToken(userEmail) );
    }

    @Override
    public User addUser(User user) {
        if (!User.validateEmail(user.getUserEmail())|| !User.validateUsername(user.getUserName())) {
            throw new UserNotValidException("User does not pass security checks");
        }
        if(!User.validatePassword(user.getUserPassword())){
            throw new UserNotValidException("User password does not pass security checks");
        }
        Optional<User> optionalUser = userRepository.getUserByEmail(user.getUserEmail());
        if(optionalUser.isPresent()) throw new UserAlreadyExistsException("User with that email already exists");
        User returnedUser = new User(user.getUserId(),user.getUserEmail(), user.getUserName(), encoder.encode(user.getUserPassword()));
        return userRepository.addUser(returnedUser);
    }

    @Override
    public User updateUser(User user) {
        String password = "";
        Optional<User> optionalUser = userRepository.getUserById(user.getUserId());
        if(optionalUser.isEmpty()) throw new UserNotFoundException("User does not exist");
        User updatedUser = optionalUser.get();
        if(!encoder.matches(user.getUserPassword(), updatedUser.getUserPassword())){
            if(!User.validatePassword(user.getUserPassword())){
                throw new UserNotValidException("User password does not pass security checks");
            }
            password = encoder.encode(user.getUserPassword());
        }else{
            password = updatedUser.getUserPassword();
        }
        if (!User.validateEmail(user.getUserEmail())|| !User.validateUsername(user.getUserName())) {
            throw new UserNotValidException("User does not pass security checks");
        }
        if(!user.getUserEmail().equals(updatedUser.getUserEmail())){
            if(userRepository.getUserByEmail(user.getUserEmail()).isPresent()) throw new UserAlreadyExistsException("User with that email already exists");
        }
        User returnedUser = new User(user.getUserId(),user.getUserEmail(), user.getUserName(), password);
        return userRepository.addUser(returnedUser);
    }

    @Override
    public User getUserById(int userId) {
        return userRepository.getUserById(userId).orElseThrow(()-> new UserNotFoundException("User does not exist"));
    }
}
