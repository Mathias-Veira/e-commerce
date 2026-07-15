package com.project.e_commerce.domain;


import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Product {
    @Setter(AccessLevel.NONE)
    private int productId;
    @NonNull
    private String productName;
    @NonNull
    private List<Category> categories;
    @NonNull
    private BigDecimal productPrice;
    @NonNull
    private String productDescription;
    private int productStock;
    private boolean discontinued;

    public static boolean validatePrice(BigDecimal productPrice){
        if(productPrice == null) return false;
        if(productPrice.compareTo(BigDecimal.ZERO)<0){
            return false;
        }
        if(productPrice.stripTrailingZeros().scale()>2){
            return false;
        }
        return true;
    }

    public static boolean validateStock(int productStock) {
        if(productStock<0)return false;
        return true;
    }

    public static boolean validateDescription(String productDescription) {
        if(productDescription == null) return false;
        if(productDescription.length()>2000) return false;
        return true;
    }

    public static boolean validateCategories(List<Category> categories) {
        if(categories == null || categories.isEmpty()) return false;
        return true;
    }

    public boolean isOutOfStock() {
        return this.productStock == 0;
    }
}
