package com.fitally.backend.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ChatRoomResponse {

    private Long roomId;
    private Long opponentUserId;
    private String opponentNickName;
    private String opponentProfileImageUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
}
