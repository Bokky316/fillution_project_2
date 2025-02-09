package com.javalab.student.controller;

import com.javalab.student.dto.ChatMessageDto;
import com.javalab.student.dto.TypingStatus;
import com.javalab.student.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket을 통해 채팅 메시지를 처리하는 컨트롤러
 */
@Controller
@Slf4j
public class WebSocketChatController {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private ChatService chatService;

    /**
     * "/chat.sendMessage" 엔드포인트로 들어오는 메시지를 처리한다.
     *
     * @param chatMessageDto 수신된 채팅 메시지 정보
     * @param headerAccessor 메시지 헤더에 접근하기 위한 객체
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 메시지 저장 및 처리
            ChatMessageDto savedMessage = chatService.sendMessage(chatMessageDto);

            // 채팅방의 다른 참가자 ID 가져오기
            Long receiverId = chatService.getReceiverIdForRoom(chatMessageDto.getRoomId(), chatMessageDto.getSenderId());

            // receiverId가 null인 경우에 대한 예외 처리
            if (receiverId == null) {
                log.error("수신자 ID가 null입니다. roomId: {}, senderId: {}", chatMessageDto.getRoomId(), chatMessageDto.getSenderId());
                return;
            }

            // 채팅방에 메시지 전송
            messagingTemplate.convertAndSend("/topic/chat/" + chatMessageDto.getRoomId(), savedMessage);

            // 수신자에게 알림 전송
            messagingTemplate.convertAndSend("/topic/notifications/" + receiverId, "새 메시지가 도착했습니다.");

            // 채팅방 목록 업데이트를 위한 이벤트 발송
            messagingTemplate.convertAndSend("/topic/chat.rooms.update", chatMessageDto.getRoomId());
        } catch (RuntimeException e) {
            log.error("메시지 전송 중 오류 발생", e);
            messagingTemplate.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/errors", "메시지 전송 중 오류가 발생했습니다.");
        }
    }

    /**
     * "/chat.typing" 엔드포인트로 들어오는 타이핑 상태 메시지를 처리한다.
     *
     * @param typingStatus 수신된 타이핑 상태 정보
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingStatus typingStatus) {
        // 타이핑 상태를 해당 채팅방의 구독자들에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/" + typingStatus.getRoomId() + "/typing", typingStatus);
    }
}
