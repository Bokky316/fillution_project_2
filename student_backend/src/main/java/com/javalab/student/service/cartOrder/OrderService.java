package com.javalab.student.service.cartOrder;

import com.javalab.student.dto.cartOrder.OrderDto;
import com.javalab.student.dto.cartOrder.OrderHistDto;
import com.javalab.student.dto.cartOrder.OrderItemDto;
import com.javalab.student.entity.Member;
import com.javalab.student.entity.product.Product;
import com.javalab.student.entity.cartOrder.Order;
import com.javalab.student.entity.cartOrder.OrderItem;
import com.javalab.student.repository.MemberRepository;
import com.javalab.student.repository.cartOrder.OrderRepository;
import com.javalab.student.repository.product.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    /**
     * 주문을 생성합니다.
     *
     * @param orderDto 주문 정보
     * @param email 주문자 이메일
     * @return 생성된 주문의 ID
     */
    public Long order(OrderDto orderDto, String email) {
        Product product = productRepository.findById(orderDto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
        Member member = memberRepository.findByEmail(email);

        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem = OrderItem.createOrderItem(product, orderDto.getCount());
        orderItemList.add(orderItem);

        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 사용자의 주문 목록을 조회합니다.
     *
     * @param email 사용자 이메일
     * @param pageable 페이징 정보
     * @return 주문 내역 페이지
     */
    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {
        List<Order> orders = orderRepository.findOrders(email, pageable);
        Long totalCount = orderRepository.countOrder(email);

        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        for (Order order : orders) {
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, "");
                orderHistDto.addOrderItemDto(orderItemDto);
            }
            orderHistDtos.add(orderHistDto);
        }

        return new PageImpl<>(orderHistDtos, pageable, totalCount);
    }

    /**
     * 주문의 소유자를 확인합니다.
     *
     * @param orderId 주문 ID
     * @param email 사용자 이메일
     * @return 소유자 일치 여부
     */
    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String email) {
        Member curMember = memberRepository.findByEmail(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));
        Member savedMember = order.getMember();
        return curMember.getEmail().equals(savedMember.getEmail());
    }

    /**
     * 주문을 취소합니다.
     *
     * @param orderId 취소할 주문 ID
     */
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));
        order.cancelOrder();
    }

    /**
     * 여러 상품을 한 번에 주문합니다.
     *
     * @param orderDtoList 주문할 상품 목록
     * @param email 주문자 이메일
     * @return 생성된 주문의 ID
     */
    public Long orders(List<OrderDto> orderDtoList, String email) {
        Member member = memberRepository.findByEmail(email);
        List<OrderItem> orderItemList = new ArrayList<>();

        for (OrderDto orderDto : orderDtoList) {
            Product product = productRepository.findById(orderDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

            OrderItem orderItem = OrderItem.createOrderItem(product, orderDto.getCount());
            orderItemList.add(orderItem);
        }

        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();
    }
}
