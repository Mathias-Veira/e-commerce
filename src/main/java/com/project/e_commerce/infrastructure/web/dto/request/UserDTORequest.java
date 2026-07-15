package com.project.e_commerce.infrastructure.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTORequest {
    private int userId;
    private String userEmail;
    private String userName;
    private String userPassword;
}
