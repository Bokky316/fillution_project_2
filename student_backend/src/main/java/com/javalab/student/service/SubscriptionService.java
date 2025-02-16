package com.javalab.student.service;

import com.javalab.student.dto.SubscriptionResponseDto;
import com.javalab.student.dto.SubscriptionUpdateNextItemDto;
import com.javalab.student.entity.product.Product;
import com.javalab.student.entity.Subscription;
import com.javalab.student.entity.SubscriptionItem;
import com.javalab.student.entity.SubscriptionNextItem;
import com.javalab.student.repository.product.ProductRepository;
import com.javalab.student.repository.SubscriptionItemRepository;
import com.javalab.student.repository.SubscriptionNextItemRepository;
import com.javalab.student.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 구독 관련 서비스 클래스
 * - 구독 정보 조회, 생성, 수정, 취소 등의 비즈니스 로직을 처리
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionItemRepository subscriptionItemRepository;
    private final SubscriptionNextItemRepository subscriptionNextItemRepository;
    private final ProductRepository productRepository;
    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    /**
     * 사용자의 최신 구독 정보 조회
     */
    /**
     * 사용자의 최신 활성화된 구독 정보 조회
     */
    @Transactional(readOnly = true)
    public SubscriptionResponseDto getSubscription(Long memberId) {
        Subscription subscription = subscriptionRepository
                .findFirstByMemberIdAndStatusOrderByCurrentCycleDesc(memberId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("활성화된 구독 정보가 없습니다."));

        // ✅ nextItems에서 productId가 없는 경우 product에서 가져오기
        subscription.getNextItems().forEach(item -> {
            if (item.getProductId() == null && item.getProduct() != null) {
                item.setProductId(item.getProduct().getId()); // ✅ productId 추가
            }
        });

        return new SubscriptionResponseDto(subscription);
    }








    /**
     * 새로운 구독 생성
     */
    @Transactional
    public Subscription createSubscription(Long memberId, String paymentMethod, String postalCode, String roadAddress, String deliverydetailAddressddress) {
        Optional<Subscription> latestActiveSubscription = subscriptionRepository
                .findFirstByMemberIdAndStatusOrderByCurrentCycleDesc(memberId, "active");

        if (latestActiveSubscription.isPresent()) {
            throw new RuntimeException("이미 활성화된 구독이 있습니다.");
        }

        Subscription subscription = Subscription.builder()
                .startDate(LocalDate.now())
                .lastBillingDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .status("active")
                .paymentMethod(paymentMethod)
                .postalCode(postalCode)
                .roadAddress(roadAddress)
                .detailAddress(deliverydetailAddressddress)
                .currentCycle(1)
                .build();

        return subscriptionRepository.save(subscription);
    }

    /**
     * 결제일 업데이트
     */
    @Transactional
    public boolean updateBillingDate(Long subscriptionId, LocalDate newBillingDate) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("구독 정보를 찾을 수 없습니다."));

        // ✅ 기존 결제일 확인
        LocalDate lastBillingDate = subscription.getLastBillingDate();

        // ✅ 다음 결제일 (lastBillingDate 기준으로 한 달 후) 계산
        LocalDate nextBillingDate = lastBillingDate.plusMonths(1);

        // ✅ 결제일 변경 가능 여부 확인 (nextBillingDate 기준으로 -15일 ~ +15일)
        LocalDate minDate = nextBillingDate.minusDays(15);
        LocalDate maxDate = nextBillingDate.plusDays(15);

        if (newBillingDate.isBefore(minDate) || newBillingDate.isAfter(maxDate)) {
            throw new RuntimeException("❌ 변경 가능한 날짜 범위를 초과했습니다!");
        }

        // ✅ 결제일 업데이트
        subscription.setNextBillingDate(newBillingDate);
        subscriptionRepository.save(subscription);
        return true;
    }


    /**
     * 결제수단 변경
     */
    @Transactional
    public boolean updateNextPaymentMethod(Long subscriptionId, String nextPaymentMethod) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("구독 정보를 찾을 수 없습니다."));

        subscription.setNextPaymentMethod(nextPaymentMethod);
        subscriptionRepository.save(subscription);
        return true;
    }


    /**
     * 배송정보 변경
     */
    @Transactional
    public boolean updateDeliveryAddress(Long subscriptionId, String postalCode, String roadAddress, String detailAddress) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("❌ 구독 정보를 찾을 수 없습니다."));

        // ✅ 새로운 주소값을 업데이트
        subscription.setPostalCode(postalCode);
        subscription.setRoadAddress(roadAddress);
        subscription.setDetailAddress(detailAddress);

        subscriptionRepository.save(subscription);
        return true;
    }


    /**
     * 구독 취소
     */
    @Transactional
    public boolean cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("구독 정보를 찾을 수 없습니다."));

        if (!subscription.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("진행 중인 구독만 취소할 수 있습니다.");
        }

        subscription.setStatus("CANCELLED"); // ✅ 상태 변경
        subscription.setEndDate(LocalDate.now()); // ✅ 현재 날짜를 종료일로 설정
        subscriptionRepository.save(subscription);
        return true;
    }

    /**
     * 구독 갱신 시 과거 구독 expired 처리
     * @param memberId
     */
    @Transactional
    public void expirePastSubscriptions(Long memberId) {
        List<Subscription> pastSubscriptions = subscriptionRepository.findByMemberIdAndStatus(memberId, "ACTIVE");

        for (Subscription sub : pastSubscriptions) {
            sub.setStatus("EXPIRED");
            subscriptionRepository.save(sub);
        }
    }

    /**
     * 구독 정보 업데이트 (결제일, 결제수단, 배송정보)
     */
    @Transactional
    public void updateSubscriptionInfo(Long subscriptionId, LocalDate newBillingDate, String newPaymentMethod,
                                       String postalCode, String roadAddress, String detailAddress) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("구독 정보를 찾을 수 없습니다."));

        if (newBillingDate != null) {
            subscription.setNextBillingDate(newBillingDate);
        }
        if (newPaymentMethod != null) {
            subscription.setNextPaymentMethod(newPaymentMethod);
        }
        if (postalCode != null) {
            subscription.setPostalCode(postalCode);
        }
        if (roadAddress != null) {
            subscription.setRoadAddress(roadAddress);
        }
        if (detailAddress != null) {
            subscription.setDetailAddress(detailAddress);
        }

        subscriptionRepository.save(subscription);
    }

    /**
     * 다음 회차에 반영할 상품 추가/삭제
     * - 사용자가 직접 다음 회차에 반영될 상품을 관리할 수 있도록 지원
     * - 기존 SubscriptionNextItem을 삭제하고 새롭게 저장
     */
    @Transactional
    public boolean updateNextSubscriptionItems(Long subscriptionId, List<SubscriptionUpdateNextItemDto> updatedItems) {
        try {
            for (SubscriptionUpdateNextItemDto item : updatedItems) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("해당 productId의 제품을 찾을 수 없습니다: " + item.getProductId()));

                Optional<SubscriptionNextItem> existingItem = subscriptionNextItemRepository.findBySubscriptionIdAndProduct(subscriptionId, product);

                if (existingItem.isPresent()) {
                    SubscriptionNextItem nextItem = existingItem.get();
                    nextItem.setNextMonthQuantity(item.getNextMonthQuantity());
                    nextItem.setNextMonthPrice(item.getNextMonthPrice());
                    subscriptionNextItemRepository.save(nextItem);
                } else {
                    SubscriptionNextItem newItem = SubscriptionNextItem.builder()
                            .subscription(subscriptionRepository.findById(subscriptionId)
                                    .orElseThrow(() -> new RuntimeException("해당 구독 ID를 찾을 수 없습니다: " + subscriptionId)))
                            .product(product)
                            .productId(product.getId())  // ✅ productId 명시적으로 설정
                            .nextMonthQuantity(item.getNextMonthQuantity())
                            .nextMonthPrice(item.getNextMonthPrice())
                            .build();
                    subscriptionNextItemRepository.save(newItem);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("❌ [ERROR] 구독 상품 업데이트 실패", e);
            return false;
        }
    }













    /**
     * 담달 정기결제 상품 추가
     * - 이미 있는 상품이면 수량만 증가하도록 추가 구현 필요
     * @param subscriptionId
     * @param newItemDto
     */
    @Transactional
    public SubscriptionNextItem addNextSubscriptionItem(Long subscriptionId, SubscriptionUpdateNextItemDto newItemDto) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("구독 정보를 찾을 수 없습니다."));

        Product product = productRepository.findById(newItemDto.getProductId())
                .orElseThrow(() -> new RuntimeException("상품 정보를 찾을 수 없습니다: " + newItemDto.getProductId()));

        SubscriptionNextItem newItem = SubscriptionNextItem.builder()
                .subscription(subscription)
                .product(product)
                .nextMonthQuantity(newItemDto.getNextMonthQuantity())
                .nextMonthPrice(newItemDto.getNextMonthPrice())
                .build();

        // ✅ 새로운 아이템 저장 후 반환
        return subscriptionNextItemRepository.save(newItem);
    }





    /**
     * 자동 결제 처리 (매월 결제일에 호출)
     * - 다음 결제일이 되면 자동으로 결제를 처리하고 구독 정보를 갱신
     */
    @Transactional
    public void processSubscriptionBilling(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("구독 정보를 찾을 수 없습니다."));

        if (!subscription.getStatus().equals("active")) {
            throw new RuntimeException("활성화된 구독이 아닙니다.");
        }

        // 기존 SubscriptionItem 삭제
        subscriptionItemRepository.deleteAll(subscription.getItems());

        // SubscriptionNextItem → SubscriptionItem으로 복사
        List<SubscriptionItem> newItems = subscription.getNextItems().stream()
                .map(nextItem -> SubscriptionItem.builder()
                        .subscription(subscription)
                        .product(nextItem.getProduct())
                        .quantity(nextItem.getNextMonthQuantity())
                        .price(nextItem.getNextMonthPrice())
                        .build())
                .collect(Collectors.toList());

        subscriptionItemRepository.saveAll(newItems);

        // SubscriptionNextItem 삭제
        subscriptionNextItemRepository.deleteAll(subscription.getNextItems());

        // 회차 증가 및 결제일 갱신
        subscription.setCurrentCycle(subscription.getCurrentCycle() + 1);
        subscription.setLastBillingDate(subscription.getNextBillingDate());
        subscription.setNextBillingDate(subscription.getNextBillingDate().plusMonths(1));

        subscriptionRepository.save(subscription);
    }


    /**
     * 다음 결제일이 가장 최근인 구독을 가져오는 메서드(추 후 결제로직에 사용가능성 있어서 만듬
     * 예를들어 구독아이템 즉시결제 등
     * @param memberId
     * @return
     */
    @Transactional(readOnly = true)
    public Subscription getNextBillingSubscription(Long memberId) {
        return subscriptionRepository.findFirstByMemberIdAndStatusOrderByNextBillingDateDesc(memberId, "active")
                .orElseThrow(() -> new RuntimeException("다음 결제일이 예정된 활성화된 구독이 없습니다."));
    }


    @Transactional
    public boolean replaceNextSubscriptionItems(Long subscriptionId, List<SubscriptionUpdateNextItemDto> updatedItems) {
        try {
            // 기존 구독 아이템 삭제
            subscriptionNextItemRepository.deleteBySubscriptionId(subscriptionId);

            // 📌 [수정] Subscription 객체 생성
            Subscription subscription = new Subscription();
            subscription.setId(subscriptionId);  // 객체에 ID만 설정 (DB에는 존재하는 값이므로 OK)

            // 새 리스트 추가
            for (SubscriptionUpdateNextItemDto item : updatedItems) {
                SubscriptionNextItem newItem = new SubscriptionNextItem();
                newItem.setSubscription(subscription);  // ✅ subscription 객체를 직접 설정
                newItem.setProductId(item.getProductId());
                newItem.setNextMonthQuantity(item.getNextMonthQuantity());
                newItem.setNextMonthPrice(item.getNextMonthPrice());
                subscriptionNextItemRepository.save(newItem);
            }

            return true;
        } catch (Exception e) {
            log.error("❌ [ERROR] 구독 상품 교체 실패", e);
            return false;
        }
    }

    @Transactional
    public boolean deleteNextSubscriptionItem(Long subscriptionId, Long productId) {
        try {
            // ✅ productId를 기반으로 Product 객체 조회
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("해당 productId의 제품을 찾을 수 없습니다: " + productId));

            // ✅ SubscriptionNextItem에서 subscriptionId와 product 객체를 기반으로 조회
            Optional<SubscriptionNextItem> existingItem = subscriptionNextItemRepository.findBySubscriptionIdAndProduct(subscriptionId, product);

            if (existingItem.isPresent()) {
                subscriptionNextItemRepository.delete(existingItem.get());
                return true;
            } else {
                log.error("❌ [ERROR] 삭제 실패 - 해당 구독 상품 없음 (subscriptionId: {}, productId: {})", subscriptionId, productId);
                return false;
            }
        } catch (Exception e) {
            log.error("❌ [ERROR] 삭제 중 오류 발생", e);
            return false;
        }
    }




}

//그리고 일단 api 테스트만 해볼라고 지금 수정중이었는데
//궁극적으로는 저 로직은 최종적으로
//다음결제 상품을 현재 들어 가 있는 상품중 선택해서 삭제하거나  프로덕트 디비에서 조회해와서 (나중에 기능구현 끝나고 모달로예정- 지금은 아님) 상품을 추가할 수 있게 할거야
//지금은 구현단계니까 프로덕트 아이디 1~10번 불러와서 선택해서 추가할 수 있게 하면 될 듯?
//나중에는 모달창으로 상품 카테고리별로 또 이름검색으로 검색해서 추가할 수있게 할 예정임
//그다음에 수량 선택하고 그러면 개당가격하고 수량에따른 가격나오고
//완전 합계 나오고
//이렇게 된 다음 다음결제 상품 업데이트 누르면 디비에 현재 구독 아이디로 서브스크립션 넥스트 아이템에 추가되는거지 거기에 들어갈 넥스트 먼스 프라이스는 프로덕트에서 조회해서 방금 설정한 수량대로 계산되어 넣고 넥스트 먼스 퀀티티는 방금 추가나 수정할때 선택한걸로 들어가고 프로덕트아이디도 넣고
//일단 지금은 모달없이 구현해보자 이해됐어?