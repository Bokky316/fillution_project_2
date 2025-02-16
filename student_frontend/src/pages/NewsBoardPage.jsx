import React, { useEffect, useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
    Button, Typography, Box, Snackbar, Dialog, DialogActions,
    DialogContent, DialogContentText, DialogTitle, IconButton,
    useTheme, useMediaQuery, Paper, Pagination
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import {
    fetchNewsPosts,
    setCurrentPage,
    deleteNewsPost
} from '@/store/newsSlice';

function NewsBoardPage() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

    const [snackbarOpen, setSnackbarOpen] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [postToDelete, setPostToDelete] = useState(null);

    const {
        posts = [], // 기본값을 빈 배열로 설정
        loading,
        error,
        currentPage,
        totalPages,
        deleteError
    } = useSelector(state => state.news || {}); // news 상태가 없을 경우를 대비한 기본값 설정

    const auth = useSelector((state) => state.auth);
    const userRole = auth?.user?.authorities?.some(auth =>
        typeof auth === 'string' ? auth === 'ROLE_ADMIN' : auth.authority === 'ROLE_ADMIN'
    ) ? 'ADMIN' : 'USER';

    useEffect(() => {
        console.log("Auth state:", auth);
        console.log("User authorities:", auth?.user?.authorities);
        const isAdmin = auth?.user?.authorities?.some(auth =>
            typeof auth === 'string' ? auth === 'ROLE_ADMIN' : auth.authority === 'ROLE_ADMIN'
        );
        console.log("Is admin:", isAdmin);
    }, [auth]);

    useEffect(() => {
        dispatch(fetchNewsPosts({ page: currentPage }));
    }, [dispatch, currentPage]);

    const handlePageChange = (event, value) => {
        dispatch(setCurrentPage(value - 1));
    };

    const handleEditPost = (postId) => {
        navigate(`/post/${postId}/edit`);
    };

    const handleDeleteClick = (postId) => {
        console.log("Delete button clicked for post:", postId);
        console.log("Current dialog state:", deleteDialogOpen);
        setPostToDelete(postId);
        setDeleteDialogOpen(true);
        console.log("Dialog state after set:", deleteDialogOpen);
    };

    const handleDeleteConfirm = async () => {
        if (postToDelete) {
            try {
                await dispatch(deleteNewsPost(postToDelete)).unwrap();
                setSnackbarMessage("게시글이 성공적으로 삭제되었습니다.");
                setSnackbarOpen(true);
            } catch (error) {
                setSnackbarMessage(error.message || "게시글 삭제 중 오류가 발생했습니다.");
                setSnackbarOpen(true);
            }
        }
        setDeleteDialogOpen(false);
        setPostToDelete(null);
    };

    const handleDeleteCancel = () => {
        setDeleteDialogOpen(false);
        setPostToDelete(null);
    };

    const handleCloseSnackbar = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setSnackbarOpen(false);
    };

     const formatDate = (dateString, isAdmin) => {
        if (!dateString) return '';
        const date = new Date(dateString);

        // 관리자일 경우 연도와 월·일을 분리해서 표시
        if (isAdmin) {
            return (
                <>
                    <Typography sx={{ fontSize: "0.8rem", color: "#666", lineHeight: 1 }}>
                        {date.getFullYear()}
                    </Typography>
                    <Typography sx={{ fontSize: "0.8rem", color: "#666", lineHeight: 1 }}>
                        {String(date.getMonth() + 1).padStart(2, '0')}.{String(date.getDate()).padStart(2, '0')}
                    </Typography>
                </>
            );
        }

        // 일반 사용자일 경우 YYYY.MM.DD 형식 유지
        return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
    };


    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
                <Typography>로딩 중...</Typography>
            </Box>
        );
    }

    if (error) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
                <Typography color="error">{error}</Typography>
            </Box>
        );
    }

    return (
            <Box
                maxWidth="lg"
                mx="auto"
                p={{ xs: 2, sm: 3, md: 4 }}
                mb={5}
                sx={{ overflowX: 'hidden' }}
            >
                {userRole === 'ADMIN' && (
                    <Box display="flex" justifyContent="flex-end" mb={2}>
                        <Button
                            variant="contained"
                            sx={{ backgroundColor: '#4169E1', color: 'white', fontSize: { xs: '0.8rem', sm: '0.875rem' } }}
                            onClick={() => navigate('/post/create', { state: { defaultCategory: '공지사항' } })}
                        >
                            게시글 등록
                        </Button>
                    </Box>
                )}
                <TableContainer sx={{ width: '100%', overflowX: 'hidden' }}>
                    <Table sx={{
                        borderLeft: 'none',
                        borderRight: 'none',
                        width: '100%',
                        tableLayout: 'fixed',
                        minWidth: '100%',
                    }}>
                        <TableHead>
                            <TableRow sx={{ borderTop: '2px solid #888' }}>
                                <TableCell align="center" sx={{ width: userRole === 'ADMIN' ? '25%' : '20%', fontWeight: 'bold' }}>분류</TableCell>
                                <TableCell align="left" sx={{ width: userRole === 'ADMIN' ? '75%' : '75%', fontWeight: 'bold' }}>제목</TableCell>
                                {userRole === 'ADMIN' && <TableCell align="center" sx={{ width: '20%', fontWeight: 'bold' }}>관리</TableCell>}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {Array.isArray(posts) && posts.length > 0 ? (
                                posts.map(post => (
                                    <TableRow
                                        key={post.id}
                                        sx={{
                                            height: 'auto',
                                            '&:hover': { cursor: 'pointer' }
                                        }}
                                        onClick={() => navigate(`/post/${post.id}`)}
                                    >
                                        <TableCell align="center" sx={{ width: userRole === 'ADMIN' ? '15%' : '15%', padding: '25px 0px' }}>
                                            <Typography variant="body1" sx={{ fontSize: '13px' }}>
                                                {post.category}
                                            </Typography>
                                        </TableCell>
                                        <TableCell align="left" sx={{ width: userRole === 'ADMIN' ? '70%' : '85%', padding: 1 }}>
                                            <Typography fontWeight="bold" variant="body1" sx={{ fontSize: '14px' }}>
                                                {post.title}
                                            </Typography>
                                            <Typography variant="body2" sx={{ display: 'block', color: 'grey', marginTop: '4px', fontSize: '12px' }}>
                                                {new Date(post.createdAt).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\. /g, '.').slice(0, -1)}
                                            </Typography>
                                        </TableCell>
                                        {userRole === 'ADMIN' && (
                                            <TableCell align="center" sx={{ width: '15%', padding: 1 }}>
                                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, alignItems: 'center' }}>
                                                    <Button
                                                        variant="outlined"
                                                        sx={{ borderColor: '#29B6F6', color: '#29B6F6' }}
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            handleEditPost(post.id);
                                                        }}
                                                    >
                                                        수정
                                                    </Button>
                                                    <Button
                                                        variant="outlined"
                                                        sx={{ borderColor: '#EF5503', color: '#EF5503' }}
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            handleDeleteClick(post.id);
                                                        }}
                                                    >
                                                        삭제
                                                    </Button>
                                                </Box>
                                            </TableCell>
                                        )}
                                    </TableRow>
                                ))
                            ) : (
                                <TableRow>
                                    <TableCell colSpan={userRole === 'ADMIN' ? 3 : 2} align="center">게시글이 없습니다.</TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>

                <Snackbar
                    anchorOrigin={{
                        vertical: 'bottom',
                        horizontal: 'left',
                    }}
                    open={snackbarOpen}
                    autoHideDuration={3000}
                    onClose={handleCloseSnackbar}
                    message={snackbarMessage}
                    action={
                        <IconButton size="small" aria-label="close" color="inherit" onClick={handleCloseSnackbar}>
                            <CloseIcon fontSize="small" />
                        </IconButton>
                    }
                />
                <Dialog
                    open={deleteDialogOpen}
                    onClose={handleDeleteCancel}
                    aria-labelledby="alert-dialog-title"
                    aria-describedby="alert-dialog-description"
                >
                    <DialogTitle id="alert-dialog-title">
                        게시글 삭제
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id="alert-dialog-description">
                            이 게시글을 삭제하시겠습니까?
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleDeleteConfirm} sx={{ color: '#EF5350' }} autoFocus>
                            삭제
                        </Button>
                        <Button onClick={handleDeleteCancel} sx={{ color: '#29B6F6' }}>
                            취소
                        </Button>
                    </DialogActions>
                </Dialog>
            </Box>
    );


}

export default NewsBoardPage;
