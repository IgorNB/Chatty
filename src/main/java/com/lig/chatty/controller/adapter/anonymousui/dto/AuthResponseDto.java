package com.lig.chatty.controller.adapter.anonymousui.dto;

import lombok.Getter;
import lombok.experimental.FieldNameConstants;

@Getter
@FieldNameConstants
public class AuthResponseDto {
    private final String accessToken;
    private final String tokenType;

    public AuthResponseDto(String accessToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
    }
}
