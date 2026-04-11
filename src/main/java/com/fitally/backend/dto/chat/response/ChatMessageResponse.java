package com.fitally.backend.dto.chat.response;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class ChatMessageResponse {

    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderNickName;
    private String senderProfileImageUrl;
    private String messageText;
    private String imageUrl;
    private LocalDateTime createdAt;
    private boolean mine;

    public Long getMessageId() {
        return messageId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderNickname() {
        return senderNickName;
    }

    public String getSenderProfileImageUrl() {
        return senderProfileImageUrl;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isMine() {
        return mine;
    }
}
