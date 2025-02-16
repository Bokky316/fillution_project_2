import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useLocation } from 'react-router-dom';
import {
    Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, Button, Typography, Box,
    Snackbar, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle,
    IconButton, useTheme, useMediaQuery
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import {
    fetchFAQPosts,
    deleteFAQPost,
    setSelectedCategory
} from "@/store/faqSlice";

// FAQ 게시글의 카테고리 목록
const categories = ["전체", "제품", "회원정보", "주문/결제", "교환/반품", "배송", "기타"];

function FAQBoardPage() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const location = useLocation();
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('sm')); // 화면 크기가 작을 경우 true

    const {
        filteredPosts, // 필터링된 게시글 목록
        selectedCategory, // 선택된 카테고리
        loading, // 로딩 상태
        error // 에러 메시지
    } = useSelector((state) => state.faq);
    const auth = useSelector((state) => state.auth); // 인증 정보

    const [expandedPostId, setExpandedPostId] = useState(null); // 확장된 게시글 ID
    const [openDeleteDialog, setOpenDeleteDialog] = useState(false); // 삭제 확인 다이얼로그 열림/닫힘 상태
    const [postToDelete, setPostToDelete] = useState(null); // 삭제할 게시글 ID
    const [snackbar, setSnackbar] = useState({ open: false, message: '' }); // 스낵바 상태

    // 사용자의 역할이 ADMIN인지 확인
    const userRole = auth?.user?.authorities?.some(auth => auth.authority === "ROLE_ADMIN") ? "ADMIN" : "USER";

    useEffect(() => {
        // 컴포넌트가 처음 마운트될 때 "전체" 카테고리를 선택하고 게시글을 불러옴
        dispatch(setSelectedCategory("전체"));
        dispatch(fetchFAQPosts());
    }, [dispatch, location.key]);

    useEffect(() => {
        // 인증 정보가 변경될 때 로컬 스토리지에 저장
        if (auth?.user) {
            localStorage.setItem("auth", JSON.stringify(auth));
        }
    }, [auth]);

    useEffect(() => {
        // 새 게시글 등록 후 스낵바 메시지 표시
        if (location.state?.newPost) {
            setSnackbar({ open: true, message: "새 게시글이 등록되었습니다." });
        }
    }, [location.state]);

    // 로딩 중일 경우 "로딩 중..." 메시지 표시
    if (loading) return <Typography align="center" variant="h6">로딩 중...</Typography>;
    // 에러가 발생했을 경우 에러 메시지 표시
    if (error) return <Typography align="center" sx={{ color: '#EF5503' }} variant="h6">{error}</Typography>;

    // 게시글 클릭 시 게시글 내용 확장/축소
    const handlePostClick = (postId) => {
        setExpandedPostId(prevPostId => (prevPostId === postId ? null : postId));
    };

    // 게시글 수정 페이지로 이동
    const handleEditPost = (postId) => {
        navigate(`/faq/post/${postId}/edit`);
    };

    // 게시글 삭제 확인 다이얼로그 열기
    const handleDeletePost = (postId) => {
        setPostToDelete(postId);
        setOpenDeleteDialog(true);
    };

    // 게시글 삭제 처리
    const handleConfirmDelete = async () => {
        if (postToDelete) {
            try {
                await dispatch(deleteFAQPost(postToDelete)).unwrap();
                setSnackbar({ open: true, message: "게시글이 성공적으로 삭제되었습니다." });
            } catch (err) {
                setSnackbar({ open: true, message: "게시글 삭제에 실패했습니다." });
            }
        }
        setOpenDeleteDialog(false);
        setPostToDelete(null);
    };

    // 게시글 삭제 확인 다이얼로그 닫기
    const handleCloseDeleteDialog = () => {
        setOpenDeleteDialog(false);
        setPostToDelete(null);
    };

    // 스낵바 닫기
    const handleCloseSnackbar = () => {
        setSnackbar({ ...snackbar, open: false });
    };

    return (
        <Box
            sx={{
                width: '100%',
                maxWidth: { xs: '100%', sm: '100%' },
                margin: '0 auto',
                padding: { xs: '0 8px', sm: '0 16px', md: '0 24px' },
                mb: { xs: 5, sm: 5 },
                boxSizing: 'border-box'
            }}
        >
            {/* 변경된 카테고리 표시 부분 */}
             <Box
                 sx={{
                     display: 'flex',
                     justifyContent: 'flex-start',
                     flexWrap: 'wrap',
                     gap: { xs: 0.3, sm: 0.5 },
                     mb: 3,
                     mx: { xs: -0.3, sm: 0 }
                 }}
             >
                 {categories.map(category => (
                     <Typography
                         key={category}
                         component="span"
                         onClick={() => dispatch(setSelectedCategory(category))}
                         sx={{
                             cursor: 'pointer',
                             margin: { xs: '0.5px', sm: '2px' },
                             fontSize: { xs: '0.75rem', sm: '0.85rem' },
                             padding: { xs: '2px 4px', sm: '3px 8px' },
                             minWidth: { xs: 'auto', sm: '50px' },
                             fontWeight: 'bold',
                             textDecoration: 'none',
                             color: selectedCategory === category ? '#4169E1' : 'grey',
                         }}
                     >
                         {category}
                     </Typography>
                 ))}
             </Box>
            {userRole === 'ADMIN' && (
                <Box
                    display="flex"
                    justifyContent="flex-end"
                    mb={2}
                    px={{ xs: 1, sm: 2 }}
                >
                    <Button
                        variant="contained"
                        size={isMobile ? "small" : "medium"}
                        onClick={() => navigate('/post/create', {
                            state: { defaultCategory: '자주 묻는 질문' }
                        })}
                        sx={{
                            backgroundColor: '#4169E1',
                            color: 'white',
                            fontSize: { xs: '0.8rem', sm: '0.875rem' }
                        }}
                    >
                        게시글 등록
                    </Button>
                </Box>
            )}
            <TableContainer
                sx={{
                    overflowX: 'auto',
                    width: '100%',
                    '& .MuiTable-root': {
                        width: '100%',
                        tableLayout: 'fixed'
                    }
                }}
            >
                <Table>
                    <TableHead>
                        <TableRow sx={{ borderTop: '2px solid #888'}}>
                            <TableCell
                                align="center"
                                sx={{
                                    width: { xs: '25%', sm: '20%' },
                                    maxWidth: { xs: '80px', sm: '100px' },
                                    fontWeight: 'bold',
                                    padding: { xs: 1, sm: 2 },
                                    fontSize: { xs: '0.8rem', sm: '0.875rem' }
                                }}
                            >
                                분류
                            </TableCell>
                            <TableCell
                                align="left"
                                sx={{
                                    width: userRole === 'ADMIN' ? { xs: '75%', sm: '60%' } : '75%',
                                    fontWeight: 'bold',
                                    padding: { xs: 1, sm: 2 },
                                    fontSize: { xs: '0.8rem', sm: '0.875rem' }
                                }}
                            >
                                제목
                            </TableCell>
                            {!isMobile && userRole === 'ADMIN' && (
                                <TableCell
                                    align="center"
                                    sx={{
                                        width: { sm: '20%' },
                                        maxWidth: { sm: '160px' },
                                        fontWeight: 'bold',
                                        fontSize: { xs: '0.8rem', sm: '0.875rem' }
                                    }}
                                >
                                    관리
                                </TableCell>
                            )}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {Array.isArray(filteredPosts) && filteredPosts.length > 0 ? (
                            [...filteredPosts].reverse().map(post => (
                                <React.Fragment key={post.id}>
                                    <TableRow>
                                        <TableCell
                                            align="center"
                                            sx={{
                                                padding: { xs: 1, sm: 2 },
                                                fontSize: { xs: '13px', sm: '13px' }
                                            }}
                                        >
                                            {post.category}
                                        </TableCell>
                                        <TableCell
                                            sx={{
                                                padding: { xs: 1, sm: 2 },
                                                fontSize: { xs: '0.8rem', sm: '0.875rem' }
                                            }}
                                        >
                                            <Box sx={{
                                                display: 'flex',
                                                justifyContent: 'space-between',
                                                alignItems: 'center',
                                                gap: 1
                                            }}>
                                                <Typography
                                                    onClick={() => handlePostClick(post.id)}
                                                    sx={{
                                                        cursor: "pointer",
                                                        fontSize: { xs: '0.8rem', sm: '0.875rem' },
                                                        fontWeight: 'bold', // 제목 bold 처리
                                                        overflow: 'hidden',
                                                        textOverflow: 'ellipsis',
                                                        maxWidth: isMobile ? 'calc(100% - 60px)' : 'none',
                                                        wordBreak: 'break-word',  // 긴 단어 줄바꿈
                                                        display: '-webkit-box', // 추가
                                                        WebkitLineClamp: 2,       // 최대 2줄
                                                        WebkitBoxOrient: 'vertical' // 추가
                                                    }}
                                                >
                                                    {post.title}
                                                </Typography>
                                                {isMobile && userRole === 'ADMIN' && (
                                                    <Box sx={{
                                                        display: 'flex',
                                                        flexDirection: 'column',
                                                        gap: 0.5,
                                                        flexShrink: 0
                                                    }}>
                                                        <Button
                                                            variant="outlined"
                                                            sx={{
                                                                borderColor: '#29B6F6',
                                                                color: '#29B6F6',
                                                                fontSize: '0.7rem',
                                                                minWidth: 'auto',
                                                                padding: '2px 8px'
                                                            }}
                                                            size="small"
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                handleEditPost(post.id);
                                                            }}
                                                        >
                                                            수정
                                                        </Button>
                                                        <Button
                                                            variant="outlined"
                                                            sx={{
                                                                borderColor: '#EF5350',
                                                                color: '#EF5350',
                                                                fontSize: '0.7rem',
                                                                minWidth: 'auto',
                                                                padding: '2px 8px'
                                                            }}
                                                            size="small"
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                handleDeletePost(post.id);
                                                            }}
                                                        >
                                                            삭제
                                                        </Button>
                                                    </Box>
                                                )}
                                            </Box>
                                        </TableCell>
                                        {!isMobile && userRole === 'ADMIN' && (
                                            <TableCell align="center">
                                                <Box sx={{
                                                    display: 'flex',
                                                    flexDirection: 'column',
                                                    alignItems: 'center',
                                                    gap: 1
                                                }}>
                                                    <Button
                                                        variant="outlined"
                                                        sx={{
                                                            borderColor: '#29B6F6',
                                                            color: '#29B6F6',
                                                        }}
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            handleEditPost(post.id);
                                                        }}
                                                    >
                                                        수정
                                                    </Button>
                                                    <Button
                                                        variant="outlined"
                                                        sx={{
                                                            borderColor: '#EF5350',
                                                            color: '#EF5350',
                                                        }}
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            handleDeletePost(post.id);
                                                        }}
                                                    >
                                                        삭제
                                                    </Button>
                                                </Box>
                                            </TableCell>
                                        )}
                                    </TableRow>
                                    {/* 게시글 내용 표시 (확장된 경우) */}
                                    {expandedPostId === post.id && (
                                        <TableRow>
                                            <TableCell colSpan={3}>
                                                <Typography
                                                    sx={{
                                                        whiteSpace: 'pre-line', // 이 부분을 추가
                                                        fontSize: { xs: '0.8rem', sm: '0.875rem' },
                                                        padding: { xs: 1, sm: 2 }
                                                    }}
                                                >
                                                    {post.content}
                                                </Typography>
                                            </TableCell>
                                        </TableRow>
                                    )}
                                </React.Fragment>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell align="center" colSpan={4}>
                                    게시글이 없습니다.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </TableContainer>

            {/* 삭제 확인 다이얼로그 */}
            <Dialog
                open={openDeleteDialog}
                onClose={handleCloseDeleteDialog}
                aria-labelledby="alert-dialog-title"
                aria-describedby="alert-dialog-description"
            >
                <DialogTitle id="alert-dialog-title">{"게시글 삭제 확인"}</DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        정말로 이 게시글을 삭제하시겠습니까?
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleConfirmDelete} sx={{ color: '#EF5350' }} autoFocus>
                        삭제
                    </Button>
                    <Button onClick={handleCloseDeleteDialog} sx={{ color: '#29B6F6' }}>
                        취소
                    </Button>
                </DialogActions>
            </Dialog>

            {/* 스낵바 */}
            <Snackbar
                open={snackbar.open}
                autoHideDuration={3000}
                onClose={handleCloseSnackbar}
                message={snackbar.message}
                action={
                    <IconButton size="small" aria-label="close" color="inherit" onClick={handleCloseSnackbar}>
                        <CloseIcon fontSize="small" />
                    </IconButton>
                }
            />
        </Box>
    );
}

export default FAQBoardPage;
