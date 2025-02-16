package com.javalab.student.entity.product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_ingredient_category_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductIngredientCategoryMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private ProductIngredient ingredient;  // 영양 성분

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;  // 연결된 카테고리
}
