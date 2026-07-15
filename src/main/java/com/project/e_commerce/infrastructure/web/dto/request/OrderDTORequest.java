package com.project.e_commerce.infrastructure.web.dto.request;

import com.project.e_commerce.domain.OrderLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderDTORequest {
    private int userId;
    private List<OrderLine> items;
}
