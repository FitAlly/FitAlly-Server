package com.fitally.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitally.backend.dto.auth.request.ResetPasswordRequest;
import com.fitally.backend.dto.auth.request.SendPasswordResetCodeRequest;
import com.fitally.backend.dto.auth.request.VerifyPasswordResetCodeRequest;
import com.fitally.backend.dto.auth.response.VerifyPasswordResetCodeResponse;
import com.fitally.backend.service.PasswordResetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PasswordResetController.class)
@Import(TestSecurityConfig.class)
public class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("인증코드 발송 성공")
    void sendCode_success() throws Exception {
        // given
        SendPasswordResetCodeRequest request = createSendCodeRequest("test@test.com");

        doNothing().when(passwordResetService).sendCode(any(SendPasswordResetCodeRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/find-pw/sendcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("인증코드를 전송했습니다."));

        verify(passwordResetService).sendCode(any(SendPasswordResetCodeRequest.class));
    }

    @Test
    @DisplayName("인증코드 발송 실패 - 이메일이 비어 있으면 400")
    void sendCode_fail_blankEmail() throws Exception {
        // given
        String requestBody = """
                {
                  "email": ""
                }
                """;

        // when & then
        mockMvc.perform(post("/api/auth/find-pw/sendcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).sendCode(any(SendPasswordResetCodeRequest.class));
    }

    @Test
    @DisplayName("인증코드 검증 성공")
    void verifyCode_success() throws Exception {
        // given
        VerifyPasswordResetCodeRequest request = createVerifyCodeRequest("test@test.com", "123456");

        VerifyPasswordResetCodeResponse response =
                new VerifyPasswordResetCodeResponse("인증이 완료되었습니다.", "verify-token-123");

        when(passwordResetService.verifyCode(any(VerifyPasswordResetCodeRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/find-pw/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("인증이 완료되었습니다."))
                .andExpect(jsonPath("$.verifyToken").value("verify-token-123"));

        verify(passwordResetService).verifyCode(any(VerifyPasswordResetCodeRequest.class));
    }

    @Test
    @DisplayName("인증코드 검증 실패 - 코드 형식이 잘못되면 400")
    void verifyCode_fail_invalidCodeFormat() throws Exception {
        // given
        String requestBody = """
                {
                  "email": "test@test.com",
                  "code": "12ab"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/auth/find-pw/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).verifyCode(any(VerifyPasswordResetCodeRequest.class));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void resetPassword_success() throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest(
                "test@test.com",
                "verify-token-123",
                "newPassword123!"
        );

        doNothing().when(passwordResetService).resetPassword(any(ResetPasswordRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/new-pw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다."));

        verify(passwordResetService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호가 비어 있으면 400")
    void resetPassword_fail_blankPassword() throws Exception {
        // given
        String requestBody = """
                {
                  "email": "test@test.com",
                  "verifyToken": "verify-token-123",
                  "newPassword": ""
                }
                """;

        // when & then
        mockMvc.perform(post("/api/auth/new-pw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 비밀번호가 8자 미만이면 400")
    void resetPassword_fail_shortPassword() throws Exception {
        // given
        String requestBody = """
                {
                  "email": "test@test.com",
                  "verifyToken": "verify-token-123",
                  "newPassword": "1234"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/auth/new-pw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).resetPassword(any(ResetPasswordRequest.class));
    }

    private SendPasswordResetCodeRequest createSendCodeRequest(String email) throws Exception {
        SendPasswordResetCodeRequest request = new SendPasswordResetCodeRequest();
        setField(request, "email", email);
        return request;
    }

    private VerifyPasswordResetCodeRequest createVerifyCodeRequest(String email, String code) throws Exception {
        VerifyPasswordResetCodeRequest request = new VerifyPasswordResetCodeRequest();
        setField(request, "email", email);
        setField(request, "code", code);
        return request;
    }

    private ResetPasswordRequest createResetPasswordRequest(String email, String verifyToken, String newPassword) throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        setField(request, "email", email);
        setField(request, "verifyToken", verifyToken);
        setField(request, "newPassword", newPassword);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}