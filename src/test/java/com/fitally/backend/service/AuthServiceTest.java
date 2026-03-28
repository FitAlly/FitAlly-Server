package com.fitally.backend.service;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.dto.auth.request.LoginRequest;
import com.fitally.backend.dto.auth.request.SignupRequest;
import com.fitally.backend.dto.auth.response.SignupResponse;
import com.fitally.backend.dto.auth.response.TokenResponse;
import com.fitally.backend.entity.User;
import com.fitally.backend.repository.UserRepository;
import com.fitally.backend.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTest {

        @Test
        @DisplayName("회원가입 성공")
        void signup_success() {
            // given
            LocalDate birthdate = LocalDate.of(2000, 1, 1);

            SignupRequest request = new SignupRequest(
                    "test@example.com",
                    "1234",
                    "혁진",
                    birthdate
            );

            when(userRepository.existsByEmailAndDeletedAtIsNull("test@example.com")).thenReturn(false);
            when(userRepository.existsByNicknameAndDeletedAtIsNull("혁진")).thenReturn(false);
            when(passwordEncoder.encode("1234")).thenReturn("encoded-password");

            User savedUser = User.createEmailUser(
                    "test@example.com",
                    "encoded-password",
                    "혁진",
                    birthdate
            );

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // when
            SignupResponse response = authService.signup(request);

            // then
            assertNotNull(response);
            assertEquals("test@example.com", response.getEmail());
            assertEquals("혁진", response.getNickname());
            assertEquals("email", response.getProvider());
            assertEquals(birthdate, response.getBirthdate());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertEquals("test@example.com", capturedUser.getEmail());
            assertEquals("encoded-password", capturedUser.getPasswordHash());
            assertEquals("혁진", capturedUser.getNickname());
            assertEquals(birthdate, capturedUser.getBirthdate());
        }

        @Test
        @DisplayName("회원가입 실패 - 이메일 중복")
        void signup_fail_duplicate_email() {
            // given
            SignupRequest request = new SignupRequest(
                    "test@example.com",
                    "1234",
                    "혁진",
                    LocalDate.of(2000, 1, 1)
            );

            when(userRepository.existsByEmailAndDeletedAtIsNull("test@example.com")).thenReturn(true);

            // when
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signup(request)
            );

            // then
            assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.getErrorCode());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 닉네임 중복")
        void signup_fail_duplicate_nickname() {
            // given
            SignupRequest request = new SignupRequest(
                    "test@example.com",
                    "1234",
                    "혁진",
                    LocalDate.of(2000, 1, 1)
            );

            when(userRepository.existsByEmailAndDeletedAtIsNull("test@example.com")).thenReturn(false);
            when(userRepository.existsByNicknameAndDeletedAtIsNull("혁진")).thenReturn(true);

            // when
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signup(request)
            );

            // then
            assertEquals(ErrorCode.DUPLICATE_NICKNAME, exception.getErrorCode());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공")
        void login_success() {
            // given
            LoginRequest request = new LoginRequest("test@example.com", "1234");

            User user = User.createEmailUser(
                    "test@example.com",
                    "encoded-password",
                    "혁진",
                    LocalDate.of(2000, 1, 1)
            );

            when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
                    .thenReturn(Optional.of(user));
            when(passwordEncoder.matches("1234", "encoded-password")).thenReturn(true);
            when(jwtTokenProvider.createAccessToken(user)).thenReturn("access-token");
            when(jwtTokenProvider.createRefreshToken(user)).thenReturn("refresh-token");

            // when
            TokenResponse response = authService.login(request);

            // then
            assertNotNull(response);
            assertEquals("Bearer", response.getGrantType());
            assertEquals("access-token", response.getAccessToken());
            assertEquals("refresh-token", response.getRefreshToken());
        }

        @Test
        @DisplayName("로그인 실패 - 존재하지 않는 이메일")
        void login_fail_user_not_found() {
            // given
            LoginRequest request = new LoginRequest("notfound@example.com", "1234");

            when(userRepository.findByEmailAndDeletedAtIsNull("notfound@example.com"))
                    .thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.login(request)
            );

            // then
            assertEquals(ErrorCode.INVALID_LOGIN_PROVIDER, exception.getErrorCode());
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호 불일치")
        void login_fail_wrong_password() {
            // given
            LoginRequest request = new LoginRequest("test@example.com", "wrong-password");

            User user = User.createEmailUser(
                    "test@example.com",
                    "encoded-password",
                    "혁진",
                    LocalDate.of(2000, 1, 1)
            );

            when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
                    .thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

            // when
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.login(request)
            );

            // then
            assertEquals(ErrorCode.LOGIN_FAILED, exception.getErrorCode());
        }
    }
}