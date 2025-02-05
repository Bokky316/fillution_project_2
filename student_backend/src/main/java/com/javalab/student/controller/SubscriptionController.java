package com.javalab.student.controller;

import com.javalab.student.entity.Subscription;
import com.javalab.student.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 사용자의 요청을 처리하는 API 엔드포인트
 */
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // 구독 정보 조회
    @GetMapping
    public ResponseEntity<Subscription> getSubscription(@RequestParam Long memberId) {
        return ResponseEntity.ok(subscriptionService.getSubscription(memberId));
    }

    // 결제일 변경
    @PutMapping("/update-billing-date")
    public ResponseEntity<Void> updateBillingDate(@RequestParam Long subscriptionId, @RequestParam String newDate) {
        subscriptionService.updateBillingDate(subscriptionId, LocalDate.parse(newDate));
        return ResponseEntity.ok().build();
    }

    // 결제수단 변경
    @PutMapping("/update-payment")
    public ResponseEntity<Void> updatePaymentMethod(@RequestParam Long subscriptionId, @RequestParam String method) {
        subscriptionService.updatePaymentMethod(subscriptionId, method);
        return ResponseEntity.ok().build();
    }

    // 구독 해지
    @DeleteMapping("/cancel")
    public ResponseEntity<Void> cancelSubscription(@RequestParam Long subscriptionId, @RequestParam boolean immediately) {
        subscriptionService.cancelSubscription(subscriptionId, immediately);
        return ResponseEntity.ok().build();
    }
}