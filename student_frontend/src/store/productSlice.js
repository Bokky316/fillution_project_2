import { createSlice } from "@reduxjs/toolkit";
import { fetchProducts, fetchCategories,fetchCategoriesByIngredient } from "@features/product/productApi";

const productSlice = createSlice({
    name: "products",
    initialState: {
        products: [], // 상품 목록
        categories: [], // 카테고리 목록
        totalRows: 0, // 전체 상품 개수
        loading: false, // 로딩 상태
        error: null, // 에러 상태
        selectedCategories: [] // ✅ 선택된 영양성분 기반 카테고리 추가
    },
    reducers: {
        clearSelectedCategories: (state) => {
            state.selectedCategories = [];
        }
    },
    extraReducers: (builder) => {
        builder
            // 상품 로딩 시작
            .addCase(fetchProducts.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            // 상품 로딩 성공
            .addCase(fetchProducts.fulfilled, (state, action) => {
                state.loading = false;
                state.products = action.payload || []; // payload를 바로 products에 저장
                state.totalRows = action.payload.length || 0; // 총 상품 개수 저장
            })
            // 상품 로딩 실패
            .addCase(fetchProducts.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload || "상품 데이터를 가져오지 못했습니다.";
            })
            // 카테고리 로딩 성공
            .addCase(fetchCategories.fulfilled, (state, action) => {
                state.categories = action.payload || []; // 카테고리 목록 업데이트
            })
            // ✅ 영양성분 선택 시 카테고리 자동 업데이트 처리
            .addCase(fetchCategoriesByIngredient.fulfilled, (state, action) => {
                state.selectedCategories = action.payload || [];
            })
            .addCase(fetchCategoriesByIngredient.rejected, (state, action) => {
                state.error = action.payload;
            });
    },
});
export const { clearSelectedCategories } = productSlice.actions;
export default productSlice.reducer;