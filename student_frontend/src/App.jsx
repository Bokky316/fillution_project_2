import React from "react";
import { CircularProgress } from "@mui/material";
import { useSelector } from "react-redux";
import { Routes, Route } from "react-router-dom";
import { persistor } from "@/store/store";
import { API_URL } from "@/utils/constants";

import Header from "@/layouts/Header";
import Footer from "@/layouts/Footer";
import Layout from "@/layouts/Layout";

import HomePage from "@/pages/HomePage";
import RecommendationPage from "@/pages/RecommendationPage";
import SurveyPage from "@/pages/SurveyPage";
import ProductDetailPage from "@/pages/ProductDetailPage";
import ProductListPage from "@/pages/ProductListPage";
import CartPage from "@/pages/CartPage";
import BoardPage from "@/pages/BoardPage";
import NewsBoardPage from "@/pages/NewsBoardPage";
import FAQBoardPage from "@/pages/FAQBoardPage";
import AdminPage from "@/pages/AdminPage";
import SubscriptionPage from "@/pages/SubscriptionManagement";
import OrderDetailPage from "@/pages/OrderDetailPage";
// import PaymentPage from '@/pages/PaymentPage';

import Login from "@/features/auth/Login";
import MyPage from "@/features/auth/MyPage";
import RegisterMember from "@/features/auth/RegisterMember";
import UnauthorizedPage from "@/features/auth/UnAuthorizedPage";
import OAuth2RedirectHandler from '@/features/auth/OAuth2RedirectHandler';
import MessageList from "@/features/auth/MessageList";
import KakaoAddressSearch from "@/features/auth/KakaoAddressSearch";

import PostDetailPage from "@/features/board/PostDetailPage";
import PostCreatePage from "@/features/board/PostCreatePage";
import PostEditPage from "@/features/board/PostEditPage";

import ConsultationRequestList from "@/features/chat/ConsultationRequestList";
import FloatingConsultationButton from "@/features/chat/FloatingConsultationButton";
import ChatRoom from "@/features/chat/ChatRoom";

import useAuth from "@/hooks/useAuth";
import useWebSocket from "@/hooks/useWebSocket";
import useMessage from "@/hooks/useMessage";

// import "@/App.css";

function App() {
    const { isLoading, isLoggedIn, user } = useAuth();
    useWebSocket(user);
    const { delayedUnreadCount } = useMessage(user?.id);

    if (isLoading) {
        return <CircularProgress />;
    }

    return (
        <div className="App">
            <Header />
            <Layout>
                <Routes>
                    {/* Routes */}
                    <Route path="/" element={<HomePage />} />
                    <Route path="/products" element={<ProductListPage />} />
                    <Route path="/products/:productId" element={<ProductDetailPage />} />
                    <Route path="/cart" element={<CartPage />} />
                    <Route path="/order-detail" element={<OrderDetailPage />} /> {/* OrderDetailPage Route 추가 */}
{/*                     <Route path="/payment" element={<PaymentPage />} /> */}
                    <Route path="/recommendation" element={<RecommendationPage />} />
                    <Route path="/survey" element={<SurveyPage />} />

                    <Route path="/board/*" element={<BoardPage />} />
                    <Route path="/news" element={<NewsBoardPage />} />
                    <Route path="/faq" element={<FAQBoardPage />} />
                    <Route path="/post/:postId" element={<PostDetailPage />} />
                    <Route path="/post/create" element={<PostCreatePage />} />
                    <Route path="/post/:postId/edit" element={<PostEditPage />} />
                    <Route path="/faq/post/:postId/edit" element={<PostEditPage />} />

                    <Route path="/login" element={<Login />} />
                    <Route path="/mypage" element={<MyPage />} />
                    <Route path="/registerMember" element={<RegisterMember />} />
                    <Route path="/unauthorized" element={<UnauthorizedPage />} />
                    <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
                    <Route path="/messages" element={<MessageList />} />
                    <Route path="/update-delivery" element={<KakaoAddressSearch />} />

                    <Route path="/consultation" element={<ConsultationRequestList />} />
                    <Route path="/chatroom/:roomId" element={<ChatRoom />} />

                    <Route path="/adminpage/*" element={<AdminPage />} />
                    <Route path="/subscription" element={<SubscriptionPage />} />
                </Routes>
            </Layout>
            <Footer />
            <FloatingConsultationButton/>
        </div>
    );
}

export default App;
