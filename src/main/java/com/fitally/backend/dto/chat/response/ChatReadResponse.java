package com.fitally.backend.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatReadResponse {

    private Long roomId;
    private Long userId;
    private Integer unreadCount;
    private LocalDateTime readAt;
}
