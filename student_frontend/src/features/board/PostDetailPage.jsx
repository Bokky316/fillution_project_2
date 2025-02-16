import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import {
    Typography, Button, Box, Divider,
    Table, TableBody, TableCell, TableRow, TableHead, TableContainer,
    Dialog, DialogTitle, DialogContent, DialogActions,
    Snackbar, IconButton
} from '@mui/material';
import CloseIcon from "@mui/icons-material/Close";
import {
    fetchPostDetail,
    deletePost,
    setIsAdmin,
    setSnackbarOpen,
    setSnackbarMessage
} from '@/store/postDetailSlice';

function PostDetailPage() {
    const { postId } = useParams();
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const [openDeleteDialog, setOpenDeleteDialog] = useState(false);

    // ✅ Redux 상태 연결
    const snackbarOpen = useSelector(state => state.postDetail.snackbarOpen);
    const snackbarMessage = useSelector(state => state.postDetail.snackbarMessage);

    const {
        post,
        prevPost,
        nextPost,
        loading,
        error,
        isAdmin
    } = useSelector(state => state.postDetail);

    const auth = useSelector((state) => state.auth);
    const userRole = auth?.user?.authorities?.some(auth => auth.authority === "ROLE_ADMIN") ? "ADMIN" : "USER";

    useEffect(() => {
        dispatch(fetchPostDetail(postId));

        // ✅ 컴포넌트가 unmount될 때 스낵바 상태 초기화
        return () => {
            dispatch(setSnackbarOpen(false));
            dispatch(setSnackbarMessage(""));
        };
    }, [dispatch, postId]);

    useEffect(() => {
        if (auth?.user) {
            localStorage.setItem("auth", JSON.stringify(auth));
        }
    }, [auth]);

    useEffect(() => {
        dispatch(setIsAdmin(userRole === "ADMIN"));
    }, [dispatch, userRole]);

    const handleDeleteConfirm = async () => {
        try {
            await dispatch(deletePost({ postId, token: auth.user.token })).unwrap();

            setOpenDeleteDialog(false);
            dispatch(setSnackbarMessage("게시글이 성공적으로 삭제되었습니다."));
            dispatch(setSnackbarOpen(true));

            setTimeout(() => {
                navigate('/board');
            }, 1000);

        } catch (err) {
            console.error('Error deleting post:', err);
            if (err.status === 401 || err.status === 403) {
                alert('관리자 권한이 필요하거나 로그인이 필요합니다.');
                navigate('/login');
            } else {
                alert('삭제에 실패했습니다.');
            }
        }
    };

    const handleCloseSnackbar = () => {
        dispatch(setSnackbarOpen(false));
    };

    const handleDeleteClick = () => {
        if (userRole !== "ADMIN") {
            alert('관리자만 삭제할 수 있습니다.');
            return;
        }
        setOpenDeleteDialog(true);
    };

    const handleEditPost = () => {
        if (userRole !== "ADMIN") {
            alert('관리자만 수정할 수 있습니다.');
            return;
        }
        navigate(`/post/${postId}/edit`);
    };

    if (loading) return <Typography>로딩 중...</Typography>;
    if (error) return <Typography color="error">오류 발생: {error.message}</Typography>;
    if (!post) return null;

    return (
        <Box maxWidth="md" mx="auto" p={4} pt={0} mb={5}>
            <Typography
                variant="h4"
                component="h1"
                gutterBottom
                align="left"
                sx={{
                    mt: 4,
                    mb: 3,
                    fontSize: { xs: '1.5rem', sm: '2rem', md: '2.125rem' }
                }}
            >
                필루션 소식
            </Typography>

            {/* 게시물 테이블 */}
            <TableContainer sx={{ width: '100%', mb: 4, px: 0 }}>
                <Table sx={{
                    borderLeft: 'none',
                    borderRight: 'none',
                    width: '100%',
                    tableLayout: 'fixed',
                    minWidth: '100%',
                }}>
                    <TableBody>
                        <TableRow sx={{
                            height: 'auto',
                            borderBottom: '1px solid #e0e0e0'
                        }}>
                            <TableCell sx={{
                                padding: '16px 0px',
                                border: 'none',
                            }}>
                                {/* 카테고리 */}
                                <Box
                                    sx={{
                                        display: 'inline-block',
                                        bgcolor: '#add8e6',
                                        borderRadius: '4px',
                                        px: 0.5,
                                        py: 0.5,
                                        mb: 0.5
                                    }}
                                >
                                    <Typography variant="body1" sx={{
                                        fontSize: '10px',
                                        color: '#fff'
                                    }}>
                                        {post.category}
                                    </Typography>
                                </Box>

                                {/* 제목 */}
                                <Typography
                                    fontWeight="bold"
                                    variant="h6"
                                    sx={{
                                        fontSize: '20px',
                                        mb: 0.5
                                    }}
                                >
                                    {post.title}
                                </Typography>

                                {/* 날짜 */}
                                <Typography
                                    variant="body2"
                                    sx={{
                                        display: 'block',
                                        color: 'grey',
                                        fontSize: '14px',
                                        mb: 0.5
                                    }}
                                >
                                    {new Date(post.createdAt).toLocaleDateString('ko-KR', {
                                        year: 'numeric',
                                        month: '2-digit',
                                        day: '2-digit'
                                    }).replace(/\. /g, '.').slice(0, -1)}
                                </Typography>
                            </TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            </TableContainer>

            {/* 게시물 내용 */}
            <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap', minHeight: '300px', textAlign: 'left', mb: 4 }}>
                {post.content}
            </Typography>

            {/* 이전글/다음글 네비게이션 */}
            <Table sx={{ borderCollapse: 'collapse' }}>
                <TableBody>
                    <TableRow
                        onClick={() => prevPost && navigate(`/post/${prevPost.id}`)}
                        style={{ cursor: prevPost ? 'pointer' : 'default' }}
                    >
                        <TableCell sx={{ pl: 2, fontWeight: 'bold', color: '#555', borderTop: '1px solid #ccc' }}>
                            ▲ <span style={{ fontWeight: 'bold', color: '#555' }}>이전 글:</span> {prevPost ? prevPost.title : '없음'}
                        </TableCell>
                    </TableRow>
                    <TableRow
                        onClick={() => nextPost && navigate(`/post/${nextPost.id}`)}
                        style={{ cursor: nextPost ? 'pointer' : 'default' }}
                    >
                        <TableCell sx={{ pl: 2, fontWeight: 'bold', color: '#555', borderTop: '1px solid #ccc' }}>
                            ▼ <span style={{ fontWeight: 'bold', color: '#555' }}>다음 글:</span> {nextPost ? nextPost.title : '없음'}
                        </TableCell>
                    </TableRow>
                </TableBody>
            </Table>

            {/* 버튼 그룹 */}
            <Box mt={5} mb={5} display="flex" justifyContent="center">
                <Button
                    variant="contained"
                    sx={{ backgroundColor: '#4169E1', color: 'white', mt: 2, px: 4, py: 1 }}
                    size="medium"
                    onClick={() => navigate('/board')}
                >
                    목록으로 가기
                </Button>
            </Box>

            {userRole === "ADMIN" && (
                <Box mt={2} display="flex" justifyContent="flex-end" gap={2}>
                    <Button variant="contained" sx={{ backgroundColor: '#29B6F6', color: 'white'}} onClick={handleEditPost}>수정</Button>
                    <Button variant="contained" sx={{ backgroundColor: '#EF5350', color: 'white'}} onClick={handleDeleteClick}>삭제</Button>
                </Box>
            )}

            {/* 삭제 확인 다이얼로그 */}
            <Dialog
                open={openDeleteDialog}
                onClose={() => setOpenDeleteDialog(false)}
                aria-labelledby="delete-dialog-title"
                aria-describedby="delete-dialog-description"
            >
                <DialogTitle id="delete-dialog-title">
                    게시글 삭제
                </DialogTitle>
                <DialogContent id="delete-dialog-description">
                    <Typography>
                        정말 이 게시글을 삭제하시겠습니까?
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleDeleteConfirm} sx={{ color: '#EF5350' }}>
                        삭제
                    </Button>
                    <Button onClick={() => setOpenDeleteDialog(false)} sx={{ color: '#29B6F6' }}>
                        취소
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Snackbar 추가 */}
            <Snackbar
                anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
                open={snackbarOpen} // Redux 상태 연결
                message={snackbarMessage} // Redux 상태 연결
                autoHideDuration={3000}
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

export default PostDetailPage;
