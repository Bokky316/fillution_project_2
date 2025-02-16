package com.javalab.student.repository.product;

import com.javalab.student.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 상품 데이터를 관리하는 Repository 인터페이스
 * JpaRepository<Product, Long>를 확장하여 기본적인 CRUD 기능을 제공하며,
 * 추가적인 쿼리 메서드를 통해 상품 검색 기능을 제공한다.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 특정 상품의 주성분(메인 영양 성분) 이름을 조회하는 메서드
     */
    @Query("SELECT pi.ingredientName FROM Product p JOIN p.ingredients pi WHERE p.id = :productId")
    String findMainIngredientByProductId(@Param("productId") Long productId);

    /**
     * 특정 영양 성분 이름을 포함하는 상품 ID 및 상품명을 조회하는 메서드
     */
    @Query("SELECT p.id, p.name FROM Product p JOIN p.ingredients i WHERE i.ingredientName = :ingredientName")
    List<Object[]> findByMainIngredientName(@Param("ingredientName") String ingredientName);

    /**
     * 여러 개의 영양 성분을 포함하는 상품 목록을 조회하는 메서드
     */
    @Query("SELECT DISTINCT p FROM Product p JOIN p.ingredients i WHERE i.ingredientName IN :ingredientNames")
    List<Product> findByIngredientsIngredientNameIn(@Param("ingredientNames") List<String> ingredientNames);

    /**
     * 특정 영양 성분을 포함하는 상품 목록을 조회하는 메서드
     */
    @Query("SELECT p FROM Product p JOIN p.ingredients i WHERE i.ingredientName = :ingredientName")
    List<Product> findByIngredientName(@Param("ingredientName") String ingredientName);

    /**
     * 영양 성분을 기준으로 카테고리별로 정렬된 상품 목록 조회
     */
    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.ingredients i " +
            "JOIN p.categories c " +
            "WHERE i.id = :ingredientId " +
            "ORDER BY c.name ASC")
    List<Product> findProductsByIngredientAndCategory(@Param("ingredientId") Long ingredientId);

    /**
     * 상품 ID로 상품 엔티티 조회 (Fetch Join - categories 포함)
     * - Product 엔티티와 연관된 categories 컬렉션을 함께 로딩 (Eager Loading 효과)
     */
    @Query("SELECT p FROM Product p JOIN FETCH p.categories WHERE p.id = :id") // ✅ Fetch Join 쿼리
    Optional<Product> findProductByIdWithCategories(@Param("id") Long id);

    List<Product> findByCategories_Id(Long categoryId);

    // 추가된 메서드 (페이징 O) - @Query 어노테이션 추가
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id = :categoryId")
    Page<Product> findByCategories_Id(@Param("categoryId") Long categoryId, Pageable pageable);

    // 검색을 위한 메서드 추가
    Page<Product> findByNameContaining(String name, Pageable pageable);
    Page<Product> findByCategories_NameContaining(String categoryName, Pageable pageable);
    Page<Product> findByIngredients_IngredientNameContaining(String ingredientName, Pageable pageable);

}
