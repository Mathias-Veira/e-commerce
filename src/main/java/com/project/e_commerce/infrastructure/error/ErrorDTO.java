package com.project.e_commerce.infrastructure.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ErrorDTO {
    private HttpStatus statusCode;
    private String mensaje;
}
