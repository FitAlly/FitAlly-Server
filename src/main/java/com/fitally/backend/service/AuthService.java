package com.fitally.backend.service;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.dto.auth.request.KakaoLoginRequest;
import com.fitally.backend.dto.auth.request.KakaoSignupRequest;
import com.fitally.backend.dto.auth.request.LoginRequest;
import com.fitally.backend.dto.auth.request.SignupRequest;
import com.fitally.backend.dto.auth.response.KakaoUserInfo;
import com.fitally.backend.dto.auth.response.SignupResponse;
import com.fitally.backend.dto.auth.response.TokenResponse;
import com.fitally.backend.entity.User;
import com.fitally.backend.external.kakao.KakaoApliClient;
import com.fitally.backend.repository.UserRepository;
import com.fitally.backend.security.jwt.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoApliClient kakaoApliClient;



    public SignupResponse signup(SignupRequest request) {
        validateDuplicateEmail(request.getEmail());
        validateDuplicateNickname(request.getNickname());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.createEmailUser(
                request.getEmail(),
                encodedPassword,
                request.getNickname(),
                request.getBirthdate()
        );

        User savedUser = userRepository.save(user);

        return new SignupResponse(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getProvider(),
                savedUser.getBirthdate()
        );
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_PROVIDER));

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!matches) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        return new TokenResponse("Bearer", accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenResponse loginWithKakao(KakaoLoginRequest request) {
        KakaoUserInfo kakaoUserInfo = kakaoApliClient.getUserInfo(request.getSocialAccessToken());

        User user = userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("kakao", kakaoUserInfo.getProviderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SOCIAL_SIGNUP_REQUIRED));

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        return new TokenResponse("Bearer", accessToken, refreshToken);
    }

    public TokenResponse signupWithKakao(KakaoSignupRequest request) {
        KakaoUserInfo kakaoUserInfo = kakaoApliClient.getUserInfo(request.getSocialAccessToken());

        if (kakaoUserInfo.getEmail() == null || kakaoUserInfo.getEmail().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("kakao", kakaoUserInfo.getProviderId())
                .ifPresent(user -> {
                   throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
                });

        userRepository.findByEmailAndDeletedAtIsNull(kakaoUserInfo.getEmail())
                .ifPresent(existingUser -> {
                    if (!"kakao".equalsIgnoreCase(existingUser.getProvider())){
                        throw new BusinessException(ErrorCode.INVALID_LOGIN_PROVIDER);
                    }
                });

        validateDuplicateNickname(request.getNickname());

        User user = User.createSocialUser(
                kakaoUserInfo.getEmail(),
                "kakao",
                kakaoUserInfo.getProviderId(),
                request.getNickname(),
                kakaoUserInfo.getProfileImageUrl()
        );

        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.createAccessToken(savedUser);
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser);

        return new TokenResponse("Bearer", accessToken, refreshToken);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNicknameAndDeletedAtIsNull(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }
}
