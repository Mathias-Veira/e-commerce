package com.project.e_commerce.infrastructure.persistence.category;

import com.project.e_commerce.infrastructure.persistence.product.ProductEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "Categories")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CategoryEntity implements  Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name="category_id")
        private int categoryId;
        @Column(name="category_name")
        private String categoryName;
        @Column(name="category_description")
        private String categoryDescription;
        @ManyToMany(mappedBy = "categories")
        private List<ProductEntity> products;
}
