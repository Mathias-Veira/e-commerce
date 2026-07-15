package com.project.e_commerce.infrastructure.persistence.order;

import com.project.e_commerce.domain.OrderState;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Orders")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_id")
    private int orderId;
    @Column(name="user_id")
    private int userId;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderLineEntity> items;
    @Column(name="order_date")
    private LocalDateTime orderDate;
    @Column(name="order_delivery_date")
    private LocalDate deliveryDate;
    @Column(name="order_total")
    private BigDecimal orderTotal;
    @Enumerated(EnumType.STRING)
    @Column(name="order_state")
    private OrderState orderState;
}
