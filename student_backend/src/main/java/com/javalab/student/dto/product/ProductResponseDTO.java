package com.javalab.student.dto.product;


import com.javalab.student.entity.product.ProductImg;
import com.javalab.student.entity.product.Product;
import com.javalab.student.entity.product.ProductCategory;
import com.javalab.student.entity.product.ProductIngredient;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 클라이언트 응답용 상품 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Boolean active;
    private String mainImageUrl;
    private List<String> categories;  // 카테고리 이름 리스트
    private List<String> ingredients; // 영양 성분 리스트
    private String description;
    private List<ProductImgDto> productImgList;

    /** Product 엔티티를 DTO로 변환 */
    public static ProductResponseDTO fromEntity(Product product) {
        ProductResponseDTO dto = ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .active(product.isActive())
                .categories(product.getCategories() != null
                        ? product.getCategories().stream()
                        .map(ProductCategory::getName)
                        .collect(Collectors.toList())
                        : List.of())  // Null 처리
                .ingredients(product.getIngredients() != null
                        ? product.getIngredients().stream()
                        .map(ProductIngredient::getIngredientName)
                        .collect(Collectors.toList())
                        : List.of())  // Null 처리
                .description(product.getDescription())
                .build();
        // Product 엔티티에서 대표 이미지 URL 설정 (product_img 테이블에서 조회)
        if (product.getProductImgList() != null && !product.getProductImgList().isEmpty()) {
            dto.setMainImageUrl(product.getProductImgList().stream()
                    .filter(img -> "대표".equals(img.getImageType())) // 이미지 유형이 "대표"인 이미지 필터링
                    .findFirst() // 첫 번째 대표 이미지 가져오기
                    .map(ProductImg::getImageUrl) // ProductImg 엔티티에서 이미지 URL 추출
                    .orElse(null)); // 대표 이미지가 없을 경우 null 설정
        }

        // Product 엔티티에서 ProductImg 리스트를 ProductImgDto 리스트로 변환하여 설정 (✅ 추가)
        if (product.getProductImgList() != null && !product.getProductImgList().isEmpty()) {
            dto.setProductImgList(product.getProductImgList().stream()
                    .map(ProductImgDto::fromEntity) // ProductImg 엔티티를 ProductImgDto 로 변환
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
