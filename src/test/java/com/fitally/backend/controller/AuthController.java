package com.fitally.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitally.backend.dto.auth.request.*;
import com.fitally.backend.dto.auth.response.SignupResponse;
import com.fitally.backend.dto.auth.response.TokenResponse;
import com.fitally.backend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// 필요하면 jsonPath 추가
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Nested
    @DisplayName("일반 회원가입 컨트롤러 테스트")
    class SignupControllerTest {

        @Test
        @DisplayName("회원가입 성공")
        void signup_success() throws Exception {
            SignupRequest request = new SignupRequest(
                    "test@example.com",
                    "12345678",
                    "혁진",
                    LocalDate.of(2000, 1, 1)
            );

            SignupResponse response = new SignupResponse(
                    1L,
                    "test@example.com",
                    "혁진",
                    "email",
                    LocalDate.of(2000, 1, 1)
            );

            when(authService.signup(any(SignupRequest.class))).thenReturn(response);

            mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(authService).signup(any(SignupRequest.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 이메일 형식 오류")
        void signup_fail_invalid_email() throws Exception {
            SignupRequest request = new SignupRequest(
                    "not-email",
                    "12345678",
                    "혁진",
                    LocalDate.of(2000, 1, 1)
            );

            mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).signup(any(SignupRequest.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 비밀번호 길이 부족")
        void signup_fail_short_password() throws Exception {
            SignupRequest request = new SignupRequest(
                    "test@example.com",
                    "1234",
                    "혁진",
                    LocalDate.of(2000, 1, 1)
            );

            mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).signup(any(SignupRequest.class));
        }
    }

    @Nested
    @DisplayName("일반 로그인 컨트롤러 테스트")
    class LoginControllerTest {

        @Test
        @DisplayName("로그인 성공")
        void login_success() throws Exception {
            LoginRequest request = new LoginRequest("test@example.com", "12345678");
            TokenResponse response = new TokenResponse("Bearer", "access-token", "refresh-token");

            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("로그인 실패 - 이메일 형식 오류")
        void login_fail_invalid_email() throws Exception {
            LoginRequest request = new LoginRequest("wrong-email", "12345678");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any(LoginRequest.class));
        }
    }

    @Nested
    @DisplayName("카카오 컨트롤러 테스트")
    class KakaoControllerTest {

        @Test
        @DisplayName("카카오 로그인 성공")
        void login_kakao_success() throws Exception {
            KakaoLoginRequest request = new KakaoLoginRequest("kakao-token");
            TokenResponse response = new TokenResponse("Bearer", "access-token", "refresh-token");

            when(authService.loginWithKakao(any(KakaoLoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/auth/login/kakao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(authService).loginWithKakao(any(KakaoLoginRequest.class));
        }

        @Test
        @DisplayName("카카오 회원가입 성공")
        void signup_kakao_success() throws Exception {
            KakaoSignupRequest request = new KakaoSignupRequest("kakao-token", "카카오닉네임");
            TokenResponse response = new TokenResponse("Bearer", "access-token", "refresh-token");

            when(authService.signupWithKakao(any(KakaoSignupRequest.class))).thenReturn(response);

            mockMvc.perform(post("/auth/signup/kakao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(authService).signupWithKakao(any(KakaoSignupRequest.class));
        }

        @Test
        @DisplayName("카카오 로그인 실패 - 토큰 누락")
        void login_kakao_fail_blank_token() throws Exception {
            KakaoLoginRequest request = new KakaoLoginRequest("");

            mockMvc.perform(post("/auth/login/kakao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).loginWithKakao(any(KakaoLoginRequest.class));
        }
    }

    @Nested
    @DisplayName("네이버 컨트롤러 테스트")
    class NaverControllerTest {

        @Test
        @DisplayName("네이버 로그인 성공")
        void login_naver_success() throws Exception {
            NaverLoginRequest request = new NaverLoginRequest("naver-token");
            TokenResponse response = new TokenResponse("Bearer", "access-token", "refresh-token");

            when(authService.loginWithNaver(any(NaverLoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/auth/login/naver")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(authService).loginWithNaver(any(NaverLoginRequest.class));
        }

        @Test
        @DisplayName("네이버 회원가입 성공")
        void signup_naver_success() throws Exception {
            NaverSignupRequest request = new NaverSignupRequest("naver-token", "네이버닉네임");
            TokenResponse response = new TokenResponse("Bearer", "access-token", "refresh-token");

            when(authService.signupWithNaver(any(NaverSignupRequest.class))).thenReturn(response);

            mockMvc.perform(post("/auth/signup/naver")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(authService).signupWithNaver(any(NaverSignupRequest.class));
        }

        @Test
        @DisplayName("네이버 회원가입 실패 - 닉네임 누락")
        void signup_naver_fail_blank_nickname() throws Exception {
            NaverSignupRequest request = new NaverSignupRequest("naver-token", "");

            mockMvc.perform(post("/auth/signup/naver")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).signupWithNaver(any(NaverSignupRequest.class));
        }
    }

    @Nested
    @DisplayName("애플 컨트롤러 테스트")
    class AppleControllerTest {

        @Test
        @DisplayName("애플 로그인 성공")
        void login_apple_success() throws Exception {
            AppleLoginRequest request = new AppleLoginRequest("apple-identity-token");
            TokenResponse response = new TokenResponse("Bearer", "access-token", "refresh-token");

            when(authService.loginWithApple(any(AppleLoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/auth/login/apple")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(authService).loginWithApple(any(AppleLoginRequest.class));
        }

        @Test
        @DisplayName("애플 회원가입 성공")
        void signup_apple_success() throws Exception {
            AppleSignupRequest request = new AppleSignupRequest("apple-identity-token", "애플닉네임");
            TokenResponse response = new TokenResponse("Bearer", "access-token", "refresh-token");

            when(authService.signupWithApple(any(AppleSignupRequest.class))).thenReturn(response);

            mockMvc.perform(post("/auth/signup/apple")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(authService).signupWithApple(any(AppleSignupRequest.class));
        }

        @Test
        @DisplayName("애플 로그인 실패 - identity token 누락")
        void login_apple_fail_blank_token() throws Exception {
            AppleLoginRequest request = new AppleLoginRequest("");

            mockMvc.perform(post("/auth/login/apple")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).loginWithApple(any(AppleLoginRequest.class));
        }
    }
}