import React, { useEffect } from 'react';
import { Box } from "@mui/material";
import { useLocation } from "react-router-dom";

const Layout = ({ children }) => {
    const location = useLocation();
    // 현재 페이지가 "/adminpage" 또는 "/adminpage/*" 경로인지 확인
    const isAdminPage = location.pathname.toLowerCase().startsWith("/adminpage"); // ✅ 대소문자 구분 제거
    // 홈 페이지 여부 확인
    const isHomePage = location.pathname === "/";

    // 모든 페이지에 가로 스크롤 비활성화 적용
    useEffect(() => {
        document.body.style.overflowX = "hidden"; // 모든 페이지에서 수평 스크롤 비활성화

        return () => {
            document.body.style.overflowX = "auto"; // 언마운트 시 원래대로 복구
        };
    }, []); // 의존성 배열이 비어있으므로 컴포넌트 마운트 시에만 실행


    return (
        <Box sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center', // 중앙 정렬
            minHeight: '100vh', // 화면 전체 높이
            backgroundColor: '#f9f9f9', // 배경색 설정 (선택 사항)
        }}>
            {/* 메인 컨텐츠 영역 */}
            <Box sx={{
                flexGrow: 1,
                width: '100%',
                maxWidth: isHomePage ? "100%" : isAdminPage ? '60%' : '480px', // 홈 페이지: 100%, 어드민: 50%, 그 외: 480px 모바일 중심 UI의 폭 (보통 360~480px)
                margin: '0 auto',
                padding: isHomePage ? 0 : '20px', // 홈 페이지에서만 padding 제거, 나머지는 '20px'
                paddingTop: isHomePage ? 0 : '80px', // 홈 페이지에서는 paddingTop 제거, 나머지는 '80px'
                backgroundColor: '#ffffff', // 컨텐츠 배경색 (선택 사항)
//                 boxShadow: isHomePage || isAdminPage ? "none" : "0px 4px 10px rgba(0, 0, 0, 0.1)", // 홈,어드민 페이지에서는 그림자 제거
                borderRadius: isHomePage || isAdminPage ? 0 : '8px', // 홈,어드민 페이지에서는 모서리 제거
            }}>
                {children}
            </Box>
        </Box>
    );
};

export default Layout;
