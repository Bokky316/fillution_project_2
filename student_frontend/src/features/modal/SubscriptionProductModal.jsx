import React from "react";
import { Box, Typography, Button } from "@mui/material";
import { useSelector } from "react-redux";

export default function SubscriptionProductModal({ isOpen, onClose, products, selectedItems, onSelectProduct }) {
    const allProducts = useSelector((state) => state.subscription.products); // ✅ Redux에서 전체 상품 정보 가져오기

    if (!isOpen) return null;

    // ✅ Redux에서 상품명으로 제품을 찾아서 대표 이미지 URL 가져오기
    const getProductImageUrl = (productName) => {
        const product = allProducts.find((p) => p.name === productName);
        if (product && product.mainImageUrl) {
            const baseUrl = import.meta.env.VITE_PUBLIC_URL || "http://localhost:8080"; // ✅ API 기본 URL 설정
            return `${baseUrl}${product.mainImageUrl.startsWith("/") ? product.mainImageUrl : "/" + product.mainImageUrl}`;
        }
        return "https://dummyimage.com/70x70/cccccc/ffffff&text=No+Image"; // 기본 이미지
    };

    return (
        <Box sx={{
            position: "fixed", top: "50%", left: "50%", transform: "translate(-50%, -50%)",
            background: "#fff", padding: "20px", borderRadius: "10px", boxShadow: "0px 0px 10px rgba(0,0,0,0.3)",
            width: "90%", maxWidth: "500px", maxHeight: "80vh", overflowY: "auto", zIndex: 1000
        }}>
            {/* 모달 헤더 */}
            <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
                <Typography variant="h6" sx={{ fontWeight: "bold", textAlign: "center", flexGrow: 1 }}>
                    정기구독 제품 추가
                </Typography>
                <button onClick={onClose} style={{
                    background: "none", border: "none", fontSize: "20px", cursor: "pointer"
                }}>
                    ✖
                </button>
            </Box>

            {/* 상품 리스트 */}
            {products.map((product) => {
                const isAdded = selectedItems.some(item => item.productId === product.id);
                const imageUrl = getProductImageUrl(product.name); // ✅ Redux에서 이미지 URL 가져오기

                return (
                    <Box key={product.id} sx={{
                        display: "flex", alignItems: "center", mb: 2, pb: 1, borderBottom: "1px solid #eee"
                    }}>
                        <img
                            src={imageUrl}
                            alt={product.name}
                            style={{ width: "70px", height: "70px", objectFit: "cover", marginRight: "15px", borderRadius: "5px" }}
                        />

                        <Box sx={{ flexGrow: 1 }}>
                            <Typography variant="subtitle1" sx={{ fontWeight: "bold", mb: 0.5 }}>
                                {product.name}
                            </Typography>
                            <Typography variant="body2" sx={{ fontWeight: "bold", color: "#4169E1", mb: 1 }}>
                                {product.price.toLocaleString()}원
                            </Typography>
                            <div style={{ display: "flex", flexWrap: "wrap", gap: "5px" }}>
                                {[...product.categories, ...product.ingredients].map((tag, index) => (
                                    <span key={index} style={{
                                        fontSize: "12px", color: "#666", background: "#f0f0f0",
                                        padding: "3px 6px", borderRadius: "10px"
                                    }}>
                                        #{tag}
                                    </span>
                                ))}
                            </div>
                        </Box>

                        {isAdded ? (
                            <Typography sx={{ fontSize: "14px", fontWeight: "bold", color: "#4169E1" }}>
                                추가됨 ✔️
                            </Typography>
                        ) : (
                            <button onClick={() => onSelectProduct(product)} className="add-button">
                                +
                            </button>
                        )}
                    </Box>
                );
            })}

            {/* 닫기 버튼 */}
            <Button fullWidth variant="contained" sx={{ mt: 2 }} onClick={onClose}>
                닫기
            </Button>
        </Box>
    );
}
