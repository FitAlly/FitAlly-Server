package com.fitally.backend.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KakaoSignupRequest {

    @NotBlank(message = "카카오 액세스 토큰은 필수입니다.")
    private String socialAccessToken;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;


}
