package com.javalab.student.service.cartOrder;

import com.javalab.student.constant.OrderStatus;
import com.javalab.student.dto.cartOrder.OrderDto;
import com.javalab.student.dto.cartOrder.PaymentRequestDto;
import com.javalab.student.entity.Member;
import com.javalab.student.entity.cartOrder.*;
import com.javalab.student.entity.product.Product;
import com.javalab.student.repository.MemberRepository;
import com.javalab.student.repository.cartOrder.CartItemRepository;
import com.javalab.student.repository.cartOrder.CartRepository;
import com.javalab.student.repository.cartOrder.OrderRepository;
import com.javalab.student.repository.cartOrder.PaymentRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 결제 서비스 (포트원 SDK 적용)
 *
 * 포트원(Iamport) SDK를 사용하여 결제 처리 및 검증을 수행하는 서비스입니다.
 * 주문 생성, 결제 검증, 결제 정보 저장, 장바구니 비우기 등의 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final IamportClient iamportClient;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;

    /**
     * 결제를 처리하고 검증합니다.
     *
     * @param requestDto   결제 요청 정보 (PaymentRequestDto)
     * @param email        사용자 이메일
     * @param purchaseType 구매 유형 ('oneTime' 또는 'subscription')
     * @return 처리된 결제 정보 (Map<String, Object>)
     * @throws EntityNotFoundException 해당 이메일로 멤버를 찾을 수 없거나, 장바구니를 찾을 수 없을 경우 예외 발생
     */
    @Transactional
    public Map<String, Object> processPayment(PaymentRequestDto requestDto, String email, String purchaseType) {
        log.info("🔹 결제 검증 시작: {}", requestDto);

        // 1. 주문 생성 및 정보 조회
        Order order = createOrder(requestDto, email, purchaseType);

        // 2. 포트원 API를 사용하여 결제 정보 조회 및 검증
        verifyPayment(requestDto, order);

        // 3. Payment 엔티티 생성 및 저장
        com.javalab.student.entity.cartOrder.Payment payment = createAndSavePayment(requestDto, order);

        // 4. 주문 상태 업데이트
        order.setOrderStatus(OrderStatus.PAYMENT_COMPLETED);
        orderRepository.save(order);

        // 5. 장바구니 비우기
        clearCart(email);

        // 6. 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("paymentId", payment.getId());
        response.put("impUid", payment.getImpUid());
        response.put("merchantUid", payment.getOrder().getId());
        response.put("amount", payment.getAmount());
        response.put("paymentMethod", payment.getPaymentMethod());
        response.put("status", payment.getOrderStatus());
        response.put("paidAt", payment.getPaidAt());
        return response;
    }

    /**
     * 장바구니 상품들을 주문으로 변환하고 처리합니다.
     *
     * @param requestDto 결제 요청 정보 (PaymentRequestDto)
     * @param email 사용자 이메일
     * @param purchaseType 구매 유형 ('oneTime' 또는 'subscription')
     * @return 주문 객체 (Order)
     * @throws EntityNotFoundException 해당 이메일로 멤버를 찾을 수 없거나, 장바구니를 찾을 수 없을 경우 예외 발생
     */
    @Transactional
    public Order createOrder(PaymentRequestDto requestDto, String email, String purchaseType) {
        // 1. 사용자 정보 조회
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("Member not found with email: " + email);
        }

        // 2. 사용자 장바구니 조회
        Cart cart = cartRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for member id: " + member.getId()));

        // 3. 장바구니 아이템 조회
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        BigDecimal totalOrderAmount = BigDecimal.ZERO;

        // 4. 주문 DTO 생성 및 총 주문 금액 계산
        List<OrderDto.OrderItemDto> orderItemDtoList = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            PaymentRequestDto.CartOrderItemDto cartOrderItemDto = requestDto.getCartOrderItems().stream()
                    .filter(itemDto -> itemDto.getCartItemId().equals(cartItem.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("CartItemDto not found for cartItemId: " + cartItem.getId()));
            OrderDto.OrderItemDto orderItemDto = OrderDto.OrderItemDto.builder()
                    .productId(cartItem.getProduct().getId())
                    .productName(cartItem.getProduct().getName())
                    .count(cartItem.getQuantity())
                    .orderPrice(cartItem.getProduct().getPrice())
                    .build();
            orderItemDtoList.add(orderItemDto);

            Product product = cartItem.getProduct();
            if (product == null) {
                log.error("Product is null for cartItem: {}", cartItem);
                throw new IllegalStateException("Product cannot be null for cart item id: " + cartItem.getId());
            }

            BigDecimal productPrice = cartOrderItemDto.getPrice();
            if (productPrice == null) {
                log.error("Product price is null for product: {}", product);
                throw new IllegalStateException("Product price cannot be null for product id: " + product.getId());
            }
            totalOrderAmount = totalOrderAmount.add(productPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // 5. 주문 객체 생성
        Order order = Order.builder()
                .member(member)
                .orderDate(java.time.LocalDateTime.now())
                .orderStatus(OrderStatus.ORDERED)
                .amount(totalOrderAmount)
                .build();

        // 6. OrderItem 생성 및 Order에 추가
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            PaymentRequestDto.CartOrderItemDto cartOrderItemDto = requestDto.getCartOrderItems().stream()
                    .filter(itemDto -> itemDto.getCartItemId().equals(cartItem.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("CartItemDto not found for cartItemId: " + cartItem.getId()));
            Product product = cartItem.getProduct();
            if (product == null) {
                log.error("Product is null for cartItem: {}", cartItem);
                throw new IllegalStateException("Product cannot be null for cart item id: " + cartItem.getId());
            }

            BigDecimal productPrice = cartOrderItemDto.getPrice();
            if (productPrice == null) {
                log.error("Product price is null for product: {}", product);
                throw new IllegalStateException("Product price cannot be null for product id: " + product.getId());
            }
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .orderPrice(productPrice)
                    .count(cartItem.getQuantity())
                    .build();
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);

        return orderRepository.save(order);
    }

    /**
     * 포트원 API를 사용하여 결제 정보를 조회하고 검증합니다.
     *
     * @param requestDto 결제 요청 정보 (PaymentRequestDto)
     * @param order 주문 정보 (Order)
     * @throws IllegalArgumentException 포트원 결제 검증 실패, 결제 정보 없음, 결제 금액 불일치 시 예외 발생
     */
    private void verifyPayment(PaymentRequestDto requestDto, Order order) {
        IamportResponse<com.siot.IamportRestClient.response.Payment> paymentResponse;
        try {
            paymentResponse = iamportClient.paymentByImpUid(requestDto.getImpUid());
        } catch (IamportResponseException | IOException e) {
            throw new IllegalArgumentException("❌ 포트원 결제 검증 실패: " + e.getMessage());
        }

        com.siot.IamportRestClient.response.Payment paymentInfo = paymentResponse.getResponse();
        if (paymentInfo == null) {
            throw new IllegalArgumentException("❌ 결제 정보 없음: imp_uid=" + requestDto.getImpUid());
        }

        BigDecimal paidAmount = paymentInfo.getAmount();
        if (paidAmount.compareTo(requestDto.getPaidAmount()) != 0) {
            throw new IllegalArgumentException("❌ 결제 금액 불일치: 요청 금액=" + requestDto.getPaidAmount() + ", 실제 결제 금액=" + paidAmount);
        }
    }

    /**
     * 결제 정보를 저장합니다.
     *
     * @param requestDto 결제 요청 정보 (PaymentRequestDto)
     * @param order 주문 정보 (Order)
     * @return Payment 결제 정보 (Payment)
     */
    private com.javalab.student.entity.cartOrder.Payment createAndSavePayment(PaymentRequestDto requestDto, Order order) {
        com.javalab.student.entity.cartOrder.Payment payment = com.javalab.student.entity.cartOrder.Payment.builder()
                .order(order)
                .impUid(requestDto.getImpUid())
                .itemNm(requestDto.getName())
                .orderStatus(OrderStatus.PAYMENT_COMPLETED)
                .amount(requestDto.getPaidAmount())
                .paymentMethod(requestDto.getPayMethod())
                .buyerEmail(requestDto.getBuyerEmail())
                .buyerName(requestDto.getBuyerName())
                .buyerTel(requestDto.getBuyerTel())
                .buyerAddr(requestDto.getBuyerAddr())
                .buyerPostcode(requestDto.getBuyerPostcode())
                .paidAt(requestDto.getPaidAt())
                .build();
        return paymentRepository.save(payment);
    }

    /**
     * 장바구니를 비웁니다.
     *
     * @param email 사용자 이메일
     * @throws EntityNotFoundException 해당 이메일로 멤버를 찾을 수 없거나, 장바구니를 찾을 수 없을 경우 예외 발생
     */
    private void clearCart(String email) {
        // 1. 사용자 정보 조회
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("해당 이메일로 멤버를 찾을 수 없습니다: " + email);
        }

        // 2. 사용자 장바구니 조회
        Cart cart = cartRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for member id: " + member.getId()));

        // 3. 장바구니 아이템 조회
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        // 4. 장바구니 아이템 삭제
        cartItemRepository.deleteAll(cartItems);
    }
}
