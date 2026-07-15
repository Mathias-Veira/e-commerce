package com.project.e_commerce.infrastructure.persistence.product;


import com.project.e_commerce.infrastructure.persistence.category.CategoryEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Products")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="product_id")
    private int productId;
    @Column(name="product_name")
    private String productName;
    @ManyToMany
    @JoinTable(name = "product_category",joinColumns = @JoinColumn(name = "product_id"),inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<CategoryEntity> categories;
    @Column(name="product_price")
    private BigDecimal productPrice;
    @Column(name="product_description")
    private String productDescription;
    @Column(name="product_stock")
    private int productStock;
    @Column(name="discontinued")
    private boolean discontinued;
}
