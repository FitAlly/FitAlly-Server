package com.fitally.backend.controller;

import com.fitally.backend.dto.chat.request.ChatSendMessageRequest;
import com.fitally.backend.service.ChatService;
import com.fitally.backend.websocket.StompPrincipalUtil;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
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
}
