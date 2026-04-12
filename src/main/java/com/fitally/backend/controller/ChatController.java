package com.fitally.backend.controller;

import com.fitally.backend.common.response.ApiResponse;
import com.fitally.backend.common.util.SecurityUtil;
import com.fitally.backend.dto.chat.response.ChatMessageResponse;
import com.fitally.backend.dto.chat.response.ChatRoomResponse;
import com.fitally.backend.security.principal.CustomUserDetails;
import com.fitally.backend.service.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/rooms")
    public List<ChatRoomResponse> getChatRooms(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getUserId();

        return chatService.getChatRooms(currentUserId);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessage(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(chatService.getMessages(currentUserId, roomId, page, size)));
    }

    @PostMapping("/rooms/direct")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createdDirectRoom(
            @RequestParam Long opponentUserId
    ) {
        Long currentUerId = SecurityUtil.getCurrentUserId();
        Long roomId = chatService.createDirectRoom(currentUerId, opponentUserId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("roomId", roomId)));
    }

    @PatchMapping("/rooms/{roomId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long roomId
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        chatService.markAsRead(currentUserId, roomId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }


}
