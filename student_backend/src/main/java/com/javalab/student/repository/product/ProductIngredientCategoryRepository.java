package com.javalab.student.repository.product;

import com.javalab.student.entity.product.ProductCategory;
import com.javalab.student.entity.product.ProductIngredientCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductIngredientCategoryRepository extends JpaRepository<ProductIngredientCategoryMapping, Long> {

    // ✅ 영양성분 ID 하나를 받아 해당하는 카테고리 목록 조회
    @Query("SELECT c FROM ProductIngredientCategoryMapping m JOIN m.category c WHERE m.ingredient.id = :ingredientId")
    List<ProductCategory> findCategoriesByIngredientId(@Param("ingredientId") Long ingredientId);

    // ✅ 여러 개의 영양성분 ID를 받아 해당하는 모든 카테고리 목록 조회
    @Query("SELECT DISTINCT c FROM ProductIngredientCategoryMapping m JOIN m.category c WHERE m.ingredient.id IN :ingredientIds")
    List<ProductCategory> findCategoriesByIngredientIds(@Param("ingredientIds") List<Long> ingredientIds);
}
