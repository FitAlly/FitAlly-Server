package com.fitally.backend.common.response;

public class ErrorResponse {

    private final boolean success;
    private final String code;
    private final String message;

    public ErrorResponse(String code, String message) {
        this.success = false;
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
