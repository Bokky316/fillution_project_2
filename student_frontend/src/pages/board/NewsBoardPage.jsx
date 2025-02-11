import React, { useEffect, useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Button,
    Typography,
    Box,
    Snackbar,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    IconButton,
    useTheme,
    useMediaQuery,
    Paper,
    Pagination
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import {
    fetchNewsPosts,
    setCurrentPage,
    deleteNewsPost
} from '../../redux/newsSlice';

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
        posts,
        loading,
        error,
        currentPage,
        totalPages,
        deleteError
    } = useSelector(state => state.news);

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

    const formatDate = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
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
            mb={18}
            sx={{ overflowX: 'hidden' }}
        >
            <Typography
                variant="h4"
                align="center"
                gutterBottom
                sx={{ fontSize: { xs: '1.5rem', sm: '2rem', md: '2.125rem' } }}
            >
                공지사항
            </Typography>
            {userRole === 'ADMIN' && (
                <Box display="flex" justifyContent="flex-end" mb={2}>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={() => navigate('/post/create', { state: { defaultCategory: '공지사항' } })}
                        sx={{ fontSize: { xs: '0.8rem', sm: '0.875rem' } }}
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
                            <TableCell
                                align="center"
                                sx={{
                                    width: '15%',
                                    fontWeight: 'bold',
                                    padding: { xs: 1, sm: 2 },
                                    fontSize: { xs: '0.8rem', sm: '0.875rem' }
                                }}
                            >
                                분류
                            </TableCell>
                            <TableCell
                                align="center"
                                sx={{
                                    fontWeight: 'bold',
                                    padding: { xs: 1, sm: 2 },
                                    fontSize: { xs: '0.8rem', sm: '0.875rem' },
                                    whiteSpace: 'nowrap',
                                    overflow: 'hidden',
                                    textOverflow: 'ellipsis',
                                    width: '60%'
                                }}
                            >
                                제목
                            </TableCell>
                            <TableCell
                                align="center"
                                sx={{
                                    fontWeight: 'bold',
                                    padding: { xs: 1, sm: 2 },
                                    fontSize: { xs: '0.8rem', sm: '0.875rem' },
                                    display: { xs: 'none', sm: 'table-cell' },
                                    width: '15%'
                                }}
                            >
                                작성일
                            </TableCell>
                            {userRole === 'ADMIN' && (
                                <TableCell
                                    align="center"
                                    sx={{
                                        fontWeight: 'bold',
                                        padding: { xs: 1, sm: 2 },
                                        fontSize: { xs: '0.8rem', sm: '0.875rem' },
                                        width: '10%'
                                    }}
                                >
                                    관리
                                </TableCell>
                            )}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {posts.length > 0 ? (
                            posts.map(post => (
                                <TableRow key={post.id}>
                                    <TableCell
                                        align="center"
                                        sx={{
                                            width: '15%',
                                            padding: { xs: '8px 4px', sm: 2 }
                                        }}
                                    >
                                        <Typography
                                            sx={{
                                                display: "inline-block",
                                                backgroundColor: "primary.main",
                                                color: "white",
                                                borderRadius: "20px",
                                                padding: { xs: '2px 6px', sm: '2px 10px' },
                                                fontSize: { xs: '0.65rem', sm: '0.75rem' },
                                                fontWeight: "bold",
                                                whiteSpace: 'nowrap'
                                            }}
                                        >
                                            {post.category}
                                        </Typography>
                                    </TableCell>
                                    <TableCell
                                        align="left"
                                        sx={{
                                            padding: { xs: 1, sm: 2 },
                                            fontSize: { xs: '0.8rem', sm: '0.875rem' },
                                            whiteSpace: 'nowrap',
                                            overflow: 'hidden',
                                            textOverflow: 'ellipsis'
                                        }}
                                    >
                                        <Link to={`/post/${post.id}`} style={{ textDecoration: 'none', color: 'black' }}>
                                            {post.title}
                                        </Link>
                                    </TableCell>
                                    <TableCell
                                        align="center"
                                        sx={{
                                            color: '#666',
                                            padding: { xs: 1, sm: 2 },
                                            display: { xs: 'none', sm: 'table-cell' },
                                            fontSize: { xs: '0.8rem', sm: '0.875rem' }
                                        }}
                                    >
                                        {formatDate(post.createdAt)}
                                    </TableCell>
                                    {userRole === 'ADMIN' && (
                                        <TableCell align="center" sx={{ padding: { xs: 1, sm: 2 } }}>
                                            <Box sx={{
                                                display: 'flex',
                                                flexDirection: 'column',
                                                gap: 1,
                                                alignItems: 'center'
                                            }}>
                                                <Button variant="outlined" color="primary" onClick={() => handleEditPost(post.id)}>
                                                    수정
                                                </Button>
                                                <Button variant="outlined" color="error" onClick={() => handleDeleteClick(post.id)}>
                                                    삭제
                                                </Button>
                                            </Box>
                                        </TableCell>
                                    )}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell colSpan={userRole === 'ADMIN' ? 4 : 3} align="center">
                                    게시글이 없습니다.
                                </TableCell>
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
                    <Button onClick={handleDeleteConfirm} color="error" autoFocus>
                        삭제
                    </Button>
                    <Button onClick={handleDeleteCancel}>취소</Button>
                </DialogActions>
            </Dialog>
        </Box>
    );


}

export default NewsBoardPage;
