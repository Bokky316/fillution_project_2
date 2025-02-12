import React, { useEffect, useState } from 'react';
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  Button, Typography, CircularProgress, Box, styled
} from '@mui/material';
import { API_URL } from '../../../constant';
import { useNavigate } from 'react-router-dom';

const StyledDialog = styled(Dialog)(({ theme }) => ({
    '& .MuiDialogContent-root': {
        padding: theme.spacing(2),
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
    },
    '& .MuiDialogActions-root': {
        padding: theme.spacing(1),
    },
}));

const StyledDialogTitle = styled(DialogTitle)(({ theme }) => ({
    textAlign: 'center',
    backgroundColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
}));

const ProductImage = styled('img')({
    width: '50%',
    maxWidth: '300px',
    borderRadius: '4px',
    marginBottom: '16px',
});

const ViewProduct = ({ productId, open, onClose }) => {
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        if (open && productId) {
            setLoading(true);
            fetch(`${API_URL}products/${productId}/dto`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('상품 상세 정보를 불러오지 못했습니다.');
                }
                return response.json();
            })
            .then(data => {
                setProduct(data);
                setLoading(false);
            })
            .catch(err => {
                console.error('상품 상세 조회 에러:', err);
                setError(err);
                setLoading(false);
            });
        }
    }, [productId, open]);

    const handleEdit = () => {
        onClose();
        navigate(`/adminpage/products/${productId}/edit`);
    };

    return (
        <StyledDialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <StyledDialogTitle>상품 상세 정보</StyledDialogTitle>
            <DialogContent dividers>
                {loading ? (
                    <Box display="flex" justifyContent="center" my={2}>
                        <CircularProgress />
                    </Box>
                ) : error ? (
                    <Typography color="error">Error: {error.message}</Typography>
                ) : product ? (
                    <Box sx={{ width: '100%', textAlign: 'center' }}>
                        <Typography variant="h5" fontWeight="bold" gutterBottom style={{ fontSize: '1.5rem' }}>
                            {product.name}
                        </Typography>
                        {product.categories && product.categories.length > 0 && (
                            <Typography variant="subtitle2" color="textSecondary" gutterBottom style={{ fontSize: '0.9rem' }}>
                                카테고리: {product.categories.join(', ')}
                            </Typography>
                        )}
                        {product.mainImageUrl && (
                            <ProductImage
                                src={product.mainImageUrl}
                                alt={product.name}
                            />
                        )}
                        <Typography variant="body1" fontWeight="bold" style={{ fontSize: '1.1rem' }}>
                            가격: {product.price}원
                        </Typography>
                        <Typography variant="body1" fontWeight="bold" style={{ fontSize: '1.1rem' }}>
                            재고: {product.stock}개
                        </Typography>
                        <Typography variant="body2" mt={2} style={{ fontSize: '1rem', textAlign: 'left' }}>
                            상품 내용: {product.description}
                        </Typography>
                    </Box>
                ) : (
                    <Typography>상품 정보를 불러오는 중입니다...</Typography>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={handleEdit} color="primary" variant="contained">
                    수정
                </Button>
                <Button onClick={onClose} color="secondary" variant="outlined">
                    취소
                </Button>
            </DialogActions>
        </StyledDialog>
    );
};

export default ViewProduct;
