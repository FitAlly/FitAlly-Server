package com.fitally.backend.service;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.dto.auth.request.*;
import com.fitally.backend.dto.auth.response.SocialUserInfo;
import com.fitally.backend.dto.auth.response.SignupResponse;
import com.fitally.backend.dto.auth.response.TokenResponse;
import com.fitally.backend.entity.User;
import com.fitally.backend.external.apple.AppleTokenVerifier;
import com.fitally.backend.external.kakao.KakaoApliClient;
import com.fitally.backend.external.naver.NaverApiClient;
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
    private final NaverApiClient naverApiClient;
    private final AppleTokenVerifier appleTokenVerifier;



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
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (!"email".equalsIgnoreCase(user.getProvider())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_PROVIDER);
        }

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!matches) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        return new TokenResponse("Bearer", accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenResponse loginWithNaver(NaverLoginRequest request) {
        SocialUserInfo socialUserInfo = naverApiClient.getUserInfo(request.getSocialAccessToken());

        User user = userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("naver", socialUserInfo.getProviderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SOCIAL_SIGNUP_REQUIRED));

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        return new TokenResponse("Bearer", accessToken, refreshToken);
    }

    public TokenResponse signupWithNaver(NaverSignupRequest request) {
        SocialUserInfo socialUserInfo = naverApiClient.getUserInfo(request.getSocialAccessToken());

        validateSocialSignup("naver", socialUserInfo, request.getNickname());

        User user = User.createSocialUser(
                socialUserInfo.getEmail(),
                "naver",
                socialUserInfo.getProviderId(),
                request.getNickname(),
                socialUserInfo.getProfileImageUrl()
        );

        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.createAccessToken(savedUser);
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser);

        return new TokenResponse("Bearer", accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenResponse loginWithApple(AppleLoginRequest request) {
        SocialUserInfo socialUserInfo = appleTokenVerifier.verify(request.getIdentityToken());

        User user =  userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("apple", socialUserInfo.getProviderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SOCIAL_SIGNUP_REQUIRED));

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        return new TokenResponse("Bearer", accessToken, refreshToken);
    }

    public TokenResponse signupWithApple(AppleSignupRequest request) {
        SocialUserInfo socialUserInfo = appleTokenVerifier.verify(request.getIdentityToken());

        validateSocialSignup("apple", socialUserInfo, request.getNickname());

        User user = User.createSocialUser(
                socialUserInfo.getEmail(),
                "apple",
                socialUserInfo.getProviderId(),
                request.getNickname(),
                socialUserInfo.getProfileImageUrl()
        );

        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.createAccessToken(savedUser);
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser);

        return new TokenResponse("Bearer", accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenResponse loginWithKakao(KakaoLoginRequest request) {
        SocialUserInfo socialUserInfo = kakaoApliClient.getUserInfo(request.getSocialAccessToken());

        User user = userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("kakao", socialUserInfo.getProviderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SOCIAL_SIGNUP_REQUIRED));

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        return new TokenResponse("Bearer", accessToken, refreshToken);
    }

    public TokenResponse signupWithKakao(KakaoSignupRequest request) {
        SocialUserInfo socialUserInfo = kakaoApliClient.getUserInfo(request.getSocialAccessToken());

        if (socialUserInfo.getEmail() == null || socialUserInfo.getEmail().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("kakao", socialUserInfo.getProviderId())
                .ifPresent(user -> {
                   throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
                });

        userRepository.findByEmailAndDeletedAtIsNull(socialUserInfo.getEmail())
                .ifPresent(existingUser -> {
                    if (!"kakao".equalsIgnoreCase(existingUser.getProvider())){
                        throw new BusinessException(ErrorCode.INVALID_LOGIN_PROVIDER);
                    }
                });

        validateDuplicateNickname(request.getNickname());

        User user = User.createSocialUser(
                socialUserInfo.getEmail(),
                "kakao",
                socialUserInfo.getProviderId(),
                request.getNickname(),
                socialUserInfo.getProfileImageUrl()
        );

        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.createAccessToken(savedUser);
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser);

        return new TokenResponse("Bearer", accessToken, refreshToken);
    }

    private void validateSocialSignup(String provider, SocialUserInfo socialUserInfo, String nickname) {
        if (socialUserInfo.getProviderId() == null || socialUserInfo.getProviderId().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        userRepository.findByProviderAndProviderIdAndDeletedAtIsNull(provider, socialUserInfo.getProviderId())
                .ifPresent(user -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
                });

        if (socialUserInfo.getEmail() != null && !socialUserInfo.getEmail().isBlank()) {
            userRepository.findByEmailAndDeletedAtIsNull(socialUserInfo.getEmail())
                    .ifPresent(existingUser -> {
                        if (!provider.equalsIgnoreCase(existingUser.getProvider())) {
                            throw new BusinessException(ErrorCode.INVALID_LOGIN_PROVIDER);
                        }
                    });
        }

        validateDuplicateNickname(nickname);
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
