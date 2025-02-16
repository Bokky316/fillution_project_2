import React, { useState, useEffect, useCallback, useMemo } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Button, TextField, Box, Typography, Paper, Radio, RadioGroup, FormControlLabel, FormControl, Divider } from "@mui/material";
import { API_URL } from "@/utils/constants";
import { fetchWithAuth } from "@/features/auth/fetchWithAuth";
import { useDispatch, useSelector } from "react-redux";
import { createOrder } from "@/store/orderSlice";
import "@/styles/OrderDetail.css";

const OrderDetail = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const { selectedItems, purchaseType, totalAmount, user: userData } = location.state
    ? location.state
    : { selectedItems: [], purchaseType: 'oneTime', totalAmount: 0, user: {} };

  const [name, setName] = useState(userData?.name || '');
  const [email, setEmail] = useState(userData?.email || '');
  const [phone, setPhone] = useState(userData?.phone || '');
  const [address, setAddress] = useState(userData?.address || '');
  const [zipCode, setZipCode] = useState("");
  const [address1, setAddress1] = useState("");
  const [address2, setAddress2] = useState("");
  const [merchantId, setMerchantId] = useState("");
  const [order, setOrder] = useState(null);
  const [isImpReady, setIsImpReady] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState("kakaopay");

  useEffect(() => {
    const id = fetchMerchantId();
    setMerchantId(id);

    const script = document.createElement("script");
    script.src = "https://cdn.iamport.kr/js/iamport.payment-1.2.0.js";
    script.async = true;
    script.onload = () => setIsImpReady(true);
    document.body.appendChild(script);

    return () => {
      document.body.removeChild(script);
    };
  }, []);

  const fetchMerchantId = () => {
    const merchantId = import.meta.env.VITE_PORTONE_MERCHANT_ID;
    console.log("가맹점 UID:", merchantId);
    return merchantId;
  };

  const calculateTotalPrice = () => {
    return selectedItems.reduce((total, item) => total + item.price * item.quantity, 0);
  };

  const handleCreateOrder = async () => {
    const orderData = {
      cartOrderItems: selectedItems.map(item => ({
        cartItemId: item.cartItemId,
        quantity: item.quantity,
        price: item.price
      })),
      buyerName: name,
      buyerEmail: email,
      buyerTel: phone,
      buyerAddr: `${address1} ${address2}`,
      buyerPostcode: zipCode,
    };

    try {
      const response = await dispatch(createOrder({ orderData, purchaseType })).unwrap();
      console.log("주문 생성 성공:", response);
      setOrder({
        ...response,
        items: selectedItems,
        totalAmount: totalAmount,
        purchaseType: purchaseType
      });
    } catch (error) {
      console.error("주문 생성 실패:", error);
      alert("주문 생성에 실패했습니다.");
    }
  };

  const handlePayment = async () => {
    if (!isImpReady || !order) {
      alert("결제 모듈이 아직 로드되지 않았거나 주문이 생성되지 않았습니다.");
      return;
    }

    const IMP = window.IMP;
    IMP.init(merchantId);

    const paymentData = {
           pg: paymentMethod, // 선택한 결제 수단으로 변경
           pay_method: "card",
           merchant_uid: order.id,
           name: order.items[0].name,
           amount: finalAmount, // 최종 결제 금액 사용
           buyer_email: email,
           buyer_name: name,
           buyer_tel: phone,
           buyer_addr: `${address1} ${address2}`,
           buyer_postcode: zipCode,
       };

       IMP.request_pay(paymentData, async (rsp) => {
           if (rsp.success) {
               console.log("결제 완료 응답:", rsp);
               try {
                   const response = await processPayment(rsp);
                   if (response.ok) {
                       const data = await response.json();
                       navigate("/payResult", {
                           state: {
                               paymentInfo: {
                                   ...data,
                                   amount: finalAmount,
                                   paymentMethod: paymentMethod,
                                   merchantUid: order.id,
                                   impUid: rsp.imp_uid,
                                   status: "PAYMENT_COMPLETED",
                                   paidAt: Math.floor(Date.now() / 1000) // 현재 시간을 Unix timestamp로 변환
                               }
                           }
                       });
                   } else {
                       throw new Error('서버 응답 오류');
                   }
               } catch (error) {
                   console.error("결제 처리 중 오류 발생:", error);
                   alert("결제는 완료되었지만 서버 처리 중 오류가 발생했습니다. 고객센터에 문의해주세요.");
               }
           } else {
               alert(`결제 실패: ${rsp.error_msg}`);
           }
       });
   };

  const processPayment = async (rsp) => {
    const paymentRequest = {
      impUid: rsp.imp_uid,
      merchantUid: order.id,
      paidAmount: rsp.paid_amount,
      name: rsp.name,
      pgProvider: rsp.pg_provider,
      buyerEmail: rsp.buyer_email,
      buyerName: rsp.buyer_name,
      buyTel: rsp.buyer_tel,
      buyerAddr: rsp.buyer_addr,
      buyerPostcode: rsp.buyer_postcode,
      paidAt: rsp.paid_at,
      status: "PAYMENT_COMPLETED",
    };

    return fetchWithAuth(`${API_URL}payments/request?purchaseType=${order.purchaseType}`, {
      method: "POST",
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(paymentRequest),
    });
  };

  const isSubscription = purchaseType === 'subscription';

  const calculateDiscount = () => {
    let discount = 0;
    if (isSubscription) {
      discount += 3000; // 정기구독 할인
    }
    return discount;
  };

    const handlePaymentMethodChange = (event) => {
        setPaymentMethod(event.target.value);
    };

  const discount = calculateDiscount();
  const shippingFee = totalAmount >= 50000 ? 0 : 3000;
  const finalAmount = totalAmount + shippingFee - discount;

  return (
    <Box sx={{ maxWidth: 800, margin: "auto", padding: 3 }}>
      <Typography variant="h4" gutterBottom>
        주문서 작성
      </Typography>
      <Paper sx={{ padding: 3 }}>
        {/* 제품 정보 */}
        <Typography variant="h6" gutterBottom>
          제품 정보
        </Typography>
        {selectedItems.map((item, index) => (
          <Box key={index} display="flex" alignItems="center" mb={2}>
            <img
              src={item.imageUrl}
              alt={item.name}
              style={{ width: 100, height: 100, marginRight: 20 }}
            />
            <Box>
              <Typography variant="subtitle1">{item.name}</Typography>
              <Typography variant="body2">{item.quantity}개 x {item.price}원</Typography>
            </Box>
          </Box>
        ))}

        {/* 합계 금액 */}
        <Typography variant="h6" mt={3}>
          총 주문 금액: {calculateTotalPrice()}원
        </Typography>

        {/* 할인 내역 */}
        <Typography variant="h6" mt={3} gutterBottom>
          할인 내역
        </Typography>
        <Box display="flex" justifyContent="space-between" mt={2}>
          <Typography>배송비 무료</Typography>
          <Typography>-{shippingFee === 0 ? 3000 : 0}원</Typography>
        </Box>
        <Box display="flex" justifyContent="space-between" mt={2}>
          <Typography>정기구독 할인</Typography>
          <Typography>-{discount}원</Typography>
        </Box>

        {/* 배송 정보 */}
        <Typography variant="h6" mt={3} gutterBottom>
          배송 정보
        </Typography>
        <TextField
          label="이름"
          value={name}
          onChange={(e) => setName(e.target.value)}
          fullWidth
          margin="normal"
        />
        <TextField
          label="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          fullWidth
          margin="normal"
        />
        <TextField
          label="전화번호"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
          fullWidth
          margin="normal"
        />
        <TextField
          label="주소"
          value={address}
          onChange={(e) => setAddress(e.target.value)}
          fullWidth
          margin="normal"
        />

        <Typography variant="h6" mt={3} gutterBottom>
          배송지 정보
        </Typography>
        <TextField
          label="우편번호"
          value={zipCode}
          onChange={(e) => setZipCode(e.target.value)}
          fullWidth
          margin="normal"
        />
        <TextField
          label="주소 1"
          value={address1}
          onChange={(e) => setAddress1(e.target.value)}
          fullWidth
          margin="normal"
        />
        <TextField
          label="주소 2 (상세주소)"
          value={address2}
          onChange={(e) => setAddress2(e.target.value)}
          fullWidth
          margin="normal"
        />

        <Box mt={3} textAlign="center">
          <Button variant="contained" color="primary" size="large" onClick={handleCreateOrder}>
            주문 생성
          </Button>
        </Box>
      </Paper>

      {order && (
        <Paper sx={{ padding: 3, marginTop: 3 }}>
          {/* 결제 정보 */}
          <Typography variant="h5" gutterBottom>
            결제 정보 등록
          </Typography>
{/*           <Box display="flex" justifyContent="space-between" mt={2}> */}
{/*             <Typography>제품합계금액</Typography> */}
{/*             <Typography>{calculateTotalPrice()}원</Typography> */}
{/*           </Box> */}
{/*           <Box display="flex" justifyContent="space-between" mt={2}> */}
{/*             <Typography>기본 배송비</Typography> */}
{/*             <Typography>{shippingFee}원</Typography> */}
{/*           </Box> */}
{/*           <Box display="flex" justifyContent="space-between" mt={2}> */}
{/*             <Typography>총 할인금액</Typography> */}
{/*             <Typography>-{discount}원</Typography> */}
{/*           </Box> */}
{/*           <Divider sx={{ my: 2 }} /> */}
{/*           <Box display="flex" justifyContent="space-between" mt={2}> */}
{/*             <Typography variant="h6">총 결제금액</Typography> */}
{/*             <Typography variant="h6">{finalAmount}원</Typography> */}
{/*           </Box> */}

          {/* 결제 수단 선택 */}
            <FormControl component="fieldset" className="payment-method-select">
                <RadioGroup
                    aria-label="paymentMethod"
                    name="paymentMethod"
                    value={paymentMethod}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                >
                    <FormControlLabel
                        value="kakaopay"
                        control={<Radio />}
                        label="카카오페이"
                    />
                    <FormControlLabel
                        value="payco"
                        control={<Radio />}
                        label="페이코"
                    />
                    <FormControlLabel
                        value="card"
                        control={<Radio />}
                        label="신용 / 체크카드"
                    />
                </RadioGroup>
            </FormControl>

            {/* 결제하기 버튼 */}
            <Box mt={2} textAlign="center">
                <Button
                    variant="contained"
                    color="secondary"
                    size="large"
                    onClick={handlePayment}
                    disabled={!isImpReady}
                    className="payment-button"
                >
                    {finalAmount}원 결제하기
                </Button>
            </Box>
        </Paper>
      )}
    </Box>
  );
};

export default OrderDetail;
