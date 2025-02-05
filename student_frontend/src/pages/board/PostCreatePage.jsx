import React, { useEffect } from "react";
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useLocation } from "react-router-dom";
import {
    Box,
    Button,
    TextField,
    Typography,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Select,
    MenuItem,
    InputLabel,
    FormControl,
} from "@mui/material";
import {
    createPost,
    setFormData,
    setIsAdmin,
    setAuthorId,
    setOpenCancelDialog,
    setOpenSubmitDialog,
} from '../../redux/postCreateSlice';

// FAQ 카테고리 목록
const faqCategories = ["전체", "제품", "회원정보", "주문/결제", "교환/반품", "배송", "기타"];

function PostCreatePage() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const location = useLocation();

    // Redux 상태 가져오기
    const {
        formData,
        isAdmin,
        openCancelDialog,
        openSubmitDialog,
        loading,
        error
    } = useSelector((state) => state.postCreate);

    // 초기 카테고리 설정
    useEffect(() => {
        const defaultCategory = location.state?.defaultCategory || '공지사항';
        dispatch(setFormData({
            category: defaultCategory,
            boardId: defaultCategory === '공지사항' ? 1 : 2,
        }));
    }, [location.state, dispatch]);

    // 사용자 권한 확인
    useEffect(() => {
        const loggedInUser = localStorage.getItem("loggedInUser");
        if (loggedInUser) {
            try {
                const userData = JSON.parse(loggedInUser);
                dispatch(setIsAdmin(userData.authorities?.includes("ROLE_ADMIN") || false));
                dispatch(setAuthorId(userData.id || 1));
            } catch (e) {
                console.error("사용자 데이터 파싱 오류:", e);
                navigate("/login");
            }
        } else {
            navigate("/login");
        }
    }, [navigate, dispatch]);

    // 폼 제출 처리
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!formData.title.trim() || !formData.content.trim()) {
            alert("제목과 내용을 입력해주세요.");
            return;
        }

        if (formData.category === "자주 묻는 질문" && !formData.subCategory) {
            alert("FAQ 카테고리를 선택해주세요.");
            return;
        }

        dispatch(setOpenSubmitDialog(true));
    };

    // 게시글 등록 확인
    const handleConfirmSubmit = async () => {
        try {
            // API 요청을 위한 데이터 준비
            const finalCategory = formData.category === "자주 묻는 질문"
                ? formData.subCategory
                : formData.category;

            const postData = {
                title: formData.title,
                content: formData.content,
                boardId: formData.boardId,
                category: finalCategory,
                authorId: formData.authorId,
            };

            // 게시글 생성 요청
            await dispatch(createPost(postData)).unwrap();
            alert("게시물이 등록되었습니다.");
            navigate("/board");
        } catch (error) {
            if (error.status === 401 || error.status === 403) {
                alert("관리자 권한이 필요하거나 로그인이 필요합니다.");
                navigate("/login");
            } else {
                alert("게시물 등록에 실패했습니다.");
            }
        } finally {
            dispatch(setOpenSubmitDialog(false));
        }
    };

    // 입력 필드 변경 처리
    const handleChange = (e) => {
        const { name, value } = e.target;
        dispatch(setFormData({ [name]: value }));
    };

    // 취소 버튼 처리
    const handleCancelClick = () => {
        dispatch(setOpenCancelDialog(true));
    };

    // 취소 확인
    const handleConfirmCancel = () => {
        dispatch(setOpenCancelDialog(false));
        navigate("/board");
    };

    // 다이얼로그 닫기
    const handleCloseDialog = (type) => {
        if (type === 'cancel') {
            dispatch(setOpenCancelDialog(false));
        } else {
            dispatch(setOpenSubmitDialog(false));
        }
    };

    // 관리자가 아닌 경우 표시
    if (!isAdmin) {
        return (
            <Box maxWidth="md" mx="auto" p={3}>
                <Typography variant="h6" color="error" align="center">
                    관리자만 게시글을 등록할 수 있습니다.
                </Typography>
                <Box display="flex" justifyContent="center" mt={2}>
                    <Button variant="contained" color="primary" onClick={() => navigate("/board")}>
                        목록으로 돌아가기
                    </Button>
                </Box>
            </Box>
        );
    }

    return (
        <Box maxWidth="md" mx="auto" p={3}>
            <Typography variant="h4" align="center" gutterBottom>
                게시글 등록
            </Typography>

            <Box component="form" onSubmit={handleSubmit}>
                {/* 게시판 선택 */}
                <Box mb={3}>
                    <FormControl fullWidth variant="outlined">
                        <InputLabel>게시판 선택</InputLabel>
                        <Select
                            name="category"
                            value={formData.category}
                            onChange={handleChange}
                            label="게시판 선택"
                        >
                            <MenuItem value="공지사항">공지사항</MenuItem>
                            <MenuItem value="자주 묻는 질문">자주 묻는 질문</MenuItem>
                        </Select>
                    </FormControl>
                </Box>

                {/* FAQ 카테고리 선택 */}
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

                {/* 제목 입력 */}
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

                {/* 내용 입력 */}
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

                {/* 버튼 그룹 */}
                <Box display="flex" justifyContent="flex-end" gap={1}>
                    <Button variant="contained" color="primary" type="submit">
                        등록
                    </Button>
                    <Button variant="outlined" color="secondary" onClick={handleCancelClick}>
                        취소
                    </Button>
                </Box>
            </Box>

            {/* 취소 확인 다이얼로그 */}
            <Dialog open={openCancelDialog} onClose={() => handleCloseDialog('cancel')}>
                <DialogTitle>취소 확인</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        작성 중인 내용이 저장되지 않습니다. 취소하시겠습니까?
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleConfirmCancel} color="secondary">
                        네
                    </Button>
                    <Button onClick={() => handleCloseDialog('cancel')} color="primary">
                        아니요
                    </Button>
                </DialogActions>
            </Dialog>

            {/* 등록 확인 다이얼로그 */}
            <Dialog open={openSubmitDialog} onClose={() => handleCloseDialog('submit')}>
                <DialogTitle>등록 확인</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        게시글을 등록하시겠습니까?
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleConfirmSubmit} color="primary">
                        네
                    </Button>
                    <Button onClick={() => handleCloseDialog('submit')} color="secondary">
                        아니요
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
}

export default PostCreatePage;