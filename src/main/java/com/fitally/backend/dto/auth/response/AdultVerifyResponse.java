package com.fitally.backend.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AdultVerifyResponse {

    private Long userId;
    private boolean adultVerified;
    private String impUid;
    private String merchantUid;
    private String birthdate;
}
