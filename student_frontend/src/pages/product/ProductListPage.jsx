import React, { useEffect, useState, useRef, useCallback } from "react";

import { useDispatch, useSelector } from "react-redux";

import {

  Grid,

  Card,

  CardContent,

  Typography,

  CardMedia,

  Chip,

  Box,

  Container,

  CircularProgress,

} from "@mui/material";

import { useNavigate } from "react-router-dom";

import { fetchProducts, fetchCategories } from "@features/product/productApi";



export default function ProductListPage() {

  const dispatch = useDispatch();

  const navigate = useNavigate();



  const { categories, error } = useSelector((state) => state.products);

  const auth = useSelector((state) => state.auth);



  const [displayedProducts, setDisplayedProducts] = useState([]);

  const [remainingProducts, setRemainingProducts] = useState([]);

  const [hasMore, setHasMore] = useState(true);

  const [isFetching, setIsFetching] = useState(false);

  const [selectedCategory, setSelectedCategory] = useState(null);

  const [allProducts, setAllProducts] = useState([]);

  const observer = useRef();



  const userRole = auth?.user?.authorities?.some((auth) => auth.authority === "ROLE_ADMIN")

    ? "ADMIN"

    : "USER";



  useEffect(() => {

    dispatch(fetchCategories());

  }, [dispatch]);



  useEffect(() => {

    const loadAllProducts = async () => {

      try {

        const initialProducts = await dispatch(fetchProducts({ page: 0, size: 100 })).unwrap();

        const activeProducts = initialProducts.filter(product => product.active); // ✅ 비활성화 상품 필터링

        setAllProducts(activeProducts);

        setDisplayedProducts(activeProducts.slice(0, 4));

        setRemainingProducts(activeProducts.slice(4));

        setHasMore(activeProducts.length > 4);

      } catch (error) {

        console.error("🚨 상품 불러오기 실패:", error);

      }

    };



    loadAllProducts();

  }, [dispatch]);



  const filterAndDisplayProducts = (products, categoryName) => {

    let filtered = products;

    if (categoryName && categoryName !== "전체") {

      filtered = products.filter(product =>

        product.categories && product.categories.includes(categoryName)

      );

    }

    return filtered;

  };



  const handleCategoryClick = (categoryName) => {

    setIsFetching(true);



    if (categoryName === "전체" || selectedCategory === categoryName) {

      setSelectedCategory(null);

      setDisplayedProducts(allProducts.slice(0, 6));

      setRemainingProducts(allProducts.slice(6));

      setHasMore(allProducts.length > 6);

    } else {

      setSelectedCategory(categoryName);

      const filteredProducts = filterAndDisplayProducts(allProducts, categoryName);

      setDisplayedProducts(filteredProducts.slice(0, 6));

      setRemainingProducts(filteredProducts.slice(6));

      setHasMore(filteredProducts.length > 6);

    }



    setIsFetching(false);

  };



  const lastProductRef = useCallback(

    (node) => {

      if (!hasMore || isFetching) return;

      if (observer.current) observer.current.disconnect();



      observer.current = new IntersectionObserver((entries) => {

        if (entries[0].isIntersecting && remainingProducts.length > 0) {

          setIsFetching(true);



          setTimeout(() => {

            const nextProducts = remainingProducts.slice(0, 2);

            setDisplayedProducts(prev => [...prev, ...nextProducts]);

            setRemainingProducts(prev => prev.slice(2));

            setHasMore(remainingProducts.length > 2);

            setIsFetching(false);

          }, 1000);

        }

      });



      if (node) observer.current.observe(node);

    },

    [remainingProducts, hasMore, isFetching]

  );



  return (

    <Container maxWidth="lg" sx={{ padding: "20px" }}>

      <Box sx={{

          display: "flex",

          gap: "10px",

          marginBottom: "20px",

          overflowX: "auto",

          padding: "10px 0" ,

          "&::-webkit-scrollbar": {

            height: "8px",

          },

          "&::-webkit-scrollbar-thumb": {

            backgroundColor: "#888",

            borderRadius: "5px",

          },

          "&::-webkit-scrollbar-thumb:hover": {

            backgroundColor: "#555",

          },

          "&::-webkit-scrollbar-track": {

            backgroundColor: "#f0f0f0",

          },

      }}>

        <Chip

          key="all"

          label="전체"

          clickable

          onClick={() => handleCategoryClick("전체")}

          color={!selectedCategory ? "primary" : "default"}

          variant={!selectedCategory ? "filled" : "outlined"}

        />

        {categories.map((category) => (

          <Chip

            key={category.name}

            label={category.name}

            clickable

            onClick={() => handleCategoryClick(category.name)}

            color={selectedCategory === category.name ? "primary" : "default"}

            variant={selectedCategory === category.name ? "filled" : "outlined"}

          />

        ))}

      </Box>



      <Grid container spacing={3}>

        {displayedProducts.map((product, index) => (

          <Grid

            item

            xs={12}

            sm={6}

            md={6}

            key={product.id}

            ref={index === displayedProducts.length - 1 ? lastProductRef : null}

          >

            <Card

              onClick={() => navigate(`/Products/${product.id}`)}

              sx={{

                cursor: "pointer",

                boxShadow: 3,

                borderRadius: 2,

                height: "100%",

                display: "flex",

                flexDirection: "column",

                ":hover": {

                  boxShadow: 6,

                  transform: "translateY(-4px)",

                  transition: "transform 0.2s ease-in-out",

                },

              }}

            >

              <CardMedia

                component="img"

                height="200"

                image={product.image || "/placeholder.jpg"}

                alt={product.name}

                sx={{ objectFit: "cover" }}

              />

              {/* ✅ 상품 정보 */}

              <CardContent sx={{ flexGrow: 1,position:"relative" }}>

               <Typography variant="h6" sx={{ fontSize:"13.5px" }} >{product.name}</Typography>

                <Typography variant="body1" sx={{ fontWeight: "bold", color: "#ff5722" }}>

                  {product.price.toLocaleString()}원

                </Typography>



                {/* ✅ 주요 성분 태그 */}

                {product.ingredients && product.ingredients.length > 0 && (

                  <Box sx={{ display: "flex", flexWrap: "wrap", gap: "5px", marginTop: "3px" }}>

                    {product.ingredients.map((ingredient, index) => (

                      <Chip

                        key={index}

                        label={ingredient}

                        sx={{

                          fontSize: "10px",

                          backgroundColor: "#f5f5f5",

                          color: "#333",

                          fontWeight: "bold",

                          borderRadius: "5px", // 둥근 정도 조정 (기본값: 16px)

                          paddingLeft: "8px",

                          paddingRight: "8px"

                        }}

                      />

                    ))}

                  </Box>

                )}



              </CardContent>

            </Card>

          </Grid>

        ))}

      </Grid>



      {/* ✅ 로딩 중 표시 (스크롤 후 데이터 로딩 중) */}

      {isFetching && (

        <Box sx={{ textAlign: "center", padding: "20px" }}>

          <CircularProgress />

          <Typography sx={{ marginTop: "10px" }}>로딩 중...</Typography>

        </Box>

      )}



      {/* ✅ 모든 상품이 표시되었을 때 메시지 */}

      {!hasMore && displayedProducts.length > 0 && (

        <Box sx={{ textAlign: "center", padding: "20px", color: "gray" }}>

          <Typography>모든 상품을 불러왔습니다.</Typography>

        </Box>

      )}



      {/* ✅ 상품이 없을 때 메시지 */}

      {displayedProducts.length === 0 && !isFetching && (

        <Box sx={{ textAlign: "center", padding: "20px", color: "gray" }}>

          <Typography>해당 카테고리의 상품이 없습니다.</Typography>

        </Box>

      )}



      {/* ✅ 에러 메시지 */}

      {error && (

        <Box sx={{ textAlign: "center", padding: "20px", color: "error.main" }}>

          <Typography>{error}</Typography>

        </Box>

      )}

    </Container>

  );

}