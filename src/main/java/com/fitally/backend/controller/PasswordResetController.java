package com.fitally.backend.controller;

import com.fitally.backend.dto.auth.response.MessageResponse;
import com.fitally.backend.dto.auth.request.ResetPasswordRequest;
import com.fitally.backend.dto.auth.request.SendPasswordResetCodeRequest;
import com.fitally.backend.dto.auth.request.VerifyPasswordResetCodeRequest;
import com.fitally.backend.dto.auth.response.VerifyPasswordResetCodeResponse;
import com.fitally.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/find-pw/sendcode")
    public ResponseEntity<MessageResponse> sendCode(@Valid @RequestBody SendPasswordResetCodeRequest request) {
        passwordResetService.sendCode(request);
        return ResponseEntity.ok(new MessageResponse("인증코드를 전송했습니다."));
    }

    @PostMapping("/find-pw/verify")
    public ResponseEntity<VerifyPasswordResetCodeResponse> verifyCode(@Valid @RequestBody VerifyPasswordResetCodeRequest request) {
        VerifyPasswordResetCodeResponse response = passwordResetService.verifyCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/new-pw")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("비밀번호가 변경되었습니다."));
    }
}
