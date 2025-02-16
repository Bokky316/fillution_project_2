package com.javalab.student.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 영양제 상품 정보를 나타내는 엔티티 클래스입니다.
 * 이 클래스는 상품의 기본 정보, 가격, 재고, 상태 등을 포함합니다.
 * 또한 상품과 관련된 영양 성분 및 카테고리와의 관계를 정의합니다.
 */
@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    /** 상품의 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 상품명 */
    @Column(nullable = false, length = 255)
    private String name;

    /** 상품 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 상품 가격 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 재고 수량 */
    @Column(nullable = false)
    private int stock;

    /** 상품 활성화 상태 (1: 활성, 0: 비활성) */
    @Column(nullable = false)
    private boolean active;

    /** 상품 이미지 */
    /**
     * 대표 이미지를 보여줄 때는, Product 엔티티의 productImgList 에서
     * imageType이 '대표'인 ProductImg 엔티티를 찾아서 그 imageUrl을 사용합니다.
     * (mainImageUrl이 필드는 삭제해야함)(아마도)
     */
    @Column(name = "main_image_url")
    private String mainImageUrl = "";

    /** 상품 이미지 목록 */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Builder pattern 사용 시 기본값 설정
    private List<ProductImg> productImgList = new ArrayList<>(); // ArrayList 로 초기화

    /**
     * 상품에 포함된 영양 성분 목록
     * 다대다 관계로, product_ingredient_mapping 테이블을 통해 매핑됩니다.
     */
    @ManyToMany
    @JoinTable(
            name = "product_ingredient_mapping",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "id")
    )
    @JsonIgnore // 순환 참조 방지
    private List<ProductIngredient> ingredients;

    /**
     * 상품이 속한 카테고리 목록
     * 다대다 관계로, product_category_mapping 테이블을 통해 매핑됩니다.
     */
    @ManyToMany
    @JoinTable(
            name = "product_category_mapping",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @JsonIgnore // 순환 참조 방지
    private List<ProductCategory> categories;

    /**
     * 추천 시스템을 위한 점수
     * 이 필드는 데이터베이스에 저장되지 않고 런타임에만 사용됩니다.
     */
    @Transient
    private int score;

    /**
     * 상품명을 반환합니다.
     * @return 상품명
     */
    public String getProductName() {
        return this.name;
    }

    /**
     * 상품 이미지 URL을 반환합니다.
     * @return 상품 이미지 URL
     */
    public String getImageUrl() {
        return this.mainImageUrl;
    }
}

