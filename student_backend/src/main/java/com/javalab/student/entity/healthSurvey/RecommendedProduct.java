package com.javalab.student.entity.healthSurvey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.javalab.student.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recommended_product")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ElementCollection
    @CollectionTable(name = "recommended_product_ingredients", joinColumns = @JoinColumn(name = "recommended_product_id"))
    @Column(name = "ingredient_name")
    private List<String> relatedIngredients = new ArrayList<>();
}
