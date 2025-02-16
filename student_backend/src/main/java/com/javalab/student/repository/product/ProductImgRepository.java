package com.javalab.student.repository.product;

import com.javalab.student.entity.product.ProductImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImgRepository extends JpaRepository<ProductImg, Long> {
    List<ProductImg> findByProductId(Long productId);
    ProductImg findFirstByProductIdAndImageTypeOrderByOrderAsc(Long productId, String imageType); // 대표 이미지 조회

    // ✅ 상세 이미지 조회 시 order 순으로 정렬하는 메소드 추가
    List<ProductImg> findByProductIdAndImageTypeOrderByOrderAsc(Long productId, String imageType);
}