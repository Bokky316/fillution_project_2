import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
    TextField,
    Button,
    Box,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Checkbox,
    ListItemText,
    Grid,
    IconButton,
    Typography,
    Chip,
    Stack
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { API_URL } from "@/utils/constants";
import axios from "axios";
import { useDispatch, useSelector } from 'react-redux';
import { fetchCategoriesByIngredient } from '@features/product/productApi';
import { clearSelectedCategories } from '@/store/productSlice';
import '@/styles/AddProduct.css';

const EditProduct = () => {
    const { productId } = useParams();
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const selectedCategories = useSelector((state) => state.products.selectedCategories) || [];
    const token = localStorage.getItem('accessToken');

    const [product, setProduct] = useState({
        name: '',
        description: '',
        price: '',
        stock: '',
        active: true,
        categoryIds: [],
        ingredientIds: [],
    });

    // ✅ 대표 이미지 관련 state
    const [mainImageFile, setMainImageFile] = useState(null);
    const [mainImagePreview, setMainImagePreview] = useState(null);

    // ✅ 상세 이미지 관련 state
    const [detailImageFiles, setDetailImageFiles] = useState(Array(4).fill(null)); // 상세 이미지 4개 배열로 초기화
    const [detailImagePreviews, setDetailImagePreviews] = useState(Array(4).fill(null)); // 상세 이미지 미리보기 4개 배열로 초기화

    const [categories, setCategories] = useState([]);
    const [ingredients, setIngredients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const getAbsoluteImageUrl = (imageUrl) => {
        if (!imageUrl) return '';
        const baseUrl = API_URL.substring(0, API_URL.indexOf('/api'));
        //console.log(`imageUrl : ', ${baseUrl}${imageUrl}`)
        return imageUrl.startsWith('http') ? imageUrl : `${baseUrl}${imageUrl}`;
    };

    const fetchIngredients = async () => {
        try {
            const response = await fetch(`${API_URL}ingredients`, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                credentials: 'include'
            });

            if (!response.ok) throw new Error('영양성분 목록을 불러오는 데 실패했습니다.');

            const data = await response.json();
            setIngredients(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error("영양성분 데이터 로딩 실패:", error);
            setError(error.message);
        }
    };

    useEffect(() => {
        const fetchData = async () => {
            try {
                await fetchIngredients();

                const productResponse = await fetch(`${API_URL}products/${productId}/dto`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    },
                    credentials: 'include'
                });

                if (!productResponse.ok) {
                    const errorData = await productResponse.json();
                    throw new Error(errorData.message || `제품 정보를 가져오는데 실패했습니다.`);
                }

                const productData = await productResponse.json();

                setProduct({
                    name: productData.name || '',
                    description: productData.description || '',
                    price: productData.price || '',
                    stock: productData.stock || '',
                    active: productData.active !== undefined ? productData.active : true,
                    categoryIds: productData.categories ? productData.categories.map(cat => cat.id) : [],
                    ingredientIds: productData.ingredients ? productData.ingredients.map(ing => ing.id) : [],
                    ingredients: productData.ingredients || []
                });

                // 영양성분에 따른 카테고리 불러오기
                if (productData.ingredients && productData.ingredients.length > 0) {
                    const ingredientIds = productData.ingredients.map(ing => ing.id);
                    dispatch(fetchCategoriesByIngredient(ingredientIds));
                }

                // 대표 이미지와 상세 이미지 분리 처리 및 state 업데이트
                if (productData.productImgList && productData.productImgList.length > 0) {
                    const mainImage = productData.productImgList.find(img => img.imageType === '대표');
                    if (mainImage) {
                        setMainImagePreview(getAbsoluteImageUrl(mainImage.imageUrl));
                    } else {
                        setMainImagePreview(null); // 대표 이미지 없는 경우 null 설정 명시적으로 추가
                    }

                    const detailImages = productData.productImgList.filter(img => img.imageType !== '대표');
                    const detailPreviews = detailImages.map(img => getAbsoluteImageUrl(img.imageUrl));
                    const initialDetailPreviews = Array(4).fill(null);
                    detailPreviews.forEach((preview, index) => {
                        if (index < 4) {
                            initialDetailPreviews[index] = preview;
                        }
                    });
                    setDetailImagePreviews(initialDetailPreviews);
                } else {
                    setMainImagePreview(null); // 이미지 없는 경우 대표 이미지 미리보기 null 로 명시적으로 초기화
                    setDetailImagePreviews(Array(4).fill(null)); // 이미지 없는 경우 상세 이미지 미리보기 null 배열로 명시적으로 초기화
                }

                // 영양성분에 따른 카테고리 불러오기
                if (productData.ingredients && productData.ingredients.length > 0) {
                    const ingredientIds = productData.ingredients.map(ing => ing.id);
                    dispatch(fetchCategoriesByIngredient(ingredientIds));
                }

            } catch (error) {
                console.error("Error fetching data:", error);
                setError(error.message);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [productId, token, dispatch]);

    useEffect(() => {
        if (selectedCategories?.length > 0) {
            setProduct(prev => ({
                ...prev,
                categoryIds: selectedCategories.map(cat => cat.id),
            }));
        }
    }, [selectedCategories]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setProduct(prev => ({
            ...prev,
            [name]: (name === 'price' || name === 'stock') ? Number(value) : value,
        }));
    };

    // ✅ 대표 이미지 변경 핸들러
    const handleMainImageChange = (e) => {
        const file = e.target.files[0];
        setMainImageFile(file);
        if (file) {
            setMainImagePreview(URL.createObjectURL(file));
        } else {
            setMainImagePreview(null);
        }
    };

    // ✅ 상세 이미지 변경 핸들러
    const handleDetailImageChange = (e, index) => {
        const file = e.target.files[0];
        const newDetailImageFiles = [...detailImageFiles];
        const newDetailImagePreviews = [...detailImagePreviews];

        if (file) {
            newDetailImageFiles[index] = file;
            newDetailImagePreviews[index] = URL.createObjectURL(file);
        } else {
            newDetailImageFiles[index] = null;
            newDetailImagePreviews[index] = null;
        }

        setDetailImageFiles(newDetailImageFiles);
        setDetailImagePreviews(newDetailImagePreviews);
    };

    // ✅ 대표 이미지 삭제 핸들러 수정 (API 호출 추가)
    const handleMainImageDelete = async () => {
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${API_URL}products/${productId}/images?imageType=대표`, { // 백엔드 API 호출 (DELETE 요청)
                method: 'DELETE',
                headers: {
                    'Authorization': token ? `Bearer ${token}` : '',
                    'Content-Type': 'application/json', // 명시적으로 Content-Type 설정
                },
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('대표 이미지 삭제 실패');
            }

            setMainImageFile(null);
            setMainImagePreview(null);
            alert('대표 이미지가 삭제되었습니다.'); // 성공 알림 추가

        } catch (error) {
            console.error("Error deleting main image:", error);
            alert('대표 이미지 삭제 중 오류가 발생했습니다.'); // 실패 알림 추가
        }
    };

    // ✅ 상세 이미지 삭제 핸들러 수정 (API 호출 추가, index 파라미터 추가)
    const handleDetailImageDelete = async (indexToDelete) => {
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${API_URL}products/${productId}/images?imageType=상세&imageIndex=${indexToDelete + 1}`, { // 백엔드 API 호출 (DELETE 요청), imageIndex 쿼리 파라미터 추가 (1부터 시작)
                method: 'DELETE',
                headers: {
                    'Authorization': token ? `Bearer ${token}` : '',
                    'Content-Type': 'application/json', // 명시적으로 Content-Type 설정
                },
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('상세 이미지 삭제 실패');
            }

            const newDetailImageFiles = [...detailImageFiles];
            const newDetailImagePreviews = [...detailImagePreviews];

            newDetailImageFiles[indexToDelete] = null;
            newDetailImagePreviews[indexToDelete] = null;

            setDetailImageFiles(newDetailImageFiles);
            setDetailImagePreviews(newDetailImagePreviews);
            alert('상세 이미지가 삭제되었습니다.'); // 성공 알림 추가


        } catch (error) {
            console.error("Error deleting detail image:", error);
            alert('상세 이미지 삭제 중 오류가 발생했습니다.'); // 실패 알림 추가
        }
    };

    const handleIngredientChange = (e) => {
        const selectedIngredients = e.target.value;
        setProduct(prev => ({ ...prev, ingredientIds: selectedIngredients }));

        if (selectedIngredients.length === 0) {
            dispatch(clearSelectedCategories());
            return;
        }

        dispatch(fetchCategoriesByIngredient(selectedIngredients));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        const formData = new FormData();

        // ✅ JSON 데이터를 Blob으로 변환하여 추가
        const productData = {
            name: product.name,
            description: product.description,
            price: product.price,
            stock: product.stock,
            active: product.active,
            ingredientIds: product.ingredientIds
        };
        formData.append('product', new Blob([JSON.stringify(productData)], { type: 'application/json' }));

        // ✅ 대표 이미지 추가 (있을 경우만)
        if (mainImageFile) {
            formData.append('mainImageFile', mainImageFile);
        }

        // ✅ 상세 이미지 추가
        detailImageFiles.forEach((file, index) => {
            if (file) {
                formData.append(`detailImageFiles`, file);
            }
        });

        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`${API_URL}products/${productId}`, {
                method: 'PUT', // 등록은 'POST', 수정은 'PUT'
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData,
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(`상품 업데이트 실패: ${response.status}`);
            }

            alert('상품이 성공적으로 업데이트되었습니다.');
            navigate('/adminPage/products');
        } catch (error) {
            console.error('Error updating product:', error);
            alert('상품 업데이트 중 오류가 발생했습니다.');
        }
    };

    // ✅ 로딩 및 에러 처리
    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;


    return (
        <Box sx={{ maxWidth: '800px', margin: '50px auto', padding: '20px', backgroundColor: '#fff', boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)', borderRadius: '8px' }} className="edit-product-container">
            <h2 className="edit-product-title">상품 수정</h2>
            <form className="edit-product-form" onSubmit={handleSubmit}>
                <TextField fullWidth label="상품명" name="name" value={product.name} onChange={handleInputChange} required margin="normal" />
                {/* ✅ 기존 상품이 가지고 있던 영양성분을 텍스트로 출력 */}
                {product.ingredients && product.ingredients.length > 0 ? (
                    <p style={{ marginTop: "10px", fontSize: "14px", color: "#666" }}>
                        기존 영양성분: {product.ingredients.join(", ")}
                    </p>
                ) : (
                    <p style={{ marginTop: "10px", fontSize: "14px", color: "#999" }}>
                        기존 영양성분이 없습니다.
                    </p>
                )}

                <FormControl fullWidth margin="normal">
                    <InputLabel>영양성분</InputLabel>
                    <Select
                        multiple
                        value={product.ingredientIds}
                        onChange={handleIngredientChange}
                    >
                        {Array.isArray(ingredients) && ingredients.length > 0 ? (
                            ingredients.map(ing => (
                                <MenuItem key={ing.id} value={ing.id}>
                                    {ing.ingredientName}
                                </MenuItem>
                            ))
                        ) : (
                            <MenuItem disabled>영양성분을 불러오는 중...</MenuItem>
                        )}
                    </Select>
                </FormControl>

                <h3>선택된 카테고리</h3>
                {selectedCategories.length > 0 ? (
                    <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
                        {selectedCategories.map((category, index) => (
                            <Chip key={index} label={category.name} variant="outlined" />
                        ))}
                    </Stack>
                ) : (
                    <p>선택된 카테고리가 없습니다.</p>
                )}

                <TextField fullWidth label="가격" name="price" type="number" value={product.price} onChange={handleInputChange} required margin="normal" />
                <TextField fullWidth label="재고" name="stock" type="number" value={product.stock} onChange={handleInputChange} required margin="normal" />
                <TextField fullWidth label="상품 상세 내용" name="description" value={product.description} onChange={handleInputChange} required multiline rows={4} margin="normal" />

                {/* 대표 이미지 영역 */}
                <Box sx={{ mt: 3, mb: 3 }} className="main-image-section">
                    <Typography variant="h6" gutterBottom>대표 이미지</Typography>
                    <Typography variant="body2" color="textSecondary" gutterBottom>상품을 대표하는 이미지를 선택해주세요.</Typography>
                    <div className="image-upload-box">
                        <input
                            className="file-input"
                            type="file"
                            name="mainImageFile"
                            // ✅ handleMainImageChange 핸들러 사용
                            onChange={handleMainImageChange}
                            accept="image/*"
                        />
                        {/* ✅ mainImagePreview state 사용 */}
                        {mainImagePreview && (
                            <Box position="relative" display="inline-block">
                                <img className="image-preview" src={mainImagePreview} alt="대표 이미지 미리보기" />
                                <IconButton
                                    // ✅ handleMainImageDelete 핸들러 사용
                                    onClick={handleMainImageDelete}
                                    sx={{ position: 'absolute', top: '-8px', right: '-8px', backgroundColor: 'rgba(255, 255, 255, 0.8)' }}
                                >
                                    <CloseIcon fontSize="small" />
                                </IconButton>
                            </Box>
                        )}
                    </div>
                </Box>

                {/* 상세 이미지 영역 */}
                <Box sx={{ mt: 3, mb: 3 }} className="detail-image-section">
                    <Typography variant="h6" gutterBottom>상세 이미지 (추가)</Typography>
                    <Typography variant="body2" color="textSecondary" gutterBottom>상품 상세 정보를 보여주는 이미지를 추가해주세요. (최대 4장)</Typography>
                    <Grid container spacing={2}>
                        {detailImagePreviews.map((previewUrl, index) => (
                            previewUrl &&
                            <Grid item xs={12} sm={6} md={4} lg={3} key={index}>
                                <div className="image-upload-box">
                                    <input
                                        className="file-input"
                                        type="file"
                                        name={`detailImageFile${index + 1}`}
                                        // ✅ handleDetailImageChange 핸들러 사용, index 전달
                                        onChange={(e) => handleDetailImageChange(e, index)}
                                        accept="image/*"
                                    />
                                    <Box position="relative" display="inline-block">
                                        <img className="image-preview" src={previewUrl} alt={`상세 이미지 미리보기 ${index + 1}`} />
                                        <IconButton
                                            // ✅ handleDetailImageDelete 핸들러 사용, index 전달
                                            onClick={() => handleDetailImageDelete(index)}
                                            sx={{ position: 'absolute', top: '-8px', right: '-8px', backgroundColor: 'rgba(255, 255, 255, 0.8)' }}
                                        >
                                            <CloseIcon fontSize="small" />
                                        </IconButton>
                                    </Box>
                                </div>
                            </Grid>
                        ))}
                        {[...Array(4 - detailImagePreviews.filter(preview => preview).length)].map((_, index) => ( // 상세 이미지 슬롯 부족분 채우기
                            <Grid item xs={12} sm={6} md={4} lg={3} key={`empty-${index}`}>
                                <div className="image-upload-box">
                                    <input
                                        className="file-input"
                                        type="file"
                                        name={`detailImageFile${detailImagePreviews.filter(preview => preview).length + index + 1}`}
                                        // ✅ handleDetailImageChange 핸들러 사용, index 계산하여 전달
                                        onChange={(e) => handleDetailImageChange(e, detailImagePreviews.filter(preview => preview).length + index)}
                                        accept="image/*"
                                    />
                                    <span>상세 이미지 추가</span>
                                </div>
                            </Grid>
                        ))}
                    </Grid>
                </Box>

                <FormControl fullWidth margin="normal">
                    <InputLabel id="active-checkbox-label">활성 상태</InputLabel>
                    <Select
                        labelId="active-checkbox-label"
                        id="active-checkbox"
                        name="active"
                        value={product.active}
                        onChange={handleInputChange}
                    >
                        <MenuItem value={true}>활성</MenuItem>
                        <MenuItem value={false}>비활성</MenuItem>
                    </Select>
                </FormControl>


                <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
                    <Button type="submit" variant="contained" color="primary" sx={{ width: '48%' }}>저장</Button>
                    <Button variant="outlined" color="secondary" sx={{ width: '48%' }} onClick={() => navigate('/adminPage/products')}>취소</Button>
                </Box>
            </form>
        </Box>
    );
};

export default EditProduct;