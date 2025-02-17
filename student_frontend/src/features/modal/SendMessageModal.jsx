import React, { useState } from "react";
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Autocomplete } from "@mui/material";
import { useSelector, useDispatch } from "react-redux";
import { API_URL } from "@/utils/constants";
import { fetchWithAuth } from "@/features/auth/fetchWithAuth";
import { showSnackbar } from "@/store/snackbarSlice";
import useDebounce from "@/hooks/useDebounce";

/**
 * SendMessageModal 컴포넌트
 * 새 메시지를 작성하고 전송하는 모달
 * @param {Object} props - 컴포넌트 props
 * @param {boolean} props.open - 모달 open 여부
 * @param {Function} props.onClose - 모달 닫기 함수
 * @param {Function} props.onSend - 메시지 전송 완료 후 실행할 함수
 * @returns {JSX.Element} SendMessageModal 컴포넌트
 */
const SendMessageModal = ({ open, onClose, onSend }) => {
    const [messageContent, setMessageContent] = useState("");
    const [selectedUser, setSelectedUser] = useState(null);
    const [users, setUsers] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const debouncedQuery = useDebounce(searchQuery, 300);
    const dispatch = useDispatch();
    const { user } = useSelector((state) => state.auth);

    const fetchUsers = async (query) => {
        if (!query) return;
        try {
            const response = await fetchWithAuth(`${API_URL}members/search?query=${query}`);
            if (response.ok) {
                const data = await response.json();
                setUsers(data.data || []);
            } else {
                setUsers([]);
            }
        } catch (error) {
            console.error("🚨 사용자 검색 실패:", error.message);
            setUsers([]);
        }
    };

    const handleSendMessage = async () => {
        if (!selectedUser || !messageContent) {
            dispatch(showSnackbar("❌ 수신자와 메시지를 입력해주세요."));
            return;
        }
        try {
            await fetchWithAuth(`${API_URL}messages/send`, {
                method: "POST",
                body: JSON.stringify({
                    senderId: user.id,
                    receiverId: selectedUser.id,
                    content: messageContent,
                }),
            });
            onClose();
            setMessageContent("");
            setSelectedUser(null);
            dispatch(showSnackbar("✅ 메시지가 성공적으로 전송되었습니다."));
            onSend();
        } catch (error) {
            console.error("🚨 메시지 전송 실패:", error.message);
        }
    };

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
            <DialogTitle>메시지 보내기</DialogTitle>
            <DialogContent>
                <Autocomplete
                    options={users}
                    getOptionLabel={(option) => option.name}
                    onChange={(event, value) => setSelectedUser(value)}
                    onInputChange={(event, newInputValue) => fetchUsers(newInputValue)}
                    renderInput={(params) => <TextField {...params} label="받는 사람" fullWidth />}
                />
                <TextField
                    fullWidth
                    multiline
                    rows={4}
                    label="메시지 내용"
                    value={messageContent}
                    onChange={(e) => setMessageContent(e.target.value)}
                    margin="normal"
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>취소</Button>
                <Button onClick={handleSendMessage} color="primary">보내기</Button>
            </DialogActions>
        </Dialog>
    );
};

export default SendMessageModal;
