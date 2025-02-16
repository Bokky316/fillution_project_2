package com.javalab.student.entity.cartOrder;

import com.javalab.student.entity.BaseEntity;
import com.javalab.student.entity.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 장바구니에 담긴 상품 정보를 담는 엔티티 클래스.
 * 장바구니 아이템과 관련된 상품, 수량 등을 관리합니다.
 */
@Entity
@Getter
@Setter
@Table(name = "cart_item")
public class CartItem extends BaseEntity {

    /** 장바구니 아이템 ID, Primary Key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    /** 장바구니, ManyToOne 관계 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    /** 상품, ManyToOne 관계 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    /** 수량 */
    private int quantity;

    /**
     * 장바구니 아이템 생성 메소드
     *
     * @param cart    장바구니
     * @param product 상품
     * @param quantity 수량
     * @return 생성된 CartItem 객체
     */
    public static CartItem createCartItem(Cart cart, Product product, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    /**
     * 수량 증가 메소드
     *
     * @param quantity 증가할 수량
     */
    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    /**
     * 수량 업데이트 메소드
     *
     * @param quantity 업데이트할 수량
     */
    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}
