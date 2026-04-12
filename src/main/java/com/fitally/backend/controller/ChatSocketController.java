package com.fitally.backend.controller;

import com.fitally.backend.dto.chat.request.ChatReadRequest;
import com.fitally.backend.dto.chat.request.ChatSendMessageRequest;
import com.fitally.backend.security.principal.CustomUserDetails;
import com.fitally.backend.service.ChatService;
import com.fitally.backend.websocket.StompPrincipalUtil;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final StompPrincipalUtil stompPrincipalUtil;

    @MessageMapping("/chat.send")
    public void send(ChatSendMessageRequest request, Principal principal) {
        Long currentUserId = stompPrincipalUtil.getCurrentUserId(principal);
        chatService.sendMessage(currentUserId, request);
    }

    @MessageMapping("/chat.read")
    public void markAsRead(Authentication authentication, ChatReadRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getUserId();

        chatService.markAsRead(currentUserId, request.getRoomId());
    }
}
