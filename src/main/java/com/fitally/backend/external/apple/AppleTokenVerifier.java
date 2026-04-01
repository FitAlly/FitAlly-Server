package com.fitally.backend.external.apple;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.dto.auth.response.SocialUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

@Component
public class AppleTokenVerifier {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_JWK_SET_URI = "https://appleid.apple.com/auth/keys";

    private final JwtDecoder jwtDecoder;

    public AppleTokenVerifier(@Value("${social.apple.audience}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(APPLE_JWK_SET_URI).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(APPLE_ISSUER);
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        decoder.setJwtValidator(validator);
        this.jwtDecoder = decoder;
    }

    public SocialUserInfo verify(String identityToken) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(identityToken);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String providerId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");

        if (providerId == null || providerId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return new SocialUserInfo(
                "apple",
                providerId,
                email,
                null,
                null
        );
    }

    private static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final String audience;

        public AudienceValidator(String audience) {
            this.audience = audience;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token) {
            if (token.getAudience() != null && token.getAudience().contains(audience)){
                return OAuth2TokenValidatorResult.success();
        }

        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "audience 값이 올바르지 않습니다.",
                null
        );
        return OAuth2TokenValidatorResult.failure(error);
    }

}
}
