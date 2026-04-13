package com.fitally.backend.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 공통
    INTERNAL_SEVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_002", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_003", "허용되지 않은 HTTP 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_004", "요청한 리소스를 찾을 수 없습니다."),

    // 회원
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_003", "이미 사용 중인 닉네임 입니다."),

    // 인증/인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "로그인이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "만료된 토큰입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_005", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_LOGIN_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_006", "일반 로그인 계정이 아닙니다."),
    SOCIAL_SIGNUP_REQUIRED(HttpStatus.NOT_FOUND, "AUTH_007", "소셜 회원가입이 필요합니다."),
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_008", "유효하지 않은 소셜 토큰입니다."),


    // 성인 인증
    ADULT_VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH_009", "성인 인증이 필요합니다."),
    ADULT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "AUTH_010", "성인 인증에 실패했습니다."),
    ADULT_VERIFICATION_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_011", "성인 인증 정보가 만료되었습니다."),
    UNDERAGE_USER(HttpStatus.FORBIDDEN, "AUTH_012", "성인만 가입할 수 있습니다."),
    INVALID_VERIFICATION_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_013", "유효하지 않은 성인 인증 토큰입니다."),
    ALREADY_ADULT_VERIFIED(HttpStatus.CONFLICT, "AUTH_014", "이미 성인 인증이 완료된 사용자입니다."),

    // 채팅(웹소켓)
    AUTHENTICATION_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_015", "인증 정보가 없습니다."),
    INVALID_AUTHENTICATION_PRINCIPAL(HttpStatus.UNAUTHORIZED, "AUTH_016", "인증 사용자 정보를 확인할 수 없습니다."),
    WEBSOCKET_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_017", "웹소켓 인증이 필요합니다."),

    // 프로필
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKOUT_001", "프로필 정보를 찾을 수 없습니다."),

    // 채팅
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅방을 찾을 수 없습니다."),
    CHAT_PARTICIPANT_NOT_FOUND(HttpStatus.FORBIDDEN, "CHAT_002", "해당 채팅방 참여자가 아닙니다."),
    CHAT_MESSAGE_EMPTY(HttpStatus.BAD_REQUEST, "CHAT_003", "메시지 내용이 없습니다."),
    CHAT_SELF_ROOM_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CHAT_004", "자기 자신과는 채팅할 수 없습니다."),
    CHAT_OPPONENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_005", "상대 사용자를 찾을 수 없습니다."),
    CHAT_SENDER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_006", "발신자를 찾을 수 없습니다."),
    CHAT_USER_NOT_FOUND(HttpStatus.NOT_FOUND,"CHAT_007", "사용자를 찾을 수 없습니다."),
    CHAT_IMAGE_EMPTY(HttpStatus.BAD_REQUEST,"CHAT_008", "채팅 이미지 파일이 비어 있습니다."),
    CHAT_IMAGE_INVALID(HttpStatus.BAD_REQUEST,"CHAT_009","유효하지 않은 이미지 파일입니다."),
    CHAT_IMAGE_EXTENSION_NOT_ALLOWED(HttpStatus.BAD_REQUEST,"CHAT_010","허용되지 않은 이미지 확장자입니다."),
    CHAT_IMAGE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE,"CHAT_011","이미지 파일 크기가 너무 큽니다."),
    CHAT_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"CHAT_012","채팅 이미지 업로드에 실패했습니다."),

    // 신고
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_001", "신고 내역을 찾을 수 없습니다."),

    // 차단
    BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOCK_001", "차단 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
