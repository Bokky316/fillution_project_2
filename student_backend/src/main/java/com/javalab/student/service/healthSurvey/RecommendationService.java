package com.javalab.student.service.healthSurvey;

import com.javalab.student.dto.healthSurvey.HealthAnalysisDTO;
import com.javalab.student.dto.healthSurvey.ProductRecommendationDTO;
import com.javalab.student.dto.healthSurvey.RecommendationDTO;
import com.javalab.student.entity.Member;
import com.javalab.student.entity.healthSurvey.MemberResponse;
import com.javalab.student.entity.healthSurvey.Recommendation;
import com.javalab.student.entity.healthSurvey.RecommendedIngredient;
import com.javalab.student.entity.healthSurvey.RecommendedProduct;
import com.javalab.student.repository.healthSurvey.MemberResponseRepository;
import com.javalab.student.repository.healthSurvey.RecommendationRepository;
import com.javalab.student.repository.healthSurvey.RecommendedIngredientRepository;
import com.javalab.student.repository.healthSurvey.RecommendedProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final AuthenticationService authenticationService;
    private final MemberInfoService memberInfoService;
    private final HealthAnalysisService healthAnalysisService;
    private final NutrientScoreService nutrientScoreService;
    private final ProductRecommendationService productRecommendationService;
    private final HealthRecordService healthRecordService;
    private final BmiCalculator bmiCalculator;
    private final RecommendationRepository recommendationRepository;
    private final RecommendedIngredientRepository recommendedIngredientRepository;
    private final RecommendedProductRepository recommendedProductRepository;
    private final MemberResponseRepository memberResponseRepository;

    /**
     * 현재 로그인한 사용자의 건강 분석 및 추천 정보를 제공합니다.
     *
     * @return 건강 분석 및 추천 정보를 포함한 Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getHealthAnalysisAndRecommendations() {
        log.info("getHealthAnalysisAndRecommendations 메서드 시작");

        try {
            // 1. 현재 인증된 사용자 정보 가져오기
            Member member = authenticationService.getAuthenticatedMember();
            log.info("1. 인증된 사용자 ID: {}", member.getId());

            // 2. 사용자 정보 가져오기 (나이, 키, 몸무게 등)
            List<MemberResponse> responses = memberResponseRepository.findAgeHeightAndWeightResponses(member.getId());
            log.info("2. 사용자 응답 데이터 조회 완료. 응답 수: {}", responses.size());

            int age = memberInfoService.getAge(responses);
            double height = memberInfoService.getHeight(responses);
            double weight = memberInfoService.getWeight(responses);
            double bmi = bmiCalculator.calculateBMI(height, weight);
            String gender = memberInfoService.getGender(member.getId());

            log.info("2. 사용자 정보 계산 완료. 나이: {}, 키: {}, 몸무게: {}, BMI: {}, 성별: {}", age, height, weight, bmi, gender);

            // 3. 건강 분석 수행
            HealthAnalysisDTO healthAnalysis = healthAnalysisService.analyzeHealth(member.getId(), age, bmi, responses, gender);

            // 이름, 나이, 성별 정보 설정
            healthAnalysis.setName(member.getName());
            healthAnalysis.setAge(age);
            healthAnalysis.setGender(gender);
            healthAnalysis.setBmi(bmi);

            log.info("3. 건강 분석 완료: {}", healthAnalysis);

            // 4. 추천 영양 성분 점수 계산
            log.info("4. 추천 영양 성분 점수 계산 시작");
            Map<String, Integer> ingredientScores = nutrientScoreService.calculateIngredientScores(responses, age, bmi);
            log.info("4. 추천 영양 성분 점수 계산 완료. 점수 수: {}", ingredientScores.size());

            // 5. 추천 엔티티 생성 및 저장
            log.info("5. 추천 엔티티 생성 시작");
            Recommendation recommendation = new Recommendation();
            recommendation.setMemberId(member.getId());
            recommendation.setCreatedAt(LocalDateTime.now());
            recommendation = recommendationRepository.saveAndFlush(recommendation);
            log.info("5. 추천 엔티티 저장 완료. ID: {}", recommendation.getId());

            // 6. 추천 영양 성분 저장
            log.info("6. 추천 영양 성분 저장 시작");
            List<RecommendedIngredient> recommendedIngredients = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : ingredientScores.entrySet()) {
                RecommendedIngredient ingredient = new RecommendedIngredient();
                ingredient.setRecommendation(recommendation);
                ingredient.setIngredientName(entry.getKey());
                ingredient.setScore(entry.getValue());
                recommendedIngredients.add(ingredient);
            }
            recommendedIngredients = recommendedIngredientRepository.saveAll(recommendedIngredients);
            recommendedIngredientRepository.flush();
            log.info("6. 추천 영양 성분 저장 완료. 저장된 개수: {}", recommendedIngredients.size());

            // 7. 추천 제품 생성 및 저장
            log.info("7. 추천 제품 생성 시작");
            List<ProductRecommendationDTO> productRecommendations = productRecommendationService.recommendProductsByIngredients(
                    new ArrayList<>(ingredientScores.keySet()), ingredientScores);
            List<RecommendedProduct> recommendedProducts = new ArrayList<>();
            for (ProductRecommendationDTO product : productRecommendations) {
                RecommendedProduct recommendedProduct = new RecommendedProduct();
                recommendedProduct.setRecommendation(recommendation);
                recommendedProduct.setProductId(product.getId());
                recommendedProduct.setReason(product.getDescription());
                recommendedProducts.add(recommendedProduct);
            }
            recommendedProducts = recommendedProductRepository.saveAll(recommendedProducts);
            recommendedProductRepository.flush();
            log.info("7. 추천 제품 저장 완료. 저장된 개수: {}", recommendedProducts.size());

            // 8. HealthRecord 저장
            log.info("8. HealthRecord 저장 시작");
            List<String> recommendedIngredientNames = recommendedIngredients.stream()
                    .map(RecommendedIngredient::getIngredientName)
                    .collect(Collectors.toList());

            healthRecordService.saveHealthRecord(
                    member,
                    healthAnalysis,
                    recommendedIngredientNames,
                    productRecommendations,
                    member.getName(),
                    gender,
                    age
            );
            log.info("8. HealthRecord 저장 완료");

            // 9. 결과 반환 데이터 구성
            Map<String, Object> result = new HashMap<>();
            result.put("healthAnalysis", healthAnalysis);
            result.put("recommendedIngredients", recommendedIngredients);
            result.put("recommendations", recommendedProducts);
            log.info("9. 결과 데이터 구성 완료");

            log.info("getHealthAnalysisAndRecommendations 메서드 정상 종료");
            return result;

        } catch (Exception e) {
            log.error("getHealthAnalysisAndRecommendations 메서드 실행 중 오류 발생", e);
            throw new RuntimeException("건강 분석 및 추천 생성 중 오류가 발생했습니다.", e);
        }
    }


    /**
     * 현재 로그인한 사용자의 건강 기록 히스토리를 조회합니다.
     *
     * @return 사용자의 모든 건강 기록 리스트를 반환
     */
    @Transactional(readOnly = true)
    public List<RecommendationDTO> getHealthHistory() {
        Member member = authenticationService.getAuthenticatedMember();

        // 사용자의 모든 Recommendation 기록 조회
        List<Recommendation> recommendations = recommendationRepository.findByMemberId(member.getId());

        // Recommendation -> RecommendationDTO로 변환
        return recommendations.stream().map(recommendation -> {
            RecommendationDTO dto = new RecommendationDTO();
            dto.setId(recommendation.getId());
            dto.setMemberId(recommendation.getMemberId());
            dto.setCreatedAt(recommendation.getCreatedAt());

            // 영양 성분 정보 추가
            List<RecommendedIngredient> ingredients =
                    recommendedIngredientRepository.findByRecommendationId(recommendation.getId());

            List<ProductRecommendationDTO> productRecommendations =
                    recommendedProductRepository.findByRecommendationId(recommendation.getId())
                            .stream()
                            .map(product -> new ProductRecommendationDTO(
                                    product.getId(),
                                    null,
                                    product.getReason(),
                                    null,
                                    0,
                                    null,
                                    null))
                            .collect(Collectors.toList());

            dto.setProductRecommendations(productRecommendations);

            return dto;
        }).collect(Collectors.toList());
    }
}
