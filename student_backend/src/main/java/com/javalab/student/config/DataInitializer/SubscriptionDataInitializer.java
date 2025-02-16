/*
package com.javalab.student.config.DataInitializer;


import com.javalab.student.entity.*;
import com.javalab.student.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionDataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionItemRepository subscriptionItemRepository;
    private final SubscriptionNextItemRepository subscriptionNextItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeSubscriptions();
    }

    private void initializeSubscriptions() {
        // ✅ 2명의 회원 조회 (자동으로 가져오기)
        List<Member> members = memberRepository.findAll();
        if (members.size() < 2) {
            System.out.println("❌ 최소 2명의 회원이 필요합니다. 초기화를 중단합니다.");
            return;
        }

        Member member1 = members.get(0);
        Member member2 = members.get(1);

        // ✅ 기존 상품 4개 조회
        List<Product> products = productRepository.findAll();
        if (products.size() < 4) {
            System.out.println("❌ 최소 4개의 제품이 필요합니다. 초기화를 중단합니다.");
            return;
        }

        Product productA = products.get(0);
        Product productB = products.get(1);
        Product productC = products.get(2);
        Product productD = products.get(3);


        // ✅ 날짜 설정
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2); // 2달 전
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);  // 1달 전
        LocalDate today = LocalDate.now();  // 오늘

        // ✅ 1회차(지난 구독) 생성 (paymentMethod = 카드, nextPaymentMethod = 네이버페이)
        Subscription pastSubscription1 = createSubscription(member1, 1, "EXPIRED", LocalDate.now().minusMonths(2), LocalDate.now().minusMonths(2), "naverpay");
        Subscription pastSubscription2 = createSubscription(member2, 1, "EXPIRED", LocalDate.now().minusMonths(2), LocalDate.now().minusMonths(2), "naverpay");

        // ✅ 2회차(현재 구독) 생성 (paymentMethod = 1회차 nextPaymentMethod(네이버페이), nextPaymentMethod = 카카오페이)
        Subscription currentSubscription1 = createSubscription(member1, 2, "ACTIVE", pastSubscription1.getStartDate(), LocalDate.now().minusMonths(1), pastSubscription1.getNextPaymentMethod());
        Subscription currentSubscription2 = createSubscription(member2, 2, "ACTIVE", pastSubscription2.getStartDate(), LocalDate.now().minusMonths(1), pastSubscription2.getNextPaymentMethod());

        // ✅ 구독 데이터 저장
        subscriptionRepository.saveAll(List.of(pastSubscription1, pastSubscription2, currentSubscription1, currentSubscription2));

        // ✅ 구독 아이템 추가 (과거 구독)
        createSubscriptionItem(pastSubscription1, productA, 2, 30000);
        createSubscriptionItem(pastSubscription1, productB, 1, 20000);
        createSubscriptionItem(pastSubscription2, productC, 2, 35000);
        createSubscriptionItem(pastSubscription2, productD, 1, 22000);

        // ✅ 구독 아이템 추가 (현재 구독)
        createSubscriptionItem(currentSubscription1, productA, 3, 45000);
        createSubscriptionItem(currentSubscription1, productB, 2, 40000);
        createSubscriptionItem(currentSubscription2, productC, 3, 52000);
        createSubscriptionItem(currentSubscription2, productD, 2, 46000);

        // ✅ 다음 회차 구독 아이템 추가 (현재 구독)
        createSubscriptionNextItem(currentSubscription1, productC, 2, 35000);
        createSubscriptionNextItem(currentSubscription1, productD, 1, 25000);
        createSubscriptionNextItem(currentSubscription2, productA, 2, 32000);
        createSubscriptionNextItem(currentSubscription2, productB, 3, 48000);

        // ✅ 지난 구독(`EXPIRED`)도 `SubscriptionNextItem`에 추가
        createSubscriptionNextItem(pastSubscription1, productA, 2, 28000);
        createSubscriptionNextItem(pastSubscription1, productB, 1, 19000);
        createSubscriptionNextItem(pastSubscription2, productC, 2, 34000);
        createSubscriptionNextItem(pastSubscription2, productD, 1, 21000);
    }
//
//*
//     * ✅ 구독 생성 메서드
//     * @param member       구독 사용자
//     * @param cycle        현재 회차
//     * @param status       구독 상태 (ACTIVE / EXPIRED)
//     * @param startDate    최초 구독 시작일
//     * @param lastBillingDate 최근 결제일 (lastBillingDate)
//     * @return 생성된 Subscription 객체
//

    private Subscription createSubscription(Member member, int cycle, String status, LocalDate startDate, LocalDate lastBillingDate, String prevNextPaymentMethod) {
        return Subscription.builder()
                .member(member)
                .startDate(startDate) // 1회차 구독 시작일 유지
                .lastBillingDate(lastBillingDate) // 라스트 빌링 날짜 설정
                .nextBillingDate(lastBillingDate.plusMonths(1)) // ✅ lastBillingDate 기준 +1달
                .currentCycle(cycle) // 현재 회차 설정
                .paymentMethod(cycle == 1 ? "card" : prevNextPaymentMethod) // ✅ 1회차는 카드, 이후는 이전 구독의 nextPaymentMethod 사용
                .nextPaymentMethod(cycle == 1 ? "naverpay" : "kakaopay") // ✅ 1회차는 네이버페이, 이후는 카카오페이
                .roadAddress(member.getId() % 2 == 0 ? "서울 강남구 테헤란로 123" : "부산 해운대구 해변로 456") // ✅ 회원별 주소 구분
                .detailAddress(member.getId() % 2 == 0 ? "10층 1001호" : "20층 2002호")
                .status(status) // 상태 설정 (ACTIVE, EXPIRED)
                .build();
    }

    private void createSubscriptionItem(Subscription subscription, Product product, int quantity, double price) {
        SubscriptionItem item = SubscriptionItem.builder()
                .subscription(subscription)
                .product(product)
                .quantity(quantity)
                .price(price)
                .build();
        subscriptionItemRepository.save(item);
    }

    private void createSubscriptionNextItem(Subscription subscription, Product product, int quantity, double price) {
        SubscriptionNextItem nextItem = SubscriptionNextItem.builder()
                .subscription(subscription)
                .product(product)
                .nextMonthQuantity(quantity)
                .nextMonthPrice(price)
                .build();
        subscriptionNextItemRepository.save(nextItem);
    }
}
*/
