package com.fitally.backend.external.portone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PortOneTokenRequest {

    private String imp_key;
    private String imp_secret;

}