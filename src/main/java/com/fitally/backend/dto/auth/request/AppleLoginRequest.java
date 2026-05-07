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
public class AppleLoginRequest {

    @NotBlank(message = "애플 identity token은 필수입니다.")
    private String identityToken;


}
