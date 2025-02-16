package com.javalab.student.controller.product;

import com.javalab.student.entity.product.ProductCategory;
import com.javalab.student.repository.product.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class ProductCategoryController {
    private final ProductCategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<ProductCategory>> getCategories() {
        List<ProductCategory> categories = categoryRepository.findAllByOrderByIdAsc();
        return ResponseEntity.ok(categories);
    }
}
