package com.javalab.student.service.healthSurvey;

import com.javalab.student.dto.healthSurvey.HealthAnalysisDTO;
import com.javalab.student.dto.healthSurvey.ProductRecommendationDTO;
import com.javalab.student.entity.Member;
import com.javalab.student.entity.healthSurvey.HealthRecord;
import com.javalab.student.entity.healthSurvey.MemberResponse;
import com.javalab.student.repository.healthSurvey.MemberResponseRepository;
import com.javalab.student.service.healthSurvey.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 추천 영양 성분 및 제품 추천 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final AuthenticationService authenticationService;
    private final MemberInfoService memberInfoService;
    private final BmiCalculator bmiCalculator;
    private final HealthAnalysisService healthAnalysisService;
    private final HealthRecordService healthRecordService;
    private final MemberResponseRepository memberResponseRepository;
    private final RiskCalculationService riskCalculationService;
    private final ProductRecommendationService productRecommendationService;
    private final NutrientScoreService nutrientScoreService;

    /**
     * 현재 로그인한 사용자의 건강 상태를 분석하고 제품을 추천합니다.
     *
     * @return 건강 분석 결과, 추천 제품, 추천 영양 성분을 포함하는 Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getHealthAnalysisAndRecommendations() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 인증 정보 확인 및 사용자 정보 조회
            Member member = authenticationService.getAuthenticatedMember();
            log.info("Authenticated member: {}", member.getId());

            // 사용자 입력 기반의 응답 목록 가져오기
            List<MemberResponse> ageHeightWeightResponses = memberResponseRepository.findAgeHeightAndWeightResponses(member.getId());
            log.info("User responses: {}", ageHeightWeightResponses);

            if (ageHeightWeightResponses.isEmpty()) {
                throw new IllegalStateException("사용자 응답이 없습니다.");
            }

            // 사용자 정보 추출
            String name = memberInfoService.getName(member.getId());
            String gender = memberInfoService.getGender(member.getId());
            int age = memberInfoService.getAge(ageHeightWeightResponses);
            log.info("Extracted user info - name: {}, gender: {}, age: {}", name, gender, age);

            // 키와 몸무게 추출
            double height = memberInfoService.getHeight(ageHeightWeightResponses);
            double weight = memberInfoService.getWeight(ageHeightWeightResponses);
            log.info("Extracted height: {}, weight: {}", height, weight);

            if (height <= 0 || weight <= 0) {
                throw new IllegalStateException("키 또는 몸무게 데이터가 올바르지 않습니다.");
            }

            // BMI 계산
            double bmi = bmiCalculator.calculateBMI(height, weight);
            log.info("Calculated BMI: {}", bmi);

            // 건강 분석 수행
            HealthAnalysisDTO healthAnalysis = healthAnalysisService.analyzeHealth(member.getId(), age, bmi, ageHeightWeightResponses, gender);
            if (healthAnalysis == null) {
                throw new IllegalStateException("건강 분석 결과가 null입니다.");
            }
            healthAnalysis.setName(name);
            healthAnalysis.setGender(gender);
            healthAnalysis.setAge(age);
            result.put("healthAnalysis", healthAnalysis);

            // 추천 영양 성분 및 제품 추천 추가
            Map<String, Integer> ingredientScores = nutrientScoreService.calculateIngredientScores(ageHeightWeightResponses, age, bmi);
            List<Map<String, Object>> recommendedIngredients = nutrientScoreService.getRecommendedIngredients(healthAnalysis, ingredientScores, age, bmi);

            // recommendedIngredients가 null인 경우 빈 리스트로 초기화
            if (recommendedIngredients == null) {
                recommendedIngredients = new ArrayList<>();
            }

            List<ProductRecommendationDTO> recommendations = new ArrayList<>();
            // recommendedIngredients가 비어 있지 않은 경우에만 제품 추천
            if (!recommendedIngredients.isEmpty()) {
                recommendations = productRecommendationService.recommendProductsByIngredients(
                        recommendedIngredients.stream()
                                .map(x -> (String) x.get("name"))
                                .collect(Collectors.toList()),
                        ingredientScores
                );
            }

            result.put("recommendations", recommendations);
            result.put("recommendedIngredients", recommendedIngredients);

            // 건강 기록 저장
            healthRecordService.saveHealthRecord(member, healthAnalysis,
                    recommendedIngredients.stream().map(x -> (String) x.get("name")).collect(Collectors.toList()),
                    recommendations,
                    name, gender, age);

            log.info("Health analysis and recommendations generated successfully");
            return result;
        } catch (Exception e) {
            log.error("Error in getHealthAnalysisAndRecommendations", e);
            throw new RuntimeException("건강 분석 및 추천 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자의 나이, BMI, 응답을 기반으로 건강 위험도를 계산합니다.
     *
     * @param age       사용자의 나이
     * @param bmi       사용자의 BMI
     * @param responses 사용자의 설문 응답 목록
     * @return 계산된 위험도 정보를 포함한 Map
     */
    public Map<String, String> calculateRisks(int age, double bmi, List<MemberResponse> responses) {
        return riskCalculationService.calculateAllRisks(age, bmi, responses);
    }

    /**
     * 추천 영양 성분에 따른 제품 추천 API.
     *
     * @param age       사용자의 나이
     * @param bmi       사용자의 BMI
     * @param responses 사용자의 설문 응답 목록
     * @return 추천된 제품 목록을 포함한 ResponseEntity
     */
    public Map<String, List<ProductRecommendationDTO>> recommendProductsByIngredients(int age, double bmi, List<MemberResponse> responses) {
        // 1. 영양 성분 점수 계산
        Map<String, Integer> ingredientScores = nutrientScoreService.calculateIngredientScores(responses, age, bmi);

        // 2. HealthAnalysisDTO 생성 (최소한의 정보만 설정)
        HealthAnalysisDTO healthAnalysis = new HealthAnalysisDTO();

        // 3. 추천 영양 성분 목록 가져오기 (이름과 점수 포함)
        List<Map<String, Object>> recommendedIngredients = nutrientScoreService.getRecommendedIngredients(healthAnalysis, ingredientScores, age, bmi);

        // 4. 제품 추천
        List<ProductRecommendationDTO> recommendations =
                productRecommendationService.recommendProductsByIngredients(
                        recommendedIngredients.stream().map(x -> (String) x.get("name")).collect(Collectors.toList()),
                        ingredientScores
                );

        // 5. 결과를 Map에 담아서 반환
        Map<String, List<ProductRecommendationDTO>> resultMap = new HashMap<>();
        resultMap.put("recommendations", recommendations);

        return resultMap;
    }

    /**
     * 사용자의 응답, 나이, BMI를 기반으로 영양 점수를 계산합니다.
     *
     * @param responses 사용자의 설문 응답 목록
     * @param age       사용자의 나이
     * @param bmi       사용자의 BMI
     * @return 계산된 영양 점수를 포함한 Map
     */
    public Map<String, Integer> calculateNutrientScores(List<MemberResponse> responses, int age, double bmi) {
        return nutrientScoreService.calculateIngredientScores(responses, age, bmi);
    }


    /**
     * 현재 로그인한 사용자의 건강 기록 히스토리를 조회합니다.
     *
     * @return 건강 기록 리스트
     */
    public List<HealthRecord> getHealthHistory() {
        Member member = authenticationService.getAuthenticatedMember();
        return healthRecordService.getHealthHistory(member.getId());
    }
}
