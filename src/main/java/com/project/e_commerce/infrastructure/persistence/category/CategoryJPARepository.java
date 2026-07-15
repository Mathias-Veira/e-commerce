package com.project.e_commerce.infrastructure.persistence.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CategoryJPARepository extends JpaRepository<CategoryEntity,Integer> {
    @Query("SELECT C FROM CategoryEntity C WHERE C.categoryName = :category_name")
    Optional<CategoryEntity> findCategoryByName(@Param("category_name") String categoryName);
}
