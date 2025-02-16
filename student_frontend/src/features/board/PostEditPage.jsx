import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Box, FormControl, InputLabel, Select, MenuItem, TextField,
    Dialog, DialogTitle, DialogContent, DialogContentText,
    DialogActions, Button, Typography, Snackbar, IconButton
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import {
    fetchPost, updatePost, setFormData,
    setOpenCancelDialog, setOpenEditDialog
} from '@/store/postEditSlice';


const faqCategories = ["제품", "회원정보", "주문/결제", "교환/반품", "배송", "기타"];

function PostEditPage() {
    const { postId } = useParams();
    const navigate = useNavigate();
    const dispatch = useDispatch();

    const {
        formData,
        loading,
        error,
        openCancelDialog,
        openEditDialog
    } = useSelector((state) => state.postEdit);
    const auth = useSelector((state) => state.auth);

    const userRole = auth?.user?.authorities?.some(auth => auth.authority === "ROLE_ADMIN") ? "ADMIN" : "USER";

    const [snackbarOpen, setSnackbarOpen] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState("");

    useEffect(() => {
        dispatch(fetchPost(postId));
    }, [dispatch, postId]);

    useEffect(() => {
        if (auth?.user) {
            localStorage.setItem("auth", JSON.stringify(auth));
        }
    }, [auth]);

    useEffect(() => {
        if (userRole !== "ADMIN") {
            alert('관리자만 접근할 수 있습니다.');
            navigate('/board');
        }
    }, [userRole, navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        dispatch(setFormData({ [name]: value }));
    };

    const handleCancelClick = () => {
        dispatch(setOpenCancelDialog(true));
    };

    const handleCloseCancelDialog = () => {
        dispatch(setOpenCancelDialog(false));
    };

    const handleConfirmCancel = () => {
        dispatch(setOpenCancelDialog(false));
        navigate(-1);
    };

    const handleCloseEditDialog = () => {
        dispatch(setOpenEditDialog(false));
    };

    const handleCloseSnackbar = () => {
        setSnackbarOpen(false);
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!formData.title.trim() || !formData.content.trim()) {
            alert('제목과 내용을 모두 입력해주세요.');
            return;
        }

        if (formData.category === '자주 묻는 질문' && !formData.subCategory) {
            alert('FAQ 카테고리를 선택해주세요.');
            return;
        }

        dispatch(setOpenEditDialog(true));
    };

    const handleConfirmEdit = async () => {
        try {
            const boardId = formData.category === '공지사항' ? 1 : 2;
            const finalCategory = formData.category === '자주 묻는 질문'
                ? formData.subCategory
                : formData.category;

            const updateData = {
                id: postId,
                title: formData.title,
                content: formData.content,
                boardId: boardId,
                category: finalCategory,
                authorId: auth.user.id
            };

            const token = auth.user.token;

            await dispatch(updatePost({ postId, updateData, token })).unwrap();

            setSnackbarMessage("게시물이 성공적으로 수정되었습니다.");
            setSnackbarOpen(true);

            setTimeout(() => {
                setSnackbarOpen(false);
                navigate('/board');
            }, 1000);
        } catch (err) {
            if (err.status === 401 || err.status === 403) {
                alert('관리자 권한이 필요하거나 로그인이 필요합니다.');
                navigate('/login');
            } else {
                alert(`수정에 실패했습니다. 오류: ${err.message || '알 수 없는 오류'}`);
            }
        }
        dispatch(setOpenEditDialog(false));
    };

    if (loading) return <Typography align="center" variant="h6">로딩 중...</Typography>;
    if (error) return <Typography align="center" sx={{ color: '#EF5350' }} variant="h6">{error}</Typography>;

    return (
        <Box maxWidth="md" mx="auto" p={3}>
            <Typography variant="h4" align="center" gutterBottom>
                게시글 수정
            </Typography>

            <Box component="form" onSubmit={handleSubmit}>
                <Box mb={3}>
                    <FormControl fullWidth variant="outlined">
                        <InputLabel>게시판</InputLabel>
                        <Select
                            name="category"
                            value={formData.category}
                            disabled={true}
                            label="게시판"
                        >
                            <MenuItem value="공지사항">공지사항</MenuItem>
                            <MenuItem value="자주 묻는 질문">자주 묻는 질문</MenuItem>
                        </Select>
                    </FormControl>
                </Box>

                {formData.category === "자주 묻는 질문" && (
                    <Box mb={3}>
                        <FormControl fullWidth variant="outlined">
                            <InputLabel>카테고리 선택</InputLabel>
                            <Select
                                name="subCategory"
                                value={formData.subCategory}
                                onChange={handleChange}
                                label="카테고리 선택"
                            >
                                {faqCategories.map((category) => (
                                    <MenuItem key={category} value={category}>
                                        {category}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Box>
                )}

                <Box mb={3}>
                    <TextField
                        fullWidth
                        label="제목"
                        name="title"
                        variant="outlined"
                        value={formData.title}
                        onChange={handleChange}
                        required
                    />
                </Box>

                <Box mb={3}>
                    <TextField
                        fullWidth
                        label="내용"
                        name="content"
                        variant="outlined"
                        multiline
                        rows={6}
                        value={formData.content}
                        onChange={handleChange}
                        required
                    />
                </Box>

                <Box display="flex" justifyContent="flex-end" gap={1}>
                    <Button variant="contained" sx={{ backgroundColor: '#29B6F6', color: '#fff' }} type="submit">
                        수정하기
                    </Button>
                    <Button variant="contained" sx={{ backgroundColor: '#EF5350', color: '#fff' }} onClick={handleCancelClick}>
                        취소
                    </Button>
                </Box>
            </Box>

            <Dialog open={openCancelDialog} onClose={handleCloseCancelDialog}>
                <DialogTitle>취소 확인</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        작성 중인 내용이 저장되지 않습니다. 취소하시겠습니까?
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleConfirmCancel} sx={{ color: '#29B6F6' }}>
                        네
                    </Button>
                    <Button onClick={handleCloseCancelDialog} sx={{ color: '#EF5350' }}>
                        아니요
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={openEditDialog} onClose={handleCloseEditDialog}>
                <DialogTitle>수정 확인</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        게시글을 수정하시겠습니까?
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleConfirmEdit} sx={{ color: '#29B6F6' }}>
                        네
                    </Button>
                    <Button onClick={handleCloseEditDialog} sx={{ color: '#EF5350' }}>
                        아니요
                    </Button>
                </DialogActions>
            </Dialog>

            <Snackbar
                anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
                open={snackbarOpen}
                message={snackbarMessage}
                autoHideDuration={1000}
                onClose={handleCloseSnackbar}
                action={
                    <IconButton size="small" color="inherit" onClick={handleCloseSnackbar}>
                        <CloseIcon fontSize="small" />
                    </IconButton>
                }
            />
        </Box>
    );
}

export default PostEditPage;