package com.project.e_commerce.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderLine {
    private int productId;
    private String productName;
    private BigDecimal productUnitPrice;
    private int quantity;

    public static boolean validateQuantity(int quantity) {
        return quantity>=1 && quantity<=100;
    }

    public static boolean validateUnitPrice(BigDecimal productUnitPrice) {
        if(productUnitPrice == null) return false;
        if(productUnitPrice.compareTo(BigDecimal.ZERO)<0) return false;
        if(productUnitPrice.stripTrailingZeros().scale()>2) return false;
        return true;
    }
}
