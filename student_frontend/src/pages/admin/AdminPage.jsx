import React from "react";
import { Routes, Route, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { setActiveMenu } from "../../redux/sidebarSlice";
import SideBar from "../../features/admin/SideBar";
import MemberManagement from "../../features/admin/member/MemberList";
import EditMember from "../../features/admin/member/EditMember";
import EditProduct from "../../features/admin/product/EditProduct";
import AddProduct from "../../features/admin/product/AddProduct";
import OrderManagement from "../../features/admin/order/OrderList";
import ProductManagement from "../../features/admin/product/ProductList";

const AdminPage = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const activeMenu = useSelector((state) => state.sidebar.activeMenu);

    const handlePageChange = (menu) => {
        dispatch(setActiveMenu(menu)); // 메뉴 상태 업데이트
        navigate(`/adminPage/${menu}`); // 해당 경로로 네비게이션
    };

    return (
        <div className="admin-container">
            <SideBar />
            <div className="admin-content">
                <Routes>
                    <Route path="members" element={<MemberManagement onPageChange={() => handlePageChange("members")} />} />
                    <Route path="members/:memberId/edit" element={<EditMember />} />
                    <Route path="orders" element={<OrderManagement onPageChange={() => handlePageChange("orders")} />} />
                    <Route path="products" element={<ProductManagement onPageChange={() => handlePageChange("products")} />} />
                    <Route path="products/:productId/edit" element={<EditProduct />} />
                    <Route path="products/add" element={<AddProduct />} />
                </Routes>
            </div>
        </div>
    );
};

export default AdminPage;
