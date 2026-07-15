package com.project.e_commerce.infrastructure.web.mapper;

import com.project.e_commerce.domain.User;
import com.project.e_commerce.infrastructure.web.dto.request.UserDTORequest;
import com.project.e_commerce.infrastructure.web.dto.response.UserDTOResponse;

public class UserDTOMapper {

    public static User toDomain(UserDTORequest request) {
        if (request == null) {
            return null;
        }
        return new User(
                request.getUserId(),
                request.getUserEmail(),
                request.getUserName(),
                request.getUserPassword()
        );
    }

    public static UserDTOResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTOResponse(
                user.getUserId(),
                user.getUserEmail(),
                user.getUserName()
        );
    }
}
