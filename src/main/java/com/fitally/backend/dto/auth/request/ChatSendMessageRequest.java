package com.fitally.backend.dto.auth.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSendMessageRequest {

    private Long roomId;
    private String messageText;
    private String imageUrl;
}
