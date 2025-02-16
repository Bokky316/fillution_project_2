import React from "react";
import { Box, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import "@/styles/HomePage.css";

const HomePage = () => {
    const navigate = useNavigate();


    return (
        <Box className="home-page" sx={{ maxWidth: "100%", width: "100%", margin: "0 auto" }}>
            {/* Hero Section */}
            <Box
                sx={{
                    backgroundImage: 'url(/src/assets/images/slide.jpg)',  // 배경 이미지 경로 설정
                    backgroundSize: 'cover',  // 이미지가 영역을 꽉 채우도록 설정
                    backgroundPosition: '50% 37%',  // 이미지를 중앙에 배치
                    backgroundRepeat: 'no-repeat',  // 배경 이미지 반복 안 함
                    width: "100%",
                    height: "600px",
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "center",
                    alignItems: "center",
                    textAlign: "center",
                    gap: 4
                }}
            >
                <Box>
                    <Typography
                        variant="h4" // 글씨 크기 작게 조정
                        component="h1"
                        sx={{
                            fontWeight: 'bold',
                            mb: 1,
                            color: '#000', // 글씨 색상 흰색
                            letterSpacing: '-1px', // 글자 간격 좁게 설정
                        }}
                    >
                        맞춤 영양제
                    </Typography>
                    <Typography
                        variant="h4" // 글씨 크기 작게 조정
                        component="h1"
                        sx={{
                            fontWeight: 'bold',
                            color: '#000', // 글씨 색상 흰색
                            letterSpacing: '-1px', // 글자 간격 좁게 설정
                        }}
                    >
                        스마트하게 시작하세요!
                    </Typography>
                </Box>

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4, alignItems: 'center' }}> {/* 수평 중앙 정렬 */}
                    <Button
                        variant="contained"
                        size="large"
                        onClick={() => navigate('/survey')}
                        sx={{
                            backgroundColor: '#4169E1 ',
                            fontWeight: 'bold',
                            borderRadius: '50px',
                            width: '200px',
                            px: 4,
                            py: 2, // 위아래 패딩 키우기
                            letterSpacing: '-0.5px', // 글자 간격 좁게 설정
                        }}
                    >
                        영양제 추천받기
                    </Button>

                    <Button
                        variant="text"
                        size="medium" // 크기 작게 조정
                        onClick={() => navigate('/products')}
                        sx={{
                            color: '#4169E1',
                            textDecoration: 'none',
                            borderRadius: 0,
                            px: 0, // 좌우 패딩을 없앰
                            py: 0.5, // 상하 패딩을 줄임 (필요에 따라 조정)
                            borderBottom: '2px solid #4169E1', // 밑줄만 추가
                            fontSize: '0.75rem',
                            fontWeight: 'bold',
                            width: '120px', // 버튼의 고정 너비 설정 (필요에 따라 조정)
                            minWidth: 'unset', // Material UI 기본 최소 너비 해제
                            letterSpacing: '-0.5px', // 글자 간격 좁게 설정
                        }}
                    >
                        모든 제품 보기
                    </Button>
                </Box>
            </Box>

            {/* 통계 정보 */}
            <Box sx={{ maxWidth: "100%", width: "100%", margin: "0 auto" }}>
                <Box sx={{
                    backgroundColor: "#F5F5F5",
                    padding: "40px 20px",
                    height: "auto",
                    minHeight: "360px"
                }}>
                    <Box sx={{
                        maxWidth: "1200px", // 컨텐츠 사이즈
                        margin: "0 auto"
                    }}>

                        {/* 제목 */}
                        <Box sx={{ textAlign: "center", marginTop: "40px", marginBottom: "60px" }}>
                            <Typography variant="h5" sx={{ fontWeight: "bold", marginBottom: "3px", letterSpacing: "-1px" }}>
                                성공적인 영양제 습관
                            </Typography>
                            <Typography variant="h5" sx={{ fontWeight: "bold", letterSpacing: "-1px"}}>
                                많은 분들이 경험하고 있어요
                            </Typography>
                        </Box>

                        {/* 통계 항목들 */}
                        <Box sx={{
                            display: "flex",
                            justifyContent: "space-between",
                            flexWrap: "wrap",
                            gap: "20px"
                        }}>
                            {/* 제품 만족도 */}
                            <Box
                                sx={{
                                    flex: "1 1 250px",
                                    textAlign: "center",
                                    maxWidth: "280px",
                                    margin: "0 auto"
                                }}
                            >
                                <Typography
                                    variant="h3"
                                    sx={{ fontWeight: "bold", letterSpacing: "-1px" }}
                                >
                                    95
                                    <span style={{ fontSize: "0.6em" }}>%</span>
                                </Typography>
                                <Typography sx={{ marginBottom: "18px", fontSize: "13px", letterSpacing: "-1px" }}>
                                    제품 만족도
                                </Typography>
                                <Typography sx={{ fontSize: "14px", color: "#666", lineHeight: 1.5, letterSpacing: "-0.5px" }}>
                                    나에게 꼭 맞는 영양제를<br /> 추천해주기에 만족하고 있어요!
                                </Typography>
                            </Box>

                            {/* 건강습관 형성 */}
                            <Box sx={{
                                flex: "1 1 250px",
                                textAlign: "center",
                                maxWidth: "280px",
                                margin: "0 auto"
                            }}>
                                <Typography variant="h3" sx={{ fontWeight: "bold", letterSpacing: "-1px" }}>
                                    94
                                    <span style={{ fontSize: "0.6em" }}>%</span>
                                </Typography>
                                <Typography sx={{ marginBottom: "18px", fontSize: "13px", letterSpacing: "-1px" }}>
                                    건강습관 형성
                                </Typography>
                                <Typography sx={{ fontSize: "14px", color: "#666", lineHeight: 1.5, letterSpacing: "-0.5px" }}>
                                    꾸준한 습관이 건강을 만들어요.<br /> 올바른 습관 형성을 돕는 필루션케어
                                </Typography>
                            </Box>

                            {/* 건강에 도움 */}
                            <Box sx={{
                                flex: "1 1 250px",
                                textAlign: "center",
                                maxWidth: "280px",
                                margin: "0 auto"
                            }}>
                                <Typography variant="h3" sx={{ fontWeight: "bold", letterSpacing: "-1px" }}>
                                    91
                                    <span style={{ fontSize: "0.6em" }}>%</span>
                                </Typography>
                                <Typography sx={{ marginBottom: "18px", fontSize: "13px", letterSpacing: "-1px" }}>
                                    건강에 도움
                                </Typography>
                                <Typography sx={{ fontSize: "14px", color: "#666", lineHeight: 1.5, letterSpacing: "-0.5px" }}>
                                    먹을 때와 안 먹을 때의 차이<br /> 이미 많은 분들이 도움받고 있어요
                                </Typography>
                            </Box>

                            {/* 자속 섭취 기간 */}
                            <Box sx={{
                                flex: "1 1 250px",
                                textAlign: "center",
                                maxWidth: "280px",
                                margin: "0 auto"
                            }}>
                                <Typography variant="h3" sx={{ fontWeight: "bold", letterSpacing: "-1px" }}>
                                    12
                                    <span style={{ fontSize: "0.6em" }}>회</span>
                                </Typography>
                                <Typography sx={{ marginBottom: "18px", fontSize: "13px", letterSpacing: "-1px" }}>
                                    지속 섭취 기간
                                </Typography>
                                <Typography sx={{ fontSize: "14px", color: "#666", lineHeight: 1.5, letterSpacing: "-0.5px" }}>
                                    1년, 12개월, 꾸준하기 힘든 기간!<br /> 필루션과 함께라면 할 수 있어요.
                                </Typography>
                            </Box>
                        </Box>
                    </Box>
                </Box>
            </Box>

            {/* 영양제 솔루션 소개 */}
            <Box sx={{
                backgroundColor: "#fff",
                padding: "60px 20px",
                minHeight: "300px"
            }}>
                <Box sx={{
                    maxWidth: "1200px", // 컨텐츠 사이즈
                    margin: "0 auto"
                }}>
                    {/* 제목 */}
                    <Typography
                        variant="h5"
                        sx={{
                            textAlign: "center",
                            fontWeight: "bold",
                            marginBottom: "50px",
                            letterSpacing: "-1px"
                        }}
                    >
                        영양제 습관을 돕는 필루션 솔루션
                    </Typography>

                    {/* 솔루션 카드 컨테이너 */}
                    <Box sx={{
                        display: "flex",
                        justifyContent: "center",
                        gap: "30px",
                        flexWrap: "wrap",
                        marginBottom: "50px"
                    }}>
                        {/* 100% 개인 맞춤 영양제 */}
                        <Box
                            sx={{
                                textAlign: "center",
                                flex: "1 1 300px",
                                maxWidth: "350px"
                            }}
                        >
                            <Box
                                sx={{
                                    width: "200px",
                                    height: "200px",
                                    borderRadius: "50%",
                                    backgroundColor: "#FAEBD7",
                                    margin: "0 auto 20px",
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "center",
                                    overflow: "hidden" // 이미지가 넘치는 걸 방지
                                }}
                            >
                                <img
                                    src="src/assets/images/content_1.jpg"
                                    alt="맞춤 영양제"
                                    style={{
                                        width: "100%",
                                        height: "100%",
                                        objectFit: "cover" // 비율 유지하면서 꽉 채움
                                    }}
                                />
                            </Box>
                            <Typography variant="h6" sx={{ fontWeight: "bold", marginBottom: "10px", letterSpacing: "-1px" }}>
                                100% 개인 맞춤 영양제
                            </Typography>
                            <Typography sx={{ color: "#666", fontSize: "14px", letterSpacing: "-1px" }}>
                                설문을 통해 나에게 필요한 <br /> 영양성분 및 영양제를 추천해줘요
                            </Typography>
                        </Box>

                        {/* 스케줄 맞춤 정기배송 */}
                        <Box sx={{
                            textAlign: "center",
                            flex: "1 1 300px",
                            maxWidth: "350px"
                        }}>
                             <Box
                                sx={{
                                    width: "200px",
                                    height: "200px",
                                    borderRadius: "50%",
                                    backgroundColor: "#FAEBD7",
                                    margin: "0 auto 20px",
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "center",
                                    overflow: "hidden" // 이미지가 넘치는 걸 방지
                                }}
                            >
                                <img
                                    src="src/assets/images/content_3.jpg"
                                    alt="맞춤 영양제"
                                    style={{
                                        width: "100%",
                                        height: "100%",
                                        objectFit: "cover" // 비율 유지하면서 꽉 채움
                                    }}
                                />
                            </Box>
                            <Typography variant="h6" sx={{ fontWeight: "bold", marginBottom: "10px", letterSpacing: "-1px" }}>
                                스케줄 맞춤 정기배송
                            </Typography>
                            <Typography sx={{ color: "#666", fontSize: "14px", letterSpacing: "-1px" }}>
                                알아서 결제하고 딱 맞춰 배송되는<br />
                                정기배송으로 영양제 끊길 일 없어요
                            </Typography>
                        </Box>
                    </Box>

                    {/* 하단 버튼 */}
                    <Box sx={{ textAlign: "center" }}>
                        <Button
                            variant="contained"
                            size="large"
                            onClick={() => navigate('/survey')}
                            sx={{
                                backgroundColor: '#4169E1 ',
                                fontWeight: 'bold',
                                borderRadius: '50px',
                                width: '200px',
                                px: 4,
                                py: 2, // 위아래 패딩 키우기
                                letterSpacing: '-0.5px', // 글자 간격 좁게 설정
                            }}
                        >
                            영양제 추천받기
                        </Button>
                    </Box>
                </Box>
            </Box>
        </Box>
    );
};
export default HomePage;
