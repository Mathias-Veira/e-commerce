package com.project.e_commerce.infrastructure.web.dto.response;

import com.project.e_commerce.domain.OrderLine;
import com.project.e_commerce.domain.OrderState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderDTOResponse {
    private int orderId;
    private int userId;
    private List<OrderLine> items;
    private LocalDateTime orderDate;
    private LocalDate deliveryDate;
    private BigDecimal orderTotal;
    private OrderState orderState;
}
