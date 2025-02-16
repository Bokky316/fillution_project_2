package com.javalab.student.controller.product;

import com.javalab.student.entity.product.ProductCategory;
import com.javalab.student.entity.product.ProductIngredient;
import com.javalab.student.repository.product.ProductIngredientCategoryRepository;
import com.javalab.student.repository.product.ProductIngredientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@Slf4j
public class ProductIngredientController {

    private final ProductIngredientRepository ingredientRepository;
    private final ProductIngredientCategoryRepository ingredientCategoryRepository;

    public ProductIngredientController(ProductIngredientRepository ingredientRepository,
                                       ProductIngredientCategoryRepository ingredientCategoryRepository) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientCategoryRepository = ingredientCategoryRepository;
    }

    // ✅ 기존 영양성분 전체 조회 API (유지)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<ProductIngredient>> getAllIngredients() {
        try {
            log.info("영양성분 목록 조회 시작");
            List<ProductIngredient> ingredients = ingredientRepository.findAll();
            log.info("조회된 영양성분 수: {}", ingredients.size());
            return ResponseEntity.ok(ingredients);
        } catch (Exception e) {
            log.error("영양성분 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ 추가: 영양성분 ID 목록을 받아 해당하는 카테고리 목록 반환
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<ProductCategory>> getCategoriesByIngredientIds(@RequestParam(name = "ingredientIds", required = false) List<Long> ingredientIds) {
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            return ResponseEntity.ok(List.of()); // 빈 리스트 반환
        }
        List<ProductCategory> categories = ingredientCategoryRepository.findCategoriesByIngredientIds(ingredientIds);
        return ResponseEntity.ok(categories);
    }
}
