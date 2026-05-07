package com.fitally.backend.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AdultVerifyRequest {

    @NotBlank(message = "본인인증 결과값 impUid는 필수입니다.")
    private String impUid;

    @NotBlank(message = "merchantUid는 필수입니다.")
    private String merchantUid;
}
