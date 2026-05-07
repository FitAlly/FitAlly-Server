package com.fitally.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_participants")
@Getter
@Setter
public class ChatParticipant {

    @EmbeddedId
    private ChatParticipantId id;

    @Column(name = "unread_count")
    private Integer unreadCount;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) joinedAt = LocalDateTime.now();
        if (unreadCount == null) unreadCount = 0;
    }
}
