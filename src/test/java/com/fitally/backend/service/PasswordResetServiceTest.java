package com.fitally.backend.service;

import com.fitally.backend.dto.auth.request.ResetPasswordRequest;
import com.fitally.backend.dto.auth.request.SendPasswordResetCodeRequest;
import com.fitally.backend.dto.auth.request.VerifyPasswordResetCodeRequest;
import com.fitally.backend.dto.auth.response.VerifyPasswordResetCodeResponse;
import com.fitally.backend.entity.PasswordResetVerification;
import com.fitally.backend.entity.User;
import com.fitally.backend.repository.PasswordResetVerificationRepository;
import com.fitally.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PasswordResetService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetVerificationRepository passwordResetVerificationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "codeExpirationMinutes", 10L);
    }

    @Test
    @DisplayName("인증코드 발송 성공 - 이메일 회원이면 인증코드를 저장하고 메일을 보낸다")
    void sendCode_success() throws Exception {
        // given
        String email = "test@test.com";

        SendPasswordResetCodeRequest request = createSendCodeRequest(email);

        User user = mock(User.class);
        when(user.isEmailUser()).thenReturn(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordResetVerificationRepository.findAllByEmailAndUsedFalseOrderByCreatedAtDesc(email))
                .thenReturn(List.of());

        // when
        passwordResetService.sendCode(request);

        // then
        verify(passwordResetVerificationRepository, times(1)).save(any(PasswordResetVerification.class));
        verify(emailService, times(1)).sendPasswordResetCode(eq(email), anyString());
    }

    @Test
    @DisplayName("인증코드 발송 무시 - 존재하지 않는 이메일이면 아무 동작도 하지 않는다")
    void sendCode_userNotFound() throws Exception {
        // given
        String email = "notfound@test.com";
        SendPasswordResetCodeRequest request = createSendCodeRequest(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when
        passwordResetService.sendCode(request);

        // then
        verify(passwordResetVerificationRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetCode(anyString(), anyString());
    }

    @Test
    @DisplayName("인증코드 발송 무시 - 소셜 회원이면 아무 동작도 하지 않는다")
    void sendCode_socialUser() throws Exception {
        // given
        String email = "social@test.com";
        SendPasswordResetCodeRequest request = createSendCodeRequest(email);

        User user = mock(User.class);
        when(user.isEmailUser()).thenReturn(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        passwordResetService.sendCode(request);

        // then
        verify(passwordResetVerificationRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetCode(anyString(), anyString());
    }

    @Test
    @DisplayName("인증코드 검증 성공 - 코드가 일치하면 verifyToken을 발급한다")
    void verifyCode_success() throws Exception {
        // given
        String email = "test@test.com";
        String code = "123456";

        VerifyPasswordResetCodeRequest request = createVerifyCodeRequest(email, code);

        PasswordResetVerification verification = new PasswordResetVerification(
                email,
                code,
                LocalDateTime.now().plusMinutes(10)
        );

        when(passwordResetVerificationRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email))
                .thenReturn(Optional.of(verification));

        // when
        VerifyPasswordResetCodeResponse response = passwordResetService.verifyCode(request);

        // then
        assertNotNull(response);
        assertEquals("인증이 완료되었습니다.", response.getMessage());
        assertNotNull(response.getVerifyToken());
        assertFalse(response.getVerifyToken().isBlank());

        assertTrue(verification.isVerified());
        assertEquals(response.getVerifyToken(), verification.getVerifyToken());
    }

    @Test
    @DisplayName("인증코드 검증 실패 - 코드가 다르면 예외가 발생한다")
    void verifyCode_fail_wrongCode() throws Exception {
        // given
        String email = "test@test.com";

        VerifyPasswordResetCodeRequest request = createVerifyCodeRequest(email, "999999");

        PasswordResetVerification verification = new PasswordResetVerification(
                email,
                "123456",
                LocalDateTime.now().plusMinutes(10)
        );

        when(passwordResetVerificationRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email))
                .thenReturn(Optional.of(verification));

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.verifyCode(request)
        );

        assertEquals("인증코드가 일치하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("인증코드 검증 실패 - 코드가 만료되면 예외가 발생한다")
    void verifyCode_fail_expired() throws Exception {
        // given
        String email = "test@test.com";
        String code = "123456";

        VerifyPasswordResetCodeRequest request = createVerifyCodeRequest(email, code);

        PasswordResetVerification verification = new PasswordResetVerification(
                email,
                code,
                LocalDateTime.now().minusMinutes(1)
        );

        when(passwordResetVerificationRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email))
                .thenReturn(Optional.of(verification));

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.verifyCode(request)
        );

        assertEquals("인증코드가 만료되었습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 성공 - verifyToken이 유효하면 비밀번호를 변경하고 인증 정보를 사용 처리한다")
    void resetPassword_success() throws Exception {
        // given
        String email = "test@test.com";
        String verifyToken = "verify-token-value";
        String rawPassword = "newPassword123!";
        String encodedPassword = "encoded-password";

        ResetPasswordRequest request = createResetPasswordRequest(email, verifyToken, rawPassword);

        User user = mock(User.class);
        when(user.isEmailUser()).thenReturn(true);

        PasswordResetVerification verification = new PasswordResetVerification(
                email,
                "123456",
                LocalDateTime.now().plusMinutes(10)
        );
        verification.verify(verifyToken);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordResetVerificationRepository.findByEmailAndVerifyTokenAndVerifiedTrueAndUsedFalse(email, verifyToken))
                .thenReturn(Optional.of(verification));
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // when
        passwordResetService.resetPassword(request);

        // then
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(user, times(1)).changePassword(encodedPassword);
        assertTrue(verification.isUsed());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 사용자가 없으면 예외가 발생한다")
    void resetPassword_fail_userNotFound() throws Exception {
        // given
        String email = "notfound@test.com";
        ResetPasswordRequest request = createResetPasswordRequest(email, "verify-token", "newPassword123!");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(request)
        );

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 인증 정보가 없으면 예외가 발생한다")
    void resetPassword_fail_invalidVerification() throws Exception {
        // given
        String email = "test@test.com";
        String verifyToken = "wrong-token";

        ResetPasswordRequest request = createResetPasswordRequest(email, verifyToken, "newPassword123!");

        User user = mock(User.class);
        when(user.isEmailUser()).thenReturn(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordResetVerificationRepository.findByEmailAndVerifyTokenAndVerifiedTrueAndUsedFalse(email, verifyToken))
                .thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(request)
        );

        assertEquals("유효하지 않은 인증 정보입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 인증 정보가 만료되면 예외가 발생한다")
    void resetPassword_fail_expiredVerification() throws Exception {
        // given
        String email = "test@test.com";
        String verifyToken = "verify-token";

        ResetPasswordRequest request = createResetPasswordRequest(email, verifyToken, "newPassword123!");

        User user = mock(User.class);
        when(user.isEmailUser()).thenReturn(true);

        PasswordResetVerification verification = new PasswordResetVerification(
                email,
                "123456",
                LocalDateTime.now().minusMinutes(1)
        );
        verification.verify(verifyToken);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordResetVerificationRepository.findByEmailAndVerifyTokenAndVerifiedTrueAndUsedFalse(email, verifyToken))
                .thenReturn(Optional.of(verification));

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(request)
        );

        assertEquals("인증 정보가 만료되었습니다.", exception.getMessage());
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
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}