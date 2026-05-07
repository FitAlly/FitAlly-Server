package com.fitally.backend.controller;

import com.fitally.backend.dto.chat.request.ChatSendMessageRequest;
import com.fitally.backend.service.ChatService;
import com.fitally.backend.websocket.StompPrincipalUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Principal;

import static org.mockito.Mockito.*;

class ChatSocketControllerTest {

    private final ChatService chatService = mock(ChatService.class);
    private final StompPrincipalUtil stompPrincipalUtil = mock(StompPrincipalUtil.class);

    private final ChatSocketController chatSocketController =
            new ChatSocketController(chatService, stompPrincipalUtil);

    @Test
    @DisplayName("send는 Principal에서 현재 사용자 ID를 꺼내 ChatService에 메시지 전송을 위임한다")
    void send_withAuthenticatedPrincipal_callsChatServiceSendMessage() {
        // given
        Principal principal = mock(Principal.class);

        ChatSendMessageRequest request = new ChatSendMessageRequest();
        request.setRoomId(10L);
        request.setMessageText("안녕하세요");
        request.setImageUrl(null);

        when(stompPrincipalUtil.getCurrentUserId(principal)).thenReturn(1L);

        // when
        chatSocketController.send(request, principal);

        // then
        verify(stompPrincipalUtil, times(1)).getCurrentUserId(principal);
        verify(chatService, times(1)).sendMessage(1L, request);
    }

    @Test
    @DisplayName("send는 이미지 메시지도 ChatService에 그대로 위임한다")
    void send_withImageMessage_callsChatServiceSendMessage() {
        // given
        Principal principal = mock(Principal.class);

        ChatSendMessageRequest request = new ChatSendMessageRequest();
        request.setRoomId(20L);
        request.setMessageText(null);
        request.setImageUrl("https://fitally-image.s3.ap-northeast-2.amazonaws.com/chat/test.png");

        when(stompPrincipalUtil.getCurrentUserId(principal)).thenReturn(7L);

        // when
        chatSocketController.send(request, principal);

        // then
        verify(stompPrincipalUtil, times(1)).getCurrentUserId(principal);
        verify(chatService, times(1)).sendMessage(7L, request);
    }

    @Test
    @DisplayName("send는 StompPrincipalUtil에서 예외가 발생하면 ChatService를 호출하지 않는다")
    void send_whenPrincipalUtilThrows_doesNotCallChatService() {
        // given
        Principal principal = mock(Principal.class);

        ChatSendMessageRequest request = new ChatSendMessageRequest();
        request.setRoomId(30L);
        request.setMessageText("테스트");
        request.setImageUrl(null);

        when(stompPrincipalUtil.getCurrentUserId(principal))
                .thenThrow(new IllegalArgumentException("WebSocket 사용자 정보를 확인할 수 없습니다."));

        // when
        try {
            chatSocketController.send(request, principal);
        } catch (IllegalArgumentException ignored) {
        }

        // then
        verify(stompPrincipalUtil, times(1)).getCurrentUserId(principal);
        verify(chatService, never()).sendMessage(anyLong(), any(ChatSendMessageRequest.class));
    }
}