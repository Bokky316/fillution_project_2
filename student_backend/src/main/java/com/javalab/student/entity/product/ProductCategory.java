package com.javalab.student.entity.product;

import jakarta.persistence.*;
import lombok.*;

/**
 * 상품 카테고리 정보를 나타내는 엔티티 클래스입니다.
 * 이 클래스는 카테고리의 기본 정보를 포함합니다.
 */
@Entity
@Table(name = "product_category")
@Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategory {

    /** 카테고리의 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카테고리 이름 (중복 불가) */
    @Column(nullable = false, unique = true)
    private String name;
}
