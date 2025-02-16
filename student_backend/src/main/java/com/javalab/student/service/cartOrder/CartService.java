package com.javalab.student.service.cartOrder;

import com.javalab.student.dto.cartOrder.CartItemDto;
import com.javalab.student.dto.cartOrder.CartDetailDto;
import com.javalab.student.dto.cartOrder.CartOrderRequestDto;
import com.javalab.student.dto.cartOrder.OrderDto;
import com.javalab.student.entity.Member;
import com.javalab.student.entity.product.Product;
import com.javalab.student.entity.cartOrder.Cart;
import com.javalab.student.entity.cartOrder.CartItem;
import com.javalab.student.repository.cartOrder.CartItemRepository;
import com.javalab.student.repository.cartOrder.CartRepository;
import com.javalab.student.repository.MemberRepository;
import com.javalab.student.repository.product.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 장바구니 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    /**
     * 장바구니에 상품을 추가합니다.
     *
     * @param cartItemDto 장바구니에 추가할 상품 정보
     * @param email 사용자 이메일
     * @return 추가된 장바구니 아이템의 ID
     */
    @Transactional
    public Long addCart(CartItemDto cartItemDto, String email) {
        try {
            Product product = productRepository.findById(cartItemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + cartItemDto.getProductId()));
            Member member = memberRepository.findByEmail(email);
            if (member == null) {
                throw new EntityNotFoundException("회원을 찾을 수 없습니다. Email: " + email);
            }

            Cart cart = cartRepository.findByMemberId(member.getId())
                    .orElseGet(() -> {
                        Cart newCart = Cart.createCart(member);
                        return cartRepository.save(newCart);
                    });

            CartItem savedCartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

            if (savedCartItem != null) {
                savedCartItem.addQuantity(cartItemDto.getQuantity());
                return savedCartItem.getId();
            } else {
                CartItem cartItem = CartItem.createCartItem(cart, product, cartItemDto.getQuantity());
                cartItemRepository.save(cartItem);
                return cartItem.getId();
            }
        } catch (Exception e) {
            log.error("장바구니 추가 중 오류 발생", e);
            throw new RuntimeException("장바구니 추가 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자의 장바구니 목록을 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 장바구니 상세 정보 목록
     */
    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email) {
        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId()).orElse(null);
        if (cart == null) {
            return new ArrayList<>();
        }
        return cartItemRepository.findCartDetailDtoList(cart.getId());
    }

    /**
     * 장바구니 아이템의 소유자를 확인합니다.
     *
     * @param cartItemId 장바구니 아이템 ID
     * @param email 사용자 이메일
     * @return 소유자 일치 여부
     */
    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email) {
        Member curMember = memberRepository.findByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("장바구니 아이템을 찾을 수 없습니다."));
        Member savedMember = cartItem.getCart().getMember();
        return curMember.getEmail().equals(savedMember.getEmail());
    }

    /**
     * 장바구니 아이템의 수량을 업데이트합니다.
     *
     * @param cartItemId 장바구니 아이템 ID
     * @param count 변경할 수량
     */
    @Transactional
    public void updateCartItemCount(Long cartItemId, int count) {
        try {
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new EntityNotFoundException("장바구니 아이템을 찾을 수 없습니다."));
            cartItem.updateQuantity(count);
            cartItemRepository.save(cartItem);  // 명시적 저장
            log.info("장바구니 아이템 수량 업데이트 성공 - ID: {}, 새 수량: {}", cartItemId, count);
        } catch (Exception e) {
            log.error("장바구니 아이템 수량 업데이트 실패 - ID: {}, 요청 수량: {}", cartItemId, count, e);
            throw new RuntimeException("장바구니 아이템 수량 업데이트에 실패했습니다.", e);
        }
    }

    /**
     * 장바구니 아이템을 삭제합니다.
     *
     * @param cartItemId 삭제할 장바구니 아이템 ID
     */
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("장바구니 아이템을 찾을 수 없습니다."));
        cartItemRepository.delete(cartItem);
    }

    /**
     * 장바구니에서 선택한 상품들을 주문합니다.
     *
     * @param cartOrderRequestDto 장바구니 주문 요청 정보
     * @param email 사용자 이메일
     * @return 생성된 주문의 ID
     */
    public Long orderCartItem(CartOrderRequestDto cartOrderRequestDto, String email) {
        List<OrderDto> orderDtoList = new ArrayList<>();

        for (CartOrderRequestDto.CartOrderItem cartOrderItem : cartOrderRequestDto.getCartOrderItems()) {
            CartItem cartItem = cartItemRepository.findById(cartOrderItem.getCartItemId())
                    .orElseThrow(() -> new EntityNotFoundException("장바구니 아이템을 찾을 수 없습니다."));

            OrderDto orderDto = new OrderDto();
            orderDto.setProductId(cartItem.getProduct().getId());
            orderDto.setCount(cartItem.getQuantity());
            orderDtoList.add(orderDto);
        }

        Long orderId = orderService.orders(orderDtoList, email);

        for (CartOrderRequestDto.CartOrderItem cartOrderItem : cartOrderRequestDto.getCartOrderItems()) {
            CartItem cartItem = cartItemRepository.findById(cartOrderItem.getCartItemId())
                    .orElseThrow(() -> new EntityNotFoundException("장바구니 아이템을 찾을 수 없습니다."));
            cartItemRepository.delete(cartItem);
        }

        return orderId;
    }

    /**
     * 상품의 재고를 확인합니다.
     *
     * @param productId 상품 ID
     * @param quantity 확인할 수량
     * @return 재고 충분 여부
     */
    @Transactional(readOnly = true)
    public boolean checkStock(Long productId, int quantity) {
        log.debug("재고 확인 - 상품 ID: {}, 요청 수량: {}", productId, quantity);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("상품을 찾을 수 없음 - 상품 ID: {}", productId);
                    return new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + productId);
                });
        log.debug("상품 재고: {}", product.getStock());
        return product.getStock() >= quantity;
    }

    /**
     * 장바구니 아이템 ID로 상품 ID를 조회합니다.
     *
     * @param cartItemId 장바구니 아이템 ID
     * @return 상품 ID
     */
    @Transactional(readOnly = true)
    public Long getItemIdByCartItemId(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("장바구니 아이템을 찾을 수 없습니다."));
        return cartItem.getProduct().getId();
    }

    /**
     * 특정 장바구니 아이템의 상세 정보를 조회합니다.
     *
     * @param cartItemId 장바구니 아이템 ID
     * @return 장바구니 아이템 상세 정보
     * @throws EntityNotFoundException 장바구니 아이템을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public CartDetailDto getCartItemDetail(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("장바구니 아이템을 찾을 수 없습니다. ID: " + cartItemId));

        return CartDetailDto.of(cartItem);
    }

}
