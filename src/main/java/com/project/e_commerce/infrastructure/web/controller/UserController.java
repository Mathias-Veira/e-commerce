package com.project.e_commerce.infrastructure.web.controller;

import com.project.e_commerce.domain.Token;
import com.project.e_commerce.domain.User;
import com.project.e_commerce.domain.ports.in.UserUseCase;
import com.project.e_commerce.infrastructure.web.dto.request.LoginDTORequest;
import com.project.e_commerce.infrastructure.web.dto.request.RefreshTokenDTORequest;
import com.project.e_commerce.infrastructure.web.dto.request.UserDTORequest;
import com.project.e_commerce.infrastructure.web.dto.response.TokenDTOResponse;
import com.project.e_commerce.infrastructure.web.dto.response.UserDTOResponse;
import com.project.e_commerce.infrastructure.web.mapper.TokenDTOMapper;
import com.project.e_commerce.infrastructure.web.mapper.UserDTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTOResponse> login(@RequestBody LoginDTORequest request) {
        Token token = userUseCase.login(request.getUserEmail(), request.getUserPassword());
        return ResponseEntity.ok(TokenDTOMapper.toResponse(token));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<TokenDTOResponse> refresh(@RequestBody RefreshTokenDTORequest refreshTokenDTORequest){
        return ResponseEntity.ok(TokenDTOMapper.toResponse(userUseCase.sendRefreshToken(refreshTokenDTORequest.refreshToken())));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTOResponse> addUser(@RequestBody UserDTORequest request) {
        User user = userUseCase.addUser(UserDTOMapper.toDomain(request));
        return ResponseEntity.ok(UserDTOMapper.toResponse(user));
    }

    @PutMapping("/update")
    public ResponseEntity<UserDTOResponse> updateUser(@RequestBody UserDTORequest request) {
        User user = userUseCase.updateUser(UserDTOMapper.toDomain(request));
        return ResponseEntity.ok(UserDTOMapper.toResponse(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTOResponse> getUserById(@PathVariable int userId) {
        User user = userUseCase.getUserById(userId);
        return ResponseEntity.ok(UserDTOMapper.toResponse(user));
    }
}
