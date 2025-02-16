package com.javalab.student.repository.product;

import com.javalab.student.entity.product.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    Optional<ProductCategory> findByName(String name);  // 카테고리명으로 조회하는 메서드 추가

    // ✅ ID 기준 오름차순 정렬된 카테고리 조회 추가
    List<ProductCategory> findAllByOrderByIdAsc();
}
