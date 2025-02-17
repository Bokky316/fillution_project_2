import React, { useState, useEffect } from "react";
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Autocomplete } from "@mui/material";
import { useSelector, useDispatch } from "react-redux";
import { API_URL } from "@/utils/constants";
import { fetchWithAuth } from "@/features/auth/fetchWithAuth";
import { showSnackbar } from "@/store/snackbarSlice";
import useDebounce from "@/hooks/useDebounce";

const SendMessageModal = ({ open, onClose, onSend }) => {
    const [messageContent, setMessageContent] = useState("");
    const [selectedUser, setSelectedUser] = useState(null);
    const [users, setUsers] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const debouncedQuery = useDebounce(searchQuery, 300);
    const dispatch = useDispatch();
    const { user } = useSelector((state) => state.auth);

    useEffect(() => {
        if (debouncedQuery) {
            fetchUsers(debouncedQuery);
        }
    }, [debouncedQuery]);

    useEffect(() => {
        if (!open) {
            setMessageContent("");
            setSelectedUser(null);
            setUsers([]);
            setSearchQuery("");
        }
    }, [open]);

    const fetchUsers = async (query) => {
        try {
            const response = await fetchWithAuth(`${API_URL}members/search?query=${query}`);
            if (response.ok) {
                const responseData = await response.json();
                if (responseData.status === 'success' && Array.isArray(responseData.data)) {
                    const formattedUsers = responseData.data.map(user => ({
                        id: user.id,
                        name: `${user.name} (${user.email}) - ID: ${user.id}`,
                        email: user.email
                    }));
                    setUsers(formattedUsers);
                } else {
                    setUsers([]);
                }
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
            const response = await fetchWithAuth(`${API_URL}messages/send`, {
                method: "POST",
                body: JSON.stringify({
                    senderId: user.id,
                    receiverType: 'USER',
                    receiverId: Number(selectedUser.id),
                    content: messageContent,
                }),
            });
            if (response.ok) {
                dispatch(showSnackbar("✅ 메시지가 성공적으로 전송되었습니다."));
                handleClose();
                onSend();
            } else {
                dispatch(showSnackbar("❌ 메시지 전송 실패"));
            }
        } catch (error) {
            console.error("🚨 메시지 전송 실패:", error.message);
            dispatch(showSnackbar("❌ 메시지 전송 실패"));
        }
    };

    const handleClose = () => {
        setMessageContent("");
        setSelectedUser(null);
        setUsers([]);
        setSearchQuery("");
        onClose();
    };

    return (
        <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
            <DialogTitle>메시지 보내기</DialogTitle>
            <DialogContent>
                <Autocomplete
                    options={users}
                    getOptionLabel={(option) => option.name || ""}
                    value={selectedUser}
                    onChange={(event, newValue) => {
                        setSelectedUser(newValue);
                        console.log("Selected user:", newValue);
                    }}
                    onInputChange={(event, newInputValue, reason) => {
                        if (reason === 'input') {
                            setSearchQuery(newInputValue);
                        }
                    }}
                    renderInput={(params) => <TextField {...params} label="받는 사람 (이름 | 이메일 | ID로 검색)" fullWidth />}
                    isOptionEqualToValue={(option, value) => option?.id === value?.id}
                    filterOptions={(x) => x}
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
                <Button onClick={handleClose}>취소</Button>
                <Button onClick={handleSendMessage} color="primary">보내기</Button>
            </DialogActions>
        </Dialog>
    );
};

export default SendMessageModal;
