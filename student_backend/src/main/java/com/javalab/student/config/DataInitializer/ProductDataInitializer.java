/*
package com.javalab.student.config.DataInitializer;

import com.javalab.student.entity.product.Product;
import com.javalab.student.entity.product.ProductCategory;
import com.javalab.student.entity.product.ProductIngredient;
import com.javalab.student.entity.product.ProductIngredientCategoryMapping;
import com.javalab.student.repository.product.ProductCategoryRepository;
import com.javalab.student.repository.product.ProductIngredientCategoryRepository;
import com.javalab.student.repository.product.ProductIngredientRepository;
import com.javalab.student.repository.product.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductDataInitializer implements CommandLineRunner {
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final ProductRepository productRepository;
    private final ProductIngredientCategoryRepository productIngredientCategoryRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeProductCategories();
        initializeProductIngredients();
        initializeProducts();
        initializeIngredientCategoryMappings();
    }

    private void initializeProductCategories() {
        List<String> categories = Arrays.asList(
                "여성 건강/PMS", "관절/뼈", "구강 관리", "다이어트", "마음 건강",
                "피부", "노화/항산화", "피로/활력", "간 건강", "장 건강",
                "위/소화", "눈 건강", "면역력", "빈혈", "수면",
                "모발/두피", "만성질환"
        );

        categories.forEach(category -> {
            if (productCategoryRepository.findByName(category).isEmpty()) {
                ProductCategory productCategory = new ProductCategory();
                productCategory.setName(category);
                productCategoryRepository.save(productCategory);
            }
        });
    }

    private void initializeProductIngredients() {
        List<String> ingredients = Arrays.asList(
                "오메가-3", "코엔자임Q10", "비타민B군", "비타민C", "철분",
                "아연", "마그네슘", "비오틴", "루테인", "인지질(PS)",
                "GABA", "칼슘", "비타민D", "크랜베리 추출물", "글루타민",
                "콜라겐", "엽산", "감마리놀렌산(GLA)", "쏘팔메토", "아르기닌"
        );

        ingredients.forEach(ingredient -> {
            if (productIngredientRepository.findByIngredientName(ingredient).isEmpty()) {
                ProductIngredient productIngredient = new ProductIngredient();
                productIngredient.setIngredientName(ingredient);
                productIngredientRepository.save(productIngredient);
            }
        });
    }

    private void initializeIngredientCategoryMappings() {
        // 여성 건강/PMS
        addMapping("감마리놀렌산(GLA)", "여성 건강/PMS");
        addMapping("엽산", "여성 건강/PMS");
        addMapping("철분", "여성 건강/PMS");
        addMapping("크랜베리 추출물", "여성 건강/PMS");
        addMapping("GABA", "여성 건강/PMS");
        addMapping("비타민B군", "여성 건강/PMS");

        // 관절/뼈
        addMapping("칼슘", "관절/뼈");
        addMapping("비타민D", "관절/뼈");
        addMapping("마그네슘", "관절/뼈");

        // 구강 관리
        addMapping("비타민C", "구강 관리");
        addMapping("코엔자임Q10", "구강 관리");
        addMapping("비오틴", "구강 관리");

        // 다이어트
        addMapping("아르기닌", "다이어트");
        addMapping("비타민B군", "다이어트");
        addMapping("마그네슘", "다이어트");

        // 마음 건강
        addMapping("GABA", "마음 건강");
        addMapping("비타민D", "마음 건강");
        addMapping("마그네슘", "마음 건강");
        addMapping("오메가-3", "마음 건강");

        // 피부
        addMapping("콜라겐", "피부");
        addMapping("비타민C", "피부");
        addMapping("아연", "피부");
        addMapping("비오틴", "피부");
        addMapping("감마리놀렌산(GLA)", "피부");

        // 노화/항산화
        addMapping("오메가-3", "노화/항산화");
        addMapping("코엔자임Q10", "노화/항산화");
        addMapping("비타민C", "노화/항산화");
        addMapping("루테인", "노화/항산화");

        // 피로/활력
        addMapping("비타민B군", "피로/활력");
        addMapping("코엔자임Q10", "피로/활력");
        addMapping("철분", "피로/활력");
        addMapping("마그네슘", "피로/활력");

        // 간 건강
        addMapping("비타민B군", "간 건강");
        addMapping("오메가-3", "간 건강");

        // 장 건강
        addMapping("글루타민", "장 건강");
        addMapping("비타민B군", "장 건강");

        // 위/소화
        addMapping("글루타민", "위/소화");
        addMapping("비타민B군", "위/소화");
        addMapping("아연", "위/소화");

        // 눈 건강
        addMapping("루테인", "눈 건강");
        addMapping("쏘팔메토", "눈 건강");
        addMapping("오메가-3", "눈 건강");

        // 면역력
        addMapping("비타민C", "면역력");
        addMapping("아연", "면역력");
        addMapping("비타민D", "면역력");
        addMapping("글루타민", "면역력");

        // 빈혈
        addMapping("철분", "빈혈");
        addMapping("비타민B군", "빈혈");
        addMapping("엽산", "빈혈");

        // 수면
        addMapping("GABA", "수면");
        addMapping("마그네슘", "수면");

        // 모발/두피
        addMapping("비오틴", "모발/두피");
        addMapping("아연", "모발/두피");
        addMapping("아르기닌", "모발/두피");

        // 만성질환
        addMapping("오메가-3", "만성질환");
        addMapping("코엔자임Q10", "만성질환");
        addMapping("인지질(PS)", "만성질환");
        addMapping("비타민D", "만성질환");
    }

    private void addMapping(String ingredientName, String categoryName) {
        ProductIngredient ingredient = productIngredientRepository.findByIngredientName(ingredientName)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: " + ingredientName));

        ProductCategory category = productCategoryRepository.findByName(categoryName)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

        if (!productIngredientCategoryRepository.findCategoriesByIngredientId(ingredient.getId()).contains(category)) {
            ProductIngredientCategoryMapping mapping = new ProductIngredientCategoryMapping();
            mapping.setIngredient(ingredient);
            mapping.setCategory(category);
            productIngredientCategoryRepository.save(mapping);
        }
    }

    private void initializeProducts() {
        // 카테고리 조회
        ProductCategory 여성건강 = productCategoryRepository.findByName("여성 건강/PMS").orElseThrow();
        ProductCategory 관절뼈 = productCategoryRepository.findByName("관절/뼈").orElseThrow();
        ProductCategory 구강관리 = productCategoryRepository.findByName("구강 관리").orElseThrow();
        ProductCategory 다이어트 = productCategoryRepository.findByName("다이어트").orElseThrow();
        ProductCategory 마음건강 = productCategoryRepository.findByName("마음 건강").orElseThrow();
        ProductCategory 피부 = productCategoryRepository.findByName("피부").orElseThrow();
        ProductCategory 노화항산화 = productCategoryRepository.findByName("노화/항산화").orElseThrow();
        ProductCategory 피로활력 = productCategoryRepository.findByName("피로/활력").orElseThrow();
        ProductCategory 간건강 = productCategoryRepository.findByName("간 건강").orElseThrow();
        ProductCategory 장건강 = productCategoryRepository.findByName("장 건강").orElseThrow();
        ProductCategory 위소화 = productCategoryRepository.findByName("위/소화").orElseThrow();
        ProductCategory 눈건강 = productCategoryRepository.findByName("눈 건강").orElseThrow();
        ProductCategory 면역력 = productCategoryRepository.findByName("면역력").orElseThrow();
        ProductCategory 빈혈 = productCategoryRepository.findByName("빈혈").orElseThrow();
        ProductCategory 수면 = productCategoryRepository.findByName("수면").orElseThrow();
        ProductCategory 모발두피 = productCategoryRepository.findByName("모발/두피").orElseThrow();
        ProductCategory 만성질환 = productCategoryRepository.findByName("만성질환").orElseThrow();

        // 성분 조회
        ProductIngredient 감마리놀렌산 = productIngredientRepository.findByIngredientName("감마리놀렌산(GLA)")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 감마리놀렌산(GLA)"));

        ProductIngredient 엽산 = productIngredientRepository.findByIngredientName("엽산")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 엽산"));

        ProductIngredient 철분 = productIngredientRepository.findByIngredientName("철분")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 철분"));

        ProductIngredient 칼슘 = productIngredientRepository.findByIngredientName("칼슘")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 칼슘"));

        ProductIngredient 비타민D = productIngredientRepository.findByIngredientName("비타민D")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 비타민D"));

        ProductIngredient GABA = productIngredientRepository.findByIngredientName("GABA")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: GABA"));

        ProductIngredient 비타민B군 = productIngredientRepository.findByIngredientName("비타민B군")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 비타민B군"));

        ProductIngredient 마그네슘 = productIngredientRepository.findByIngredientName("마그네슘")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 마그네슘"));

        ProductIngredient 비타민C = productIngredientRepository.findByIngredientName("비타민C")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 비타민C"));

        ProductIngredient 코엔자임Q10 = productIngredientRepository.findByIngredientName("코엔자임Q10")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 코엔자임Q10"));

        ProductIngredient 오메가3 = productIngredientRepository.findByIngredientName("오메가-3")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 오메가-3"));

        ProductIngredient 루테인 = productIngredientRepository.findByIngredientName("루테인")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 루테인"));

        ProductIngredient 콜라겐 = productIngredientRepository.findByIngredientName("콜라겐")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 콜라겐"));

        ProductIngredient 인지질 = productIngredientRepository.findByIngredientName("인지질(PS)")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 인지질(PS)"));

        ProductIngredient 글루타민 = productIngredientRepository.findByIngredientName("글루타민")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 글루타민"));

        ProductIngredient 아연 = productIngredientRepository.findByIngredientName("아연")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 아연"));

        ProductIngredient 비오틴 = productIngredientRepository.findByIngredientName("비오틴")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 비오틴"));

        ProductIngredient 쏘팔메토 = productIngredientRepository.findByIngredientName("쏘팔메토")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 쏘팔메토"));

        ProductIngredient 아르기닌 = productIngredientRepository.findByIngredientName("아르기닌")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Ingredient not found: 아르기닌"));

        List<Product> products = Arrays.asList(
                Product.builder()
                        .name("종근당 황후의봄")
                        .description("여성 건강을 위한 감마리놀렌산(GLA) 보충제")
                        .price(new BigDecimal("30000"))
                        .stock(50)
                        .active(true)
                        .categories(List.of(여성건강))
                        .ingredients(List.of(감마리놀렌산))
                        .build(),
                Product.builder()
                        .name("뉴트라라이프 보라지 오일")
                        .description("감마리놀렌산(GLA) 함유 보충제")
                        .price(new BigDecimal("28000"))
                        .stock(40)
                        .active(true)
                        .categories(List.of(여성건강))
                        .ingredients(List.of(감마리놀렌산))
                        .build(),
                Product.builder()
                        .name("솔가 엽산 400mcg")
                        .description("여성 건강과 임신 준비를 위한 엽산 보충제")
                        .price(new BigDecimal("15000"))
                        .stock(100)
                        .active(true)
                        .categories(List.of(여성건강))
                        .ingredients(List.of(엽산))
                        .build(),
                Product.builder()
                        .name("페로글로빈 철분 시럽")
                        .description("빈혈 예방을 위한 철분 보충제")
                        .price(new BigDecimal("20000"))
                        .stock(60)
                        .active(true)
                        .categories(List.of(빈혈))
                        .ingredients(List.of(철분))
                        .build(),
                Product.builder()
                        .name("센트룸 칼슘+D3")
                        .description("뼈 건강을 위한 칼슘과 비타민D 보충제")
                        .price(new BigDecimal("22000"))
                        .stock(100)
                        .active(true)
                        .categories(List.of(관절뼈))
                        .ingredients(List.of(칼슘, 비타민D))
                        .build(),
                Product.builder()
                        .name("나우푸드 GABA 500mg")
                        .description("긴장 완화와 숙면을 위한 GABA 보충제")
                        .price(new BigDecimal("28000"))
                        .stock(90)
                        .active(true)
                        .categories(List.of(수면))
                        .ingredients(List.of(GABA))
                        .build(),
                Product.builder()
                        .name("솔가 비타민 B6 100mg")
                        .description("PMS 및 신경 건강을 위한 비타민 B6 보충제")
                        .price(new BigDecimal("16000"))
                        .stock(100)
                        .active(true)
                        .categories(List.of(여성건강, 피로활력))
                        .ingredients(List.of(비타민B군))
                        .build(),
                Product.builder()
                        .name("닥터스베스트 고흡수 마그네슘")
                        .description("근육 이완과 신경 안정에 도움을 주는 마그네슘")
                        .price(new BigDecimal("24000"))
                        .stock(100)
                        .active(true)
                        .categories(List.of(수면, 관절뼈))
                        .ingredients(List.of(마그네슘))
                        .build(),
                Product.builder()
                        .name("얼라이브 비타민 C 1000mg")
                        .description("면역력 향상과 항산화 효과를 위한 비타민 C")
                        .price(new BigDecimal("18000"))
                        .stock(150)
                        .active(true)
                        .categories(List.of(구강관리, 면역력))
                        .ingredients(List.of(비타민C))
                        .build(),
                Product.builder()
                        .name("나우푸드 CoQ10 100mg")
                        .description("항산화 및 심혈관 건강을 위한 CoQ10 보충제")
                        .price(new BigDecimal("29000"))
                        .stock(90)
                        .active(true)
                        .categories(List.of(노화항산화, 만성질환))
                        .ingredients(List.of(코엔자임Q10))
                        .build(),
                Product.builder()
                        .name("네이처메이드 오메가-3")
                        .description("심혈관 건강을 위한 고순도 오메가-3")
                        .price(new BigDecimal("31000"))
                        .stock(85)
                        .active(true)
                        .categories(List.of(노화항산화, 간건강))
                        .ingredients(List.of(오메가3))
                        .build(),
                Product.builder()
                        .name("닥터스베스트 루테인")
                        .description("눈 건강을 위한 루테인 보충제")
                        .price(new BigDecimal("22000"))
                        .stock(90)
                        .active(true)
                        .categories(List.of(눈건강))
                        .ingredients(List.of(루테인))
                        .build(),
                Product.builder()
                        .name("네오셀 슈퍼 콜라겐+C")
                        .description("피부 건강을 위한 콜라겐 보충제")
                        .price(new BigDecimal("33000"))
                        .stock(75)
                        .active(true)
                        .categories(List.of(피부))
                        .ingredients(List.of(콜라겐, 비타민C))
                        .build(),
                Product.builder()
                        .name("닥터스베스트 포스파티딜세린")
                        .description("인지력 개선을 위한 인지질 보충제")
                        .price(new BigDecimal("28000"))
                        .stock(60)
                        .active(true)
                        .categories(List.of(만성질환))
                        .ingredients(List.of(인지질))
                        .build(),
                Product.builder()
                        .name("닥터스베스트 칼슘 마그네슘 비타민D")
                        .description("뼈 건강을 위한 필수 미네랄과 비타민D 보충제")
                        .price(new BigDecimal("32000"))
                        .stock(100)
                        .active(true)
                        .categories(List.of(관절뼈, 피로활력, 수면))
                        .ingredients(List.of(칼슘, 마그네슘, 비타민D))
                        .build(),
                Product.builder()
                        .name("솔가 아연 50mg")
                        .description("면역력 강화와 피부 건강을 위한 아연 보충제")
                        .price(new BigDecimal("19000"))
                        .stock(120)
                        .active(true)
                        .categories(List.of(면역력, 피부))
                        .ingredients(List.of(아연))
                        .build(),
                Product.builder()
                        .name("나우푸드 글루타민 1000mg")
                        .description("장 건강과 면역력 증진을 위한 글루타민")
                        .price(new BigDecimal("25000"))
                        .stock(80)
                        .active(true)
                        .categories(List.of(장건강, 면역력))
                        .ingredients(List.of(글루타민))
                        .build(),
                Product.builder()
                        .name("재로우 쏘팔메토")
                        .description("전립선과 탈모 예방을 위한 쏘팔메토 보충제")
                        .price(new BigDecimal("27000"))
                        .stock(70)
                        .active(true)
                        .categories(List.of(만성질환))
                        .ingredients(List.of(쏘팔메토))
                        .build(),
                Product.builder()
                        .name("NOW 아르기닌 1000mg")
                        .description("운동 능력 향상과 혈액순환 개선을 위한 아르기닌")
                        .price(new BigDecimal("26000"))
                        .stock(95)
                        .active(true)
                        .categories(List.of(피로활력))
                        .ingredients(List.of(아르기닌))
                        .build(),
                Product.builder()
                        .name("네이처스바운티 비오틴 5000mcg")
                        .description("모발과 손톱 건강을 위한 비오틴")
                        .price(new BigDecimal("21000"))
                        .stock(110)
                        .active(true)
                        .categories(List.of(피부))
                        .ingredients(List.of(비오틴))
                        .build()
        );

        productRepository.saveAll(products);
    }
}*/
