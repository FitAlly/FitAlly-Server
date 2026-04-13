package com.fitally.backend.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatImageUploadResponse {

    private String imageUrl;
    private String originalFileName;
    private String storedFileName;
}
