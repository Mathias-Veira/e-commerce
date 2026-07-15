package com.project.e_commerce.infrastructure.web.dto.request;

import com.project.e_commerce.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductDTORequest {
    private int productId;
    private String productName;
    private List<Category> categories;
    private BigDecimal productPrice;
    private String productDescription;
    private int productStock;
    private boolean discontinued;
}
