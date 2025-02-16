package com.javalab.student.service.product;

import com.javalab.student.dto.product.ProductDto;
import com.javalab.student.dto.product.ProductFormDto;
import com.javalab.student.dto.product.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {

    /** 상품 생성 */
    ProductDto createProduct(ProductFormDto productFormDto);

    /** 상품 정보 수정 */
    ProductDto updateProduct(Long id, ProductFormDto productFormDto);

    /** 상품 단건 조회 */
    ProductResponseDTO getProductById(Long id);

    /** 상품 활성화/비활성화 */
    void toggleProductActive(Long id);

    /** 전체 상품 목록 조회 */
    List<ProductResponseDTO> getProductList();

    /** 영양 성분과 카테고리 기준으로 정렬된 상품 조회 */
    List<ProductResponseDTO> getProductsSortedByIngredientAndCategory(Long ingredientId);

    List<ProductResponseDTO> getProductsByCategory(Long categoryId);

    /** 검색 메서드 추가 */
    Page<ProductResponseDTO> searchProducts(String field, String query, Pageable pageable);

    /** 상품 이미지 삭제 */
    void deleteProductImage(Long productId, String imageType, Integer imageIndex);
}
