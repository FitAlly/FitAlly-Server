package com.fitally.backend.common.response;

import com.fitally.backend.common.exception.ErrorCode;

public class ErrorResponse {

    private final boolean success;
    private final String code;
    private final String message;

    private ErrorResponse(String code, String message) {
        this.success = false;
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return new ErrorResponse(errorCode.getCode(), customMessage);
    }

    public boolean isSuccess() { return success; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
