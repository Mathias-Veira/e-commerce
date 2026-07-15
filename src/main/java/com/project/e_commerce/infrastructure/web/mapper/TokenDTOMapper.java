package com.project.e_commerce.infrastructure.web.mapper;

import com.project.e_commerce.domain.Token;
import com.project.e_commerce.infrastructure.web.dto.response.TokenDTOResponse;

public class TokenDTOMapper {

    public static TokenDTOResponse toResponse(Token token) {
        return new TokenDTOResponse(token.getAccessToken(), token.getRefreshToken());
    }

    public static Token toDomain(TokenDTOResponse response) {
        return new Token(response.accessToken(), response.refreshToken());
    }
}
