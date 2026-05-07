package com.fitally.backend.service;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.dto.chat.response.ChatImageUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class ChatImageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${chat.image.upload-dir}")
    private String uploadDir;

    @Value("${chat.image.access-url-prefix}")
    private String accessUrlPrefix;

    public ChatImageUploadResponse uploadChatImage(MultipartFile file) {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String extension = getExtension(originalFileName);
        String storedFileName = createStoredFileName(extension);

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path targetPath = uploadPath.resolve(storedFileName);
            file.transferTo(targetPath.toFile());

            String imageUrl = accessUrlPrefix + "/" + storedFileName;

            return new ChatImageUploadResponse(
                    imageUrl,
                    originalFileName,
                    storedFileName
            );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.CHAT_IMAGE_UPLOAD_FAILED);
        }
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.CHAT_IMAGE_EMPTY);
        }

        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFileName)) {
            throw new BusinessException(ErrorCode.CHAT_IMAGE_INVALID);
        }

        String extension = getExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.CHAT_IMAGE_EXTENSION_NOT_ALLOWED);
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.CHAT_IMAGE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.CHAT_IMAGE_INVALID);
        }
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1 || index == fileName.length() -1) {
            throw new BusinessException(ErrorCode.CHAT_IMAGE_INVALID);
        }
        return fileName.substring(index + 1);
    }

    private String createStoredFileName(String extension) {
        return LocalDateTime.now().toString().replace(":", "-")
                + "-"
                + UUID.randomUUID()
                + "."
                + extension;
    }
}
