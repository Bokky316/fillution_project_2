package com.javalab.student.entity.product;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * 영양제 성분 정보를 나타내는 엔티티 클래스입니다.
 * 이 클래스는 영양 성분의 기본 정보와 해당 성분을 포함하는 상품들과의 관계를 정의합니다.
 */
@Entity
@Table(name = "product_ingredient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductIngredient {

    /** 영양 성분의 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 영양 성분 이름 (중복 불가) */
    @Column(name = "ingredient_name", nullable = false, unique = true)
    private String ingredientName;

    /**
     * 이 영양 성분을 포함하는 상품 목록
     * Product 엔티티의 ingredients 필드와 양방향 관계를 형성합니다.
     */
    @ManyToMany(mappedBy = "ingredients")
    private List<Product> products;
}

