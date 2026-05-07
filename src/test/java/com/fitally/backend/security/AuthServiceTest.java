package com.fitally.backend.security;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.dto.auth.request.AppleLoginRequest;
import com.fitally.backend.dto.auth.request.AppleSignupRequest;
import com.fitally.backend.dto.auth.request.KakaoLoginRequest;
import com.fitally.backend.dto.auth.request.KakaoSignupRequest;
import com.fitally.backend.dto.auth.request.LoginRequest;
import com.fitally.backend.dto.auth.request.NaverLoginRequest;
import com.fitally.backend.dto.auth.request.NaverSignupRequest;
import com.fitally.backend.dto.auth.request.SignupRequest;
import com.fitally.backend.dto.auth.response.SocialUserInfo;
import com.fitally.backend.dto.auth.response.SignupResponse;
import com.fitally.backend.dto.auth.response.TokenResponse;
import com.fitally.backend.entity.User;
import com.fitally.backend.external.apple.AppleTokenVerifier;
import com.fitally.backend.external.kakao.KakaoApiClient;
import com.fitally.backend.external.naver.NaverApiClient;
import com.fitally.backend.repository.UserRepository;
import com.fitally.backend.security.jwt.JwtTokenProvider;
import com.fitally.backend.service.AuthService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private NaverApiClient naverApiClient;

    @Mock
    private AppleTokenVerifier appleTokenVerifier;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("일반 회원가입 테스트")
    class SignupTest {

        @Test
        @DisplayName("회원가입 성공")
        void signup_success() {
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

            SignupResponse response = authService.signup(request);

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
            SignupRequest request = new SignupRequest(
                    "test@example.com",
                    "1234",
                    "혁진",
                    LocalDate.of(2000, 1, 1)
            );

            when(userRepository.existsByEmailAndDeletedAtIsNull("test@example.com")).thenReturn(true);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signup(request)
            );

            assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.getErrorCode());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 닉네임 중복")
        void signup_fail_duplicate_nickname() {
            SignupRequest request = new SignupRequest(
                    "test@example.com",
                    "1234",
                    "혁진",
                    LocalDate.of(2000, 1, 1)
            );

            when(userRepository.existsByEmailAndDeletedAtIsNull("test@example.com")).thenReturn(false);
            when(userRepository.existsByNicknameAndDeletedAtIsNull("혁진")).thenReturn(true);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signup(request)
            );

            assertEquals(ErrorCode.DUPLICATE_NICKNAME, exception.getErrorCode());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("일반 로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공")
        void login_success() {
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

            TokenResponse response = authService.login(request);

            assertNotNull(response);
            assertEquals("Bearer", response.getGrantType());
            assertEquals("access-token", response.getAccessToken());
            assertEquals("refresh-token", response.getRefreshToken());
        }

        @Test
        @DisplayName("로그인 실패 - 존재하지 않는 이메일")
        void login_fail_user_not_found() {
            LoginRequest request = new LoginRequest("notfound@example.com", "1234");

            when(userRepository.findByEmailAndDeletedAtIsNull("notfound@example.com"))
                    .thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.login(request)
            );

            assertEquals(ErrorCode.LOGIN_FAILED, exception.getErrorCode());
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호 불일치")
        void login_fail_wrong_password() {
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

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.login(request)
            );

            assertEquals(ErrorCode.LOGIN_FAILED, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("카카오 로그인 테스트")
    class KakaoLoginTest {

        @Test
        @DisplayName("카카오 로그인 성공")
        void login_with_kakao_success() {
            KakaoLoginRequest request = new KakaoLoginRequest("kakao-access-token");

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "kakao",
                    "123456",
                    "kakao@test.com",
                    "카카오유저",
                    "http://image.test/profile.png"
            );

            User user = User.createSocialUser(
                    "kakao@test.com",
                    "kakao",
                    "123456",
                    "카카오유저",
                    "http://image.test/profile.png"
            );

            when(kakaoApiClient.getUserInfo("kakao-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("kakao", "123456"))
                    .thenReturn(Optional.of(user));
            when(jwtTokenProvider.createAccessToken(user)).thenReturn("access-token");
            when(jwtTokenProvider.createRefreshToken(user)).thenReturn("refresh-token");

            TokenResponse response = authService.loginWithKakao(request);

            assertNotNull(response);
            assertEquals("Bearer", response.getGrantType());
            assertEquals("access-token", response.getAccessToken());
            assertEquals("refresh-token", response.getRefreshToken());
        }

        @Test
        @DisplayName("카카오 로그인 실패 - 회원가입 필요")
        void login_with_kakao_fail_signup_required() {
            KakaoLoginRequest request = new KakaoLoginRequest("kakao-access-token");

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "kakao",
                    "123456",
                    "kakao@test.com",
                    "카카오유저",
                    "http://image.test/profile.png"
            );

            when(kakaoApiClient.getUserInfo("kakao-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("kakao", "123456"))
                    .thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.loginWithKakao(request)
            );

            assertEquals(ErrorCode.SOCIAL_SIGNUP_REQUIRED, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("카카오 회원가입 테스트")
    class KakaoSignupTest {

        @Test
        @DisplayName("카카오 회원가입 성공")
        void signup_with_kakao_success() {
            KakaoSignupRequest request = new KakaoSignupRequest(
                    "kakao-access-token",
                    "새닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "kakao",
                    "123456",
                    "kakao@test.com",
                    "카카오기본닉네임",
                    "http://image.test/profile.png"
            );

            User savedUser = User.createSocialUser(
                    "kakao@test.com",
                    "kakao",
                    "123456",
                    "새닉네임",
                    "http://image.test/profile.png"
            );

            when(kakaoApiClient.getUserInfo("kakao-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("kakao", "123456"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDeletedAtIsNull("kakao@test.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByNicknameAndDeletedAtIsNull("새닉네임"))
                    .thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtTokenProvider.createAccessToken(savedUser)).thenReturn("access-token");
            when(jwtTokenProvider.createRefreshToken(savedUser)).thenReturn("refresh-token");

            TokenResponse response = authService.signupWithKakao(request);

            assertNotNull(response);
            assertEquals("Bearer", response.getGrantType());
            assertEquals("access-token", response.getAccessToken());
            assertEquals("refresh-token", response.getRefreshToken());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertEquals("kakao@test.com", capturedUser.getEmail());
            assertEquals("kakao", capturedUser.getProvider());
            assertEquals("123456", capturedUser.getProviderId());
            assertEquals("새닉네임", capturedUser.getNickname());
        }

        @Test
        @DisplayName("카카오 회원가입 실패 - 이메일 없음")
        void signup_with_kakao_fail_no_email() {
            KakaoSignupRequest request = new KakaoSignupRequest(
                    "kakao-access-token",
                    "새닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "kakao",
                    "123456",
                    null,
                    "카카오기본닉네임",
                    "http://image.test/profile.png"
            );

            when(kakaoApiClient.getUserInfo("kakao-access-token")).thenReturn(socialUserInfo);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signupWithKakao(request)
            );

            assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        }

        @Test
        @DisplayName("카카오 회원가입 실패 - 이미 카카오 회원 존재")
        void signup_with_kakao_fail_duplicate_social_user() {
            KakaoSignupRequest request = new KakaoSignupRequest(
                    "kakao-access-token",
                    "새닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "kakao",
                    "123456",
                    "kakao@test.com",
                    "카카오기본닉네임",
                    "http://image.test/profile.png"
            );

            User existingKakaoUser = User.createSocialUser(
                    "kakao@test.com",
                    "kakao",
                    "123456",
                    "기존카카오회원",
                    "http://image.test/profile.png"
            );

            when(kakaoApiClient.getUserInfo("kakao-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("kakao", "123456"))
                    .thenReturn(Optional.of(existingKakaoUser));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signupWithKakao(request)
            );

            assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.getErrorCode());
        }

        @Test
        @DisplayName("카카오 회원가입 실패 - 같은 이메일의 일반 회원 존재")
        void signup_with_kakao_fail_invalid_login_provider() {
            KakaoSignupRequest request = new KakaoSignupRequest(
                    "kakao-access-token",
                    "새닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "kakao",
                    "123456",
                    "kakao@test.com",
                    "카카오기본닉네임",
                    "http://image.test/profile.png"
            );

            User emailUser = User.createEmailUser(
                    "kakao@test.com",
                    "encoded-password",
                    "일반회원",
                    null
            );

            when(kakaoApiClient.getUserInfo("kakao-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("kakao", "123456"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDeletedAtIsNull("kakao@test.com"))
                    .thenReturn(Optional.of(emailUser));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signupWithKakao(request)
            );

            assertEquals(ErrorCode.INVALID_LOGIN_PROVIDER, exception.getErrorCode());
        }

        @Test
        @DisplayName("카카오 회원가입 실패 - 닉네임 중복")
        void signup_with_kakao_fail_duplicate_nickname() {
            KakaoSignupRequest request = new KakaoSignupRequest(
                    "kakao-access-token",
                    "중복닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "kakao",
                    "123456",
                    "kakao@test.com",
                    "카카오기본닉네임",
                    "http://image.test/profile.png"
            );

            when(kakaoApiClient.getUserInfo("kakao-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("kakao", "123456"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDeletedAtIsNull("kakao@test.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByNicknameAndDeletedAtIsNull("중복닉네임"))
                    .thenReturn(true);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signupWithKakao(request)
            );

            assertEquals(ErrorCode.DUPLICATE_NICKNAME, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("네이버 로그인 테스트")
    class NaverLoginTest {

        @Test
        @DisplayName("네이버 로그인 성공")
        void login_with_naver_success() {
            NaverLoginRequest request = new NaverLoginRequest("naver-access-token");

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "naver",
                    "naver-123456",
                    "naver@test.com",
                    "네이버유저",
                    "http://image.test/naver.png"
            );

            User user = User.createSocialUser(
                    "naver@test.com",
                    "naver",
                    "naver-123456",
                    "네이버유저",
                    "http://image.test/naver.png"
            );

            when(naverApiClient.getUserInfo("naver-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("naver", "naver-123456"))
                    .thenReturn(Optional.of(user));
            when(jwtTokenProvider.createAccessToken(user)).thenReturn("access-token");
            when(jwtTokenProvider.createRefreshToken(user)).thenReturn("refresh-token");

            TokenResponse response = authService.loginWithNaver(request);

            assertNotNull(response);
            assertEquals("Bearer", response.getGrantType());
            assertEquals("access-token", response.getAccessToken());
            assertEquals("refresh-token", response.getRefreshToken());
        }

        @Test
        @DisplayName("네이버 로그인 실패 - 회원가입 필요")
        void login_with_naver_fail_signup_required() {
            NaverLoginRequest request = new NaverLoginRequest("naver-access-token");

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "naver",
                    "naver-123456",
                    "naver@test.com",
                    "네이버유저",
                    "http://image.test/naver.png"
            );

            when(naverApiClient.getUserInfo("naver-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("naver", "naver-123456"))
                    .thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.loginWithNaver(request)
            );

            assertEquals(ErrorCode.SOCIAL_SIGNUP_REQUIRED, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("네이버 회원가입 테스트")
    class NaverSignupTest {

        @Test
        @DisplayName("네이버 회원가입 성공")
        void signup_with_naver_success() {
            NaverSignupRequest request = new NaverSignupRequest(
                    "naver-access-token",
                    "네이버닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "naver",
                    "naver-123456",
                    "naver@test.com",
                    "네이버기본닉네임",
                    "http://image.test/naver.png"
            );

            User savedUser = User.createSocialUser(
                    "naver@test.com",
                    "naver",
                    "naver-123456",
                    "네이버닉네임",
                    "http://image.test/naver.png"
            );

            when(naverApiClient.getUserInfo("naver-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("naver", "naver-123456"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDeletedAtIsNull("naver@test.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByNicknameAndDeletedAtIsNull("네이버닉네임"))
                    .thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtTokenProvider.createAccessToken(savedUser)).thenReturn("access-token");
            when(jwtTokenProvider.createRefreshToken(savedUser)).thenReturn("refresh-token");

            TokenResponse response = authService.signupWithNaver(request);

            assertNotNull(response);
            assertEquals("Bearer", response.getGrantType());
            assertEquals("access-token", response.getAccessToken());
            assertEquals("refresh-token", response.getRefreshToken());
        }

        @Test
        @DisplayName("네이버 회원가입 실패 - 이미 네이버 회원 존재")
        void signup_with_naver_fail_duplicate_social_user() {
            NaverSignupRequest request = new NaverSignupRequest(
                    "naver-access-token",
                    "네이버닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "naver",
                    "naver-123456",
                    "naver@test.com",
                    "네이버기본닉네임",
                    "http://image.test/naver.png"
            );

            User existingNaverUser = User.createSocialUser(
                    "naver@test.com",
                    "naver",
                    "naver-123456",
                    "기존네이버회원",
                    "http://image.test/naver.png"
            );

            when(naverApiClient.getUserInfo("naver-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("naver", "naver-123456"))
                    .thenReturn(Optional.of(existingNaverUser));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signupWithNaver(request)
            );

            assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.getErrorCode());
        }

        @Test
        @DisplayName("네이버 회원가입 실패 - 같은 이메일의 일반 회원 존재")
        void signup_with_naver_fail_invalid_login_provider() {
            NaverSignupRequest request = new NaverSignupRequest(
                    "naver-access-token",
                    "네이버닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "naver",
                    "naver-123456",
                    "naver@test.com",
                    "네이버기본닉네임",
                    "http://image.test/naver.png"
            );

            User emailUser = User.createEmailUser(
                    "naver@test.com",
                    "encoded-password",
                    "일반회원",
                    null
            );

            when(naverApiClient.getUserInfo("naver-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("naver", "naver-123456"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDeletedAtIsNull("naver@test.com"))
                    .thenReturn(Optional.of(emailUser));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signupWithNaver(request)
            );

            assertEquals(ErrorCode.INVALID_LOGIN_PROVIDER, exception.getErrorCode());
        }

        @Test
        @DisplayName("네이버 회원가입 실패 - 닉네임 중복")
        void signup_with_naver_fail_duplicate_nickname() {
            NaverSignupRequest request = new NaverSignupRequest(
                    "naver-access-token",
                    "중복닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "naver",
                    "naver-123456",
                    "naver@test.com",
                    "네이버기본닉네임",
                    "http://image.test/naver.png"
            );

            when(naverApiClient.getUserInfo("naver-access-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("naver", "naver-123456"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDeletedAtIsNull("naver@test.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByNicknameAndDeletedAtIsNull("중복닉네임"))
                    .thenReturn(true);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signupWithNaver(request)
            );

            assertEquals(ErrorCode.DUPLICATE_NICKNAME, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("애플 로그인 테스트")
    class AppleLoginTest {

        @Test
        @DisplayName("애플 로그인 성공")
        void login_with_apple_success() {
            AppleLoginRequest request = new AppleLoginRequest("apple-identity-token");

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "apple",
                    "apple-sub-123456",
                    "apple@test.com",
                    null,
                    null
            );

            User user = User.createSocialUser(
                    "apple@test.com",
                    "apple",
                    "apple-sub-123456",
                    "애플유저",
                    null
            );

            when(appleTokenVerifier.verify("apple-identity-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("apple", "apple-sub-123456"))
                    .thenReturn(Optional.of(user));
            when(jwtTokenProvider.createAccessToken(user)).thenReturn("access-token");
            when(jwtTokenProvider.createRefreshToken(user)).thenReturn("refresh-token");

            TokenResponse response = authService.loginWithApple(request);

            assertNotNull(response);
            assertEquals("Bearer", response.getGrantType());
            assertEquals("access-token", response.getAccessToken());
            assertEquals("refresh-token", response.getRefreshToken());
        }

        @Test
        @DisplayName("애플 로그인 실패 - 회원가입 필요")
        void login_with_apple_fail_signup_required() {
            AppleLoginRequest request = new AppleLoginRequest("apple-identity-token");

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "apple",
                    "apple-sub-123456",
                    "apple@test.com",
                    null,
                    null
            );

            when(appleTokenVerifier.verify("apple-identity-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("apple", "apple-sub-123456"))
                    .thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.loginWithApple(request)
            );

            assertEquals(ErrorCode.SOCIAL_SIGNUP_REQUIRED, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("애플 회원가입 테스트")
    class AppleSignupTest {

        @Test
        @DisplayName("애플 회원가입 성공")
        void signup_with_apple_success() {
            AppleSignupRequest request = new AppleSignupRequest(
                    "apple-identity-token",
                    "애플닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "apple",
                    "apple-sub-123456",
                    "apple@test.com",
                    null,
                    null
            );

            User savedUser = User.createSocialUser(
                    "apple@test.com",
                    "apple",
                    "apple-sub-123456",
                    "애플닉네임",
                    null
            );

            when(appleTokenVerifier.verify("apple-identity-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("apple", "apple-sub-123456"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDeletedAtIsNull("apple@test.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByNicknameAndDeletedAtIsNull("애플닉네임"))
                    .thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtTokenProvider.createAccessToken(savedUser)).thenReturn("access-token");
            when(jwtTokenProvider.createRefreshToken(savedUser)).thenReturn("refresh-token");

            TokenResponse response = authService.signupWithApple(request);

            assertNotNull(response);
            assertEquals("Bearer", response.getGrantType());
            assertEquals("access-token", response.getAccessToken());
            assertEquals("refresh-token", response.getRefreshToken());
        }

        @Test
        @DisplayName("애플 회원가입 실패 - 이미 애플 회원 존재")
        void signup_with_apple_fail_duplicate_social_user() {
            AppleSignupRequest request = new AppleSignupRequest(
                    "apple-identity-token",
                    "애플닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "apple",
                    "apple-sub-123456",
                    "apple@test.com",
                    null,
                    null
            );

            User existingAppleUser = User.createSocialUser(
                    "apple@test.com",
                    "apple",
                    "apple-sub-123456",
                    "기존애플회원",
                    null
            );

            when(appleTokenVerifier.verify("apple-identity-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("apple", "apple-sub-123456"))
                    .thenReturn(Optional.of(existingAppleUser));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signupWithApple(request)
            );

            assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.getErrorCode());
        }

        @Test
        @DisplayName("애플 회원가입 실패 - 같은 이메일의 일반 회원 존재")
        void signup_with_apple_fail_invalid_login_provider() {
            AppleSignupRequest request = new AppleSignupRequest(
                    "apple-identity-token",
                    "애플닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "apple",
                    "apple-sub-123456",
                    "apple@test.com",
                    null,
                    null
            );

            User emailUser = User.createEmailUser(
                    "apple@test.com",
                    "encoded-password",
                    "일반회원",
                    null
            );

            when(appleTokenVerifier.verify("apple-identity-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("apple", "apple-sub-123456"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDeletedAtIsNull("apple@test.com"))
                    .thenReturn(Optional.of(emailUser));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signupWithApple(request)
            );

            assertEquals(ErrorCode.INVALID_LOGIN_PROVIDER, exception.getErrorCode());
        }

        @Test
        @DisplayName("애플 회원가입 실패 - 닉네임 중복")
        void signup_with_apple_fail_duplicate_nickname() {
            AppleSignupRequest request = new AppleSignupRequest(
                    "apple-identity-token",
                    "중복닉네임"
            );

            SocialUserInfo socialUserInfo = new SocialUserInfo(
                    "apple",
                    "apple-sub-123456",
                    "apple@test.com",
                    null,
                    null
            );

            when(appleTokenVerifier.verify("apple-identity-token")).thenReturn(socialUserInfo);
            when(userRepository.findByProviderAndProviderIdAndDeletedAtIsNull("apple", "apple-sub-123456"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDeletedAtIsNull("apple@test.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByNicknameAndDeletedAtIsNull("중복닉네임"))
                    .thenReturn(true);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.signupWithApple(request)
            );

            assertEquals(ErrorCode.DUPLICATE_NICKNAME, exception.getErrorCode());
        }
    }
}