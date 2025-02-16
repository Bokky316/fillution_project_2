package com.javalab.student.service.healthSurvey;

import com.javalab.student.dto.healthSurvey.ProductRecommendationDTO;
import com.javalab.student.entity.product.Product;
import com.javalab.student.entity.product.ProductIngredient;
import com.javalab.student.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 추천된 영양 성분을 기반으로 제품을 추천하는 서비스 클래스
 */
@Service
public class ProductRecommendationService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * 추천된 영양 성분 목록에 따라 제품을 추천합니다.
     * 이 메소드는 필수 영양 성분과 추가 영양 성분을 모두 고려하여 제품을 추천하고,
     * 각 제품이 얼마나 많은 추천 영양 성분을 포함하는지에 따라 정렬합니다.
     *
     * @param recommendedIngredients 추천된 영양 성분 이름 목록 (필수 및 추가 영양 성분 포함)
     * @param ingredientScores       영양 성분별 점수 맵
     * @return 추천 제품 목록 (각 영양 성분별로 그룹화되지 않음)
     */
    public List<ProductRecommendationDTO> recommendProductsByIngredients(List<String> recommendedIngredients, Map<String, Integer> ingredientScores) {
        List<Product> allProducts = productRepository.findAll();

        // 모든 제품에 대해 추천 영양 성분 포함 여부를 확인하고 DTO로 변환
        List<ProductRecommendationDTO> recommendedProducts = allProducts.stream()
                .map(product -> convertToDTO(product, recommendedIngredients, ingredientScores))
                .filter(dto -> !dto.getRecommendedIngredients().isEmpty()) // 추천 영양 성분을 포함한 제품만 필터링
                .sorted(Comparator.comparingInt(dto -> -dto.getRecommendedIngredients().size())) // 추천 영양 성분 개수 내림차순 정렬
                .collect(Collectors.toList());

        // 중복 영양 성분을 처리하면서 최종 추천 목록 생성
        List<ProductRecommendationDTO> finalRecommendations = new ArrayList<>();
        Set<String> coveredIngredients = new HashSet<>();

        for (ProductRecommendationDTO product : recommendedProducts) {
            List<String> productIngredients = product.getRecommendedIngredients();

            // 아직 커버되지 않은 새로운 영양 성분이 있는 경우에만 추가
            if (productIngredients.stream().anyMatch(ingredient -> !coveredIngredients.contains(ingredient))) {
                finalRecommendations.add(product);
                coveredIngredients.addAll(productIngredients);
            }

            // 모든 추천 영양 성분이 커버되면 종료
            if (coveredIngredients.containsAll(recommendedIngredients)) {
                break;
            }
        }

        return finalRecommendations;
    }

    /**
     * Product 엔티티를 ProductRecommendationDTO로 변환합니다.
     * 이 메소드는 제품에 포함된 영양 성분 중 추천된 영양 성분만 필터링하고,
     * 해당 영양 성분의 점수를 함께 저장합니다.
     *
     * @param product               변환할 Product 엔티티
     * @param recommendedIngredients 추천된 영양 성분 목록
     * @param ingredientScores      영양 성분 점수 맵
     * @return 변환된 ProductRecommendationDTO
     */
    private ProductRecommendationDTO convertToDTO(Product product, List<String> recommendedIngredients, Map<String, Integer> ingredientScores) {
        // 제품에 포함된 추천 영양 성분 추출
        List<String> productRecommendedIngredients = product.getIngredients().stream()
                .map(ProductIngredient::getIngredientName)
                .filter(recommendedIngredients::contains)
                .collect(Collectors.toList());

        // 제품에 포함된 각 영양 성분의 점수를 매핑합니다.
        Map<String, Integer> productIngredientScores = product.getIngredients().stream()
                .filter(ingredient -> recommendedIngredients.contains(ingredient.getIngredientName())) // 추천된 영양 성분만 필터링
                .collect(Collectors.toMap(
                        ProductIngredient::getIngredientName,
                        ingredient -> ingredientScores.getOrDefault(ingredient.getIngredientName(), 0)
                ));

        return new ProductRecommendationDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getScore(),
                String.join(", ", productRecommendedIngredients), // 추천 영양 성분을 쉼표로 구분하여 저장
                productIngredientScores
        );
    }
}
