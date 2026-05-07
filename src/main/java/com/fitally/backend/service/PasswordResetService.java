package com.fitally.backend.service;

import com.fitally.backend.dto.auth.request.ResetPasswordRequest;
import com.fitally.backend.dto.auth.request.SendPasswordResetCodeRequest;
import com.fitally.backend.dto.auth.request.VerifyPasswordResetCodeRequest;
import com.fitally.backend.dto.auth.response.VerifyPasswordResetCodeResponse;
import com.fitally.backend.entity.PasswordResetVerification;
import com.fitally.backend.entity.User;
import com.fitally.backend.repository.PasswordResetVerificationRepository;
import com.fitally.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetVerificationRepository passwordResetVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset.code-expiration-minutes:10}")
    private long codeExpirationMinutes;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetVerificationRepository passwordResetVerificationRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetVerificationRepository = passwordResetVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void sendCode(SendPasswordResetCodeRequest request) {
        log.info("[비밀번호 찾기] 인증코드 발송요청 email={}" + request.getEmail());

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            log.warn("[비밀번호 찾기] 사용자를 찾지 못했습니다. email={}", request.getEmail());
            return;
        }

        User user = optionalUser.get();
        log.info("[비밀번호 찾기] 사용자 조회 성공 email={}, provider={}", user.getEmail(), user.getProvider());

        if (!user.isEmailUser()) {
            log.warn("[비밀번호 찾기] 이메일 회원이 아니어서 발송 중단 email={}, provider={}", user.getEmail(), user.getProvider());
            return;
        }

        List<PasswordResetVerification> oldVerifications =
                passwordResetVerificationRepository.findAllByEmailAndUsedFalseOrderByCreatedAtDesc(request.getEmail());

        for (PasswordResetVerification verification : oldVerifications) {
            verification.markUsed();
        }

        String code = generateSixDigitCode();
        log.info("[비밀번호 찾기] 인증코드 생성 email={}, code={}", request.getEmail(), code);

        PasswordResetVerification verification = new PasswordResetVerification(
                request.getEmail(),
                code,
                LocalDateTime.now().plusMinutes(codeExpirationMinutes)
        );

        passwordResetVerificationRepository.save(verification);
        log.info("[비밀번호 찾기] 인증정보 저장 완료 email={}", request.getEmail());

        emailService.sendPasswordResetCode(request.getEmail(), code);
        log.info("[비밀번호 찾기] 이메일 발송 요청 완료 email={}", request.getEmail());
    }

    public VerifyPasswordResetCodeResponse verifyCode(VerifyPasswordResetCodeRequest request) {
        PasswordResetVerification verification = passwordResetVerificationRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("인증 요청을 먼저 진행해주세요."));

        if (verification.isExpired()) {
            throw new IllegalArgumentException("인증코드가 만료되었습니다.");
        }

        if (!verification.getCode().equals(request.getCode())) {
            throw new IllegalArgumentException("인증코드가 일치하지 않습니다.");
        }

        String verifyToken = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        verification.verify(verifyToken);

        return new VerifyPasswordResetCodeResponse("인증이 완료되었습니다.", verifyToken);
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.isEmailUser()) {
            throw new IllegalArgumentException("이메일 회원만 비밀번호 변경이 가능합니다.");
        }

        PasswordResetVerification verification = passwordResetVerificationRepository
                .findByEmailAndVerifyTokenAndVerifiedTrueAndUsedFalse(request.getEmail(), request.getVerifyToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 정보입니다."));

        if (verification.isExpired()) {
            throw new IllegalArgumentException("인증 정보가 만료되었습니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(encodedPassword);

        verification.markUsed();
    }

    private String generateSixDigitCode() {
        int number = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(number);
    }
}