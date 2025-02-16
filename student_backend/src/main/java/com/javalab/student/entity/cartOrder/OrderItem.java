package com.javalab.student.entity.cartOrder;

import com.javalab.student.dto.cartOrder.OrderItemDto;
import com.javalab.student.entity.BaseEntity;
import com.javalab.student.entity.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * 각 주문에 포함된 상품 정보를 담는 엔티티 클래스.
 * 주문 아이템과 관련된 상품, 가격, 수량 등을 관리합니다.
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem extends BaseEntity {

    /** 주문 아이템 ID, Primary Key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    /** 상품, ManyToOne 관계 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    /** 주문, ManyToOne 관계 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    /** 주문 가격 */
    @NotNull
    @Column(name = "order_price", nullable = false, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
    private BigDecimal orderPrice;

    /** 주문 수량 */
    private Integer count;

    /**
     * OrderItem 생성 메소드
     *
     * @param product 상품
     * @param count   수량
     * @return 생성된 OrderItem 객체
     */
    public static OrderItem createOrderItem(Product product, int count) {
        OrderItem orderItem = OrderItem.builder()
                .product(product)    // 자신의 product 변수에 상위객체(Product) 주소 할당. 상위객체와 매핑
                .count(count)
                .orderPrice(product.getPrice())
                .build();
        product.setStock(product.getStock() - count);
        return orderItem;
    }

    /**
     * 주문 금액 계산 메소드 (수량 * 가격)
     *
     * @return 주문 금액
     */
    public BigDecimal getTotalPrice() {
        return orderPrice.multiply(BigDecimal.valueOf(count));
    }

    /**
     * 주문 취소 메소드 (재고 증가)
     */
    public void cancel() {
        this.getProduct().setStock(this.getProduct().getStock() + count);
    }

    /**
     * Entity -> Dto 변환
     *
     * @return OrderItemDto
     */
    public OrderItemDto entityToDto() {
        OrderItemDto orderItemDto = OrderItemDto.builder()
                .orderItemId(this.getId())
                .productId(this.getProduct().getId())  // itemId를 productId로 변경
                .productName(this.getProduct().getName())  // itemNm을 productName으로 변경
                .count(this.getCount())
                .orderPrice(this.getOrderPrice())
                .build();
        return orderItemDto;
    }
}
