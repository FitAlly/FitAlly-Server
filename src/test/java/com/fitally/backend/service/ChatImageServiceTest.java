package com.fitally.backend.service;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.dto.chat.response.ChatImageUploadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ChatImageServiceTest {

    private ChatImageService chatImageService;

    @BeforeEach
    void setUp() {
        chatImageService = new ChatImageService();
        ReflectionTestUtils.setField(chatImageService, "uploadDir", "uploads/chat-test");
        ReflectionTestUtils.setField(chatImageService, "accessUrlPrefix", "/chat-images");
    }

    @Test
    @DisplayName("이미지 업로드는 정상 파일이면 URL을 반환한다")
    void uploadChatImage_success() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "dummy-image-content".getBytes(StandardCharsets.UTF_8)
        );

        // when
        ChatImageUploadResponse response = chatImageService.uploadChatImage(file);

        // then
        assertNotNull(response);
        assertNotNull(response.getImageUrl());
        assertTrue(response.getImageUrl().startsWith("/chat-images/"));
        assertEquals("test.png", response.getOriginalFileName());
        assertNotNull(response.getStoredFileName());
    }

    @Test
    @DisplayName("이미지 업로드는 빈 파일이면 예외가 발생한다")
    void uploadChatImage_emptyFile_throwsException() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> chatImageService.uploadChatImage(file)
        );

        // then
        assertEquals("채팅 이미지 파일이 비어 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("이미지 업로드는 허용되지 않은 확장자면 예외가 발생한다")
    void uploadChatImage_invalidExtension_throwsException() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.exe",
                "application/octet-stream",
                "not-image".getBytes(StandardCharsets.UTF_8)
        );

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> chatImageService.uploadChatImage(file)
        );

        // then
        assertEquals("허용되지 않은 이미지 확장자입니다.", exception.getMessage());
    }
}