import { createAsyncThunk } from "@reduxjs/toolkit";
import { fetchWithAuth } from "@/features/auth/fetchWithAuth";
import { API_URL } from '@/utils/constants';

// 모든 상품 가져오기
export const fetchProducts = createAsyncThunk(
    "products/fetchProducts",
    async ({ page, size }, { rejectWithValue }) => {
        try {
            const response = await fetchWithAuth(`${API_URL}products?page=${page}&size=${size}`, {
                method: "GET"
            });

            if (!response.ok) {
                const errorData = await response.json();
                return rejectWithValue(errorData.message || "알 수 없는 에러");
            }

            const data = await response.json();
            return data;
        } catch (error) {
            return rejectWithValue("상품 목록 가져오기 실패: 네트워크 또는 서버 오류");
        }
    }
);

// 카테고리 목록 가져오기
export const fetchCategories = createAsyncThunk(
    "products/fetchCategories",
    async (_, { rejectWithValue }) => {
        try {
            const response = await fetchWithAuth(`${API_URL}categories`, {
                method: "GET"
            });

            if (!response.ok) {
                const errorData = await response.json();
                return rejectWithValue(errorData.message || "알 수 없는 에러");
            }

            const data = await response.json();
            return data;
        } catch (error) {
            return rejectWithValue("카테고리 목록 가져오기 실패: 네트워크 또는 서버 오류");
        }
    }
);

// 영양성분 선택 시 카테고리 자동 매핑 API
export const fetchCategoriesByIngredient = createAsyncThunk(
    "products/fetchCategoriesByIngredient",
    async (ingredientIds, { rejectWithValue }) => {
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${API_URL}ingredients/categories?ingredientIds=${ingredientIds.join(',')}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': token ? `Bearer ${token}` : '',
                },
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('카테고리 자동 매핑 실패');
            }

            return await response.json();
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);