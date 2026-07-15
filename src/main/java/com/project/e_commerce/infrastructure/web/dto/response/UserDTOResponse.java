package com.project.e_commerce.infrastructure.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTOResponse {
    private int userId;
    private String userEmail;
    private String userName;
}
