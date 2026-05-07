package com.fitally.backend.controller;

import com.fitally.backend.dto.chat.response.ChatImageUploadResponse;
import com.fitally.backend.service.ChatImageService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/chat")
public class ChatImageController {

    private final ChatImageService chatImageService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatImageUploadResponse uploadChatImage(@RequestPart("file") MultipartFile file) {
        return chatImageService.uploadChatImage(file);
    }
}
