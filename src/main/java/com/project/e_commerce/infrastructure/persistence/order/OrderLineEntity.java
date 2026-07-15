package com.project.e_commerce.infrastructure.persistence.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
@Entity
@Table(name = "Order_Lines")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderLineEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_line_id")
    private int orderLineId;
    @ManyToOne()
    @JoinColumn(name = "order_id")
    private OrderEntity order;
    @Column(name="product_id")
    private int productId;
    @Column(name="product_name")
    private String productName;
    @Column(name="product_unit_price")
    private BigDecimal productUnitPrice;
    @Column(name="product_quantity")
    private int quantity;
}
