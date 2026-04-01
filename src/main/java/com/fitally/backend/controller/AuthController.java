package com.fitally.backend.controller;

import com.fitally.backend.common.response.ApiResponse;
import com.fitally.backend.dto.auth.request.KakaoLoginRequest;
import com.fitally.backend.dto.auth.request.KakaoSignupRequest;
import com.fitally.backend.dto.auth.request.LoginRequest;
import com.fitally.backend.dto.auth.request.SignupRequest;
import com.fitally.backend.dto.auth.response.SignupResponse;
import com.fitally.backend.dto.auth.response.TokenResponse;
import com.fitally.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("201", "회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("200", "로그인이 완료되었습니다.", response));
    }

    @PostMapping("/login/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> loginKakao(@Valid @RequestBody KakaoLoginRequest request) {
        TokenResponse response = authService.loginWithKakao(request);
        return ResponseEntity.ok(ApiResponse.success("200", "카카오 로그인이 완료되었습니다.", response));
    }

    @PostMapping("/signup/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> signupKakao(@Valid @RequestBody KakaoSignupRequest request) {
        TokenResponse response = authService.signupWithKakao(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("201", "카카오 회원가입이 완료되었습니다.", response));
    }
}
