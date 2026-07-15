package com.project.e_commerce.infrastructure.web.dto.request;

import com.project.e_commerce.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CategoryDTORequest {
    private int categoryId;

    private String categoryName;

    private String categoryDescription;
    private List<Product> products;
}
