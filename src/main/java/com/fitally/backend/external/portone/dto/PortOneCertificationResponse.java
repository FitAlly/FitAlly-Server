package com.fitally.backend.external.portone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortOneCertificationResponse {

    private int code;
    private String message;
    private CertificationData response;

    @Getter
    @Setter
    public static class CertificationData {

        @JsonProperty("imp_uid")
        private String impUid;

        @JsonProperty("merchant_uid")
        private String merchantUid;

        private String name;
        private String birthday;
        private String gender;
        private String phone;
        private String carrier;

        @JsonProperty("foreign")
        private Boolean foreigner;

    }
}