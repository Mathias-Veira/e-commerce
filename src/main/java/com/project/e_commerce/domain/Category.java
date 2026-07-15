package com.project.e_commerce.domain;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Category {
    @Setter(AccessLevel.NONE)
    private int categoryId;
    @NonNull
    private String categoryName;
    @NonNull
    private String categoryDescription;
    @NonNull
    private List<Product> products;

    public static boolean validateName(String categoryName){
        if(categoryName == null) return false;
        if(categoryName.isEmpty() || categoryName.isBlank()) return false;
        return categoryName.length() <= 100;
    }

    public static boolean validateDescription(String categoryDescription){
        if(categoryDescription == null) return false;
        return categoryDescription.length()<=500;
    }

    public static boolean validateProducts(List<Product> products){
        return products != null;
    }
}
