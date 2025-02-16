package com.javalab.student.dto.product;

import lombok.*;

import java.math.BigDecimal;

/**
 * 상품 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private boolean active;
    private String mainImageUrl;

}

