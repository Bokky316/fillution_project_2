package com.javalab.student.dto.product;

import com.javalab.student.entity.product.ProductImg;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ProductImg DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImgDto {
    private Long id;
    private String imageUrl;
    private String imageType;
    private Integer order;
    private LocalDateTime createdAt;

    public static ProductImgDto fromEntity(ProductImg productImg) {
        return ProductImgDto.builder()
                .id(productImg.getId())
                .imageUrl(productImg.getImageUrl())
                .imageType(productImg.getImageType())
                .order(productImg.getOrder())
                .createdAt(productImg.getCreatedAt())
                .build();
    }
}