import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Button, Snackbar, Grid, Card, CardContent, Typography, CardMedia, Select, MenuItem } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { fetchProducts,fetchCategories,fetchProductsByCategory  } from "@features/product/productApi";

export default function ProductListPage() {
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const { products, categories, loading, error } = useSelector((state) => state.products);
    const auth = useSelector((state) => state.auth); // Redux에서 auth 가져오기
    const [snackbarOpen, setSnackbarOpen] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState("");
    const [selectedCategory, setSelectedCategory] = useState("");


    // Redux 상태에서 userRole 가져오기
    const userRole = auth?.user?.authorities?.some(auth => auth.authority === "ROLE_ADMIN") ? "ADMIN" : "USER";

    useEffect(() => {
        console.log("📌 fetchProducts 호출!");
        dispatch(fetchProducts({ page: 0, size: 10 }));
        dispatch(fetchCategories());
    }, [dispatch]);

   /*  const handleFilterChange = () => {
        dispatch(fetchFilteredProducts({ categoryId: selectedCategory, ingredientId: selectedIngredient }));
    }; */

    // 로그인 시 Redux 상태를 `localStorage`와 동기화
    useEffect(() => {
        if (auth?.user) {
            localStorage.setItem("auth", JSON.stringify(auth));
        }
    }, [auth]);

    const handleCategoryClick = (categoryId) => {
        setSelectedCategory(categoryId); // 선택된 카테고리 설정
        dispatch(fetchProductsByCategory(categoryId)); // 카테고리별 상품 조회
    };

    const handleCardClick = (id) => {
        navigate(`/viewProduct/${id}`);
    };

    return (
        <div style={{ padding: "20px" }}>
           {/* 카테고리 버튼 영역 */}
           <div style={{ display: "flex", gap: "10px", marginBottom: "20px" }}>
               {categories.map((category) => (
                   <Button
                       key={category.id}
                       variant={selectedCategory === category.id ? "contained" : "outlined"}
                       color={selectedCategory === category.id ? "primary" : "default"}
                       onClick={() => handleCategoryClick(category.id)}
                   >
                       {category.name}
                   </Button>
               ))}
           </div>

            <Grid container spacing={3}>
                {products.map((product) => (
                    <Grid item xs={12} sm={6} md={4} key={product.id}>
                        <Card onClick={() => handleCardClick(product.id)} style={{ cursor: "pointer" }}>
                            <CardMedia
                                component="img"
                                height="200"
                                image={product.image || "placeholder.jpg"} // 기본 이미지 설정
                                alt={product.name}
                            />
                            <CardContent>
                                <Typography variant="h6" gutterBottom>
                                    {product.name}
                                </Typography>
                                <Typography variant="body1" color="textSecondary">
                                    {product.price.toLocaleString()}원
                                </Typography>
                                {userRole === "ADMIN" && (
                                    <>
                                        <Typography variant="body2" color="textSecondary">
                                            재고: {product.stock}
                                        </Typography>
                                        <Typography variant="body2" color={product.active ? "green" : "red"}>
                                            {product.active ? "활성화" : "비활성화"}
                                        </Typography>
                                    </>
                                )}
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>

            <Snackbar
                open={snackbarOpen}
                autoHideDuration={3000}
                onClose={() => setSnackbarOpen(false)}
                message={snackbarMessage}
            />

            {/* 관리자만 상품 등록 버튼 보이게 */}
            {userRole === "ADMIN" && (
                <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate("/addProduct")}
                    style={{ marginTop: "20px" }}
                >
                    상품 등록
                </Button>
            )}

            {loading && <p>로딩 중...</p>}
            {error && <p style={{ color: "red" }}>{error}</p>}
        </div>
    );
}
