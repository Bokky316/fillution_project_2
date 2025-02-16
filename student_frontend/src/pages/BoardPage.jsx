import React, { useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useLocation } from "react-router-dom";
import { Container, Typography, Box, Button } from "@mui/material";
import NewsBoardPage from "@/pages/NewsBoardPage";
import FAQBoardPage from "@/pages/FAQBoardPage";
import { setCurrentBoard } from '@/store/boardSlice';

function BoardPage() {
    const dispatch = useDispatch();
    const currentBoard = useSelector(state => state.board.currentBoard);
    const location = useLocation();

    useEffect(() => {
        const isNewVisit = !sessionStorage.getItem('boardPageVisited');
        const isHeaderClick = sessionStorage.getItem('headerBoardClick') === 'true';

        if (isNewVisit || isHeaderClick) {
            dispatch(setCurrentBoard("news"));
            localStorage.setItem("currentBoard", "news");
            sessionStorage.setItem('boardPageVisited', 'true');
            sessionStorage.removeItem('headerBoardClick');
        } else {
            const savedBoard = localStorage.getItem("currentBoard") || "news";
            dispatch(setCurrentBoard(savedBoard));
        }

        return () => {
            sessionStorage.removeItem('boardPageVisited');
        };
    }, [location.key, dispatch]);

    const handleBoardChange = (board) => {
        dispatch(setCurrentBoard(board));
        localStorage.setItem("currentBoard", board);
    };

    const buttonStyle = (isSelected) => ({
        color: isSelected ? "#4169E1" : "text.secondary",
        px: 2,
        py: 0.5,
        fontSize: { xs: '0.9rem', sm: '1rem' },
        fontWeight: "bold",
        minWidth: 'auto',
        position: 'relative',
        borderRadius: 0,
        cursor: "pointer",
        '&:hover': {
            backgroundColor: "transparent",
        },
        '&::after': {
            content: '""',
            position: 'absolute',
            bottom: -1,
            left: 0,
            width: '100%',
            height: isSelected ? '2px' : '0px',
            backgroundColor: isSelected ? "#4169E1" : "transparent",
        },
    });

    return (
        <Container
            disableGutters
            sx={{
                overflowX: 'hidden',
                maxWidth: '100vw',
                boxSizing: 'border-box'
            }}
        >
            <Typography
                variant="h4"
                component="h1"
                gutterBottom
                align="left"
                sx={{
                    my: 5,
                    fontSize: { xs: '1.5rem', sm: '2rem', md: '2.125rem' },
                    px: { xs: 2, sm: 3 },
                }}
            >
                필루션 소식
            </Typography>

            <Box sx={{
                display: 'flex',
                justifyContent: 'flex-start',
                mb: 1,
                ml: { xs: 2, sm: 3 },
            }}>
                <Button
                    onClick={() => handleBoardChange("news")}
                    sx={buttonStyle(currentBoard === "news")}
                >
                    공지사항
                </Button>
                <Button
                    onClick={() => handleBoardChange("faq")}
                    sx={buttonStyle(currentBoard === "faq")}
                >
                    자주 묻는 질문
                </Button>
            </Box>

            <Box sx={{ overflowX: 'hidden', width: '100%', paddingTop:"16px" }}>
                {currentBoard === "news" ? <NewsBoardPage /> : <FAQBoardPage />}
            </Box>
        </Container>
    );
}

export default BoardPage;
