package com.javalab.student.repository.product;

import com.javalab.student.entity.product.ProductIngredient;
import com.javalab.student.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Long> {

    /**
     * 특정 제품과 관련된 모든 ProductIngredient를 찾습니다.
     * @param product 찾고자 하는 제품
     * @return 제품과 관련된 ProductIngredient 목록
     */
    @Query("SELECT pi FROM ProductIngredient pi JOIN pi.products p WHERE p = :product")
    List<ProductIngredient> findByProduct(@Param("product") Product product);

    /**
     * 특정 성분 이름을 가진 모든 ProductIngredient를 찾습니다.
     * @param ingredientName 찾고자 하는 성분 이름
     * @return 해당 성분 이름을 가진 ProductIngredient 목록
     */
    List<ProductIngredient> findByIngredientName(String ingredientName);
}
