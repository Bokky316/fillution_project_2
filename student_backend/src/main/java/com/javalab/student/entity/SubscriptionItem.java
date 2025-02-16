package com.javalab.student.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.javalab.student.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

/**
 * SubscriptionItem (구독 상품) 엔티티
 * - 현재 구독에 포함된 상품 목록 저장
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"subscription"}) // 순환참조 방지
public class SubscriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    @JsonIgnoreProperties({"items"})
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"subscriptionItems"})
    private Product product;

    private int quantity;
    private double price;
}
