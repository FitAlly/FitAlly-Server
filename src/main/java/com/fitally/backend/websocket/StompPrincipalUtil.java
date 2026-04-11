package com.fitally.backend.websocket;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.security.principal.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class StompPrincipalUtil {

    public Long getCurrentUserId(Principal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.INVALID_AUTHENTICATION_PRINCIPAL);
        }

        if (!(principal instanceof Authentication authentication)) {
            throw new BusinessException(ErrorCode.INVALID_AUTHENTICATION_PRINCIPAL);
        }

        Object principalObject = authentication.getPrincipal();

        if (principalObject instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUserId();
        }

        throw new BusinessException(ErrorCode.INVALID_AUTHENTICATION_PRINCIPAL);
    }
}
