package com.javalab.student.controller.product;

import com.javalab.student.dto.product.ProductDto;
import com.javalab.student.dto.product.ProductFormDto;
import com.javalab.student.dto.product.ProductResponseDTO;
import com.javalab.student.entity.product.Product;
import com.javalab.student.entity.product.ProductCategory;
import com.javalab.student.repository.product.ProductCategoryRepository;
import com.javalab.student.repository.product.ProductRepository;
import com.javalab.student.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ProductCategoryRepository productCategoryRepository;

    @Value("${itemImgLocation}")
    private String itemImgLocation;

    public ProductController(ProductRepository productRepository, ProductService productService,
                             ProductCategoryRepository productCategoryRepository) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.productCategoryRepository = productCategoryRepository;
    }

    /** 특정 상품 상세 정보 조회 */
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductDetails(@PathVariable("productId") Long productId) {
        try {
            return productRepository.findById(productId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (DataAccessException e) {
            log.error("Database error occurred while fetching product with id: " + productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching product with id: " + productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** 상품 등록 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDto> createProduct(
            @RequestPart("product") @Valid ProductFormDto productFormDto, // ✅ JSON 데이터를 받는 부분
            @RequestPart(value = "mainImageFile", required = false) MultipartFile mainImageFile, // ✅ 대표 이미지
            @RequestPart(value = "detailImageFiles", required = false) List<MultipartFile> detailImageFiles) { // ✅ 상세 이미지들

        log.info("상품 등록 요청 수신: {}", productFormDto);
        try {
            productFormDto.setMainImageFile(mainImageFile);
            productFormDto.setDetailImageFiles(detailImageFiles);
            ProductDto savedProduct = productService.createProduct(productFormDto);
            log.info("상품 등록 성공: {}", savedProduct);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            log.error("상품 등록 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** 상품 수정 */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable("id") Long id,
            @RequestPart("product") @Valid ProductFormDto productFormDto, // ✅ JSON 데이터를 받는 부분
            @RequestPart(value = "mainImageFile", required = false) MultipartFile mainImageFile, // ✅ 대표 이미지
            @RequestPart(value = "detailImageFiles", required = false) List<MultipartFile> detailImageFiles) { // ✅ 상세 이미지들

        productFormDto.setMainImageFile(mainImageFile);
        productFormDto.setDetailImageFiles(detailImageFiles);
        return ResponseEntity.ok(productService.updateProduct(id, productFormDto));
    }


    /** 이미지 업로드 핸들러  */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("imageFile") MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(itemImgLocation, fileName);
            Files.createDirectories(Paths.get(itemImgLocation));
            Files.write(filePath, file.getBytes());

            String imageUrl = "/api/products/images/" + fileName;

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Image upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Image upload failed: " + e.getMessage()));
        }
    }

    /** 이미지 제공 핸들러 */
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable("filename") String filename) {
        try {
            Path file = Paths.get(itemImgLocation).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if(contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
            }
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found", e);
        } catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not determine file type.", e);
        }
    }

    /** 전체 상품 목록 조회 */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getProductList();
        return ResponseEntity.ok(products);
    }

    /** 검색어로 상품 목록 조회 (페이징) */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchProducts(
            @RequestParam("field") String field,
            @RequestParam("query") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDTO> products = productService.searchProducts(field, query, pageable);
        return ResponseEntity.ok(products);
    }

    /** 카테고리 ID로 상품 필터링 */
    @GetMapping("/filter-by-category")
    public ResponseEntity<List<ProductResponseDTO>> getProductsFilteredByCategory(@RequestParam("categoryId") Long categoryId) {
        List<ProductResponseDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    /** 상품 활성/비활성 */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleProductActive(@PathVariable("id") Long id) {
        productService.toggleProductActive(id);
        return ResponseEntity.ok().build();
    }

    /** 영양 성분 기반으로 카테고리별로 정렬된 상품 리스트 API */
    @GetMapping("/sorted-by-ingredient-and-category")
    public ResponseEntity<List<ProductResponseDTO>> getProductsSortedByIngredientAndCategory(
            @RequestParam Long ingredientId) {
        List<ProductResponseDTO> products = productService.getProductsSortedByIngredientAndCategory(ingredientId);
        return ResponseEntity.ok(products);
    }

    /** 특정 카테고리에 속한 상품 목록 조회 */
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponseDTO> products = productService.getProductsByCategory(categoryId);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(products);
        }
        return ResponseEntity.ok(products);
    }

    /** 특정 상품 상세 정보 조회 (ProductResponseDTO 반환) */
    @GetMapping("/{productId}/dto") // ✅ URL 유지
    public ResponseEntity<ProductResponseDTO> getProductDetailsDto(@PathVariable("productId") Long productId) { // ✅ 반환 타입 ProductResponseDTO 로 변경
        try {
            // ProductResponseDTO.fromEntity() 를 사용하여 Product 엔티티를 DTO로 변환
            // ProductResponseDTO productResponseDTO = ProductResponseDTO.fromEntity(productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"))); // 기존 코드 (삭제)
            ProductResponseDTO productResponseDTO = productService.getProductById(productId); // ✅ ProductService.getProductById() 사용
            return ResponseEntity.ok(productResponseDTO); // ✅ ProductResponseDTO 반환
        } catch (Exception e) {
            log.error("Error fetching product details for product ID: " + productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** 상품 카테고리만 업데이트하는 엔드포인트 */
    @PutMapping("/{id}/categories")
    public ResponseEntity<Void> updateProductCategories(@PathVariable("id") Long productId,
                                                        @RequestBody List<Long> categoryIds) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        List<ProductCategory> categories = productCategoryRepository.findAllById(categoryIds);
        product.setCategories(categories);
        productRepository.save(product);
        return ResponseEntity.ok().build();
    }

    /** 새로운 전체 상품 목록 조회 (페이징 O) */
    @GetMapping("/paged")
    public ResponseEntity<Page<ProductResponseDTO>> getAllProductsPaged(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        Page<ProductResponseDTO> responseDTOPage = productPage.map(ProductResponseDTO::fromEntity);
        return ResponseEntity.ok(responseDTOPage);
    }

    /** 새로운 카테고리별 필터링 (페이징 O) */
    @GetMapping("/filter-by-category/paged")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsFilteredByCategoryPaged(
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByCategories_Id(categoryId, pageable);
        Page<ProductResponseDTO> responseDTOPage = productPage.map(ProductResponseDTO::fromEntity);
        return ResponseEntity.ok(responseDTOPage);
    }

    /** 상품 이미지 삭제 핸들러 */
    @DeleteMapping("/{productId}/images") // 예시 URL: /api/products/1/images?imageType=대표
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable("productId") Long productId,
            @RequestParam("imageType") String imageType, // or @RequestParam("imageIndex") Integer imageIndex 상세 이미지의 경우 인덱스
            @RequestParam(value = "imageIndex", required = false) Integer imageIndex) { // 상세 이미지 인덱스 (필수 아님)
        try {
            productService.deleteProductImage(productId, imageType, imageIndex);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting product image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}