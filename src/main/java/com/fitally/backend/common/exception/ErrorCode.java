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

    // 프로필
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKOUT_001", "운동 정보를 찾을 수 없습니다."),

    // 채팅
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001",  "채팅방을 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_002", "채팅 메세지를 찾을 수 없습니다."),

    // 신고
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_001", "신고 내역을 찾을 수 없습니다."),

    // 차단
    BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOCK_001", "차단 정보를 찾을 수 없습니다."),

    // 운동(Exercise)
    EXERCISE_NOT_FOUND(HttpStatus.NOT_FOUND, "EXERCISE_001", "운동 정보를 찾을 수 없습니다."),

    // 루틴(Routine)
    ROUTINE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTINE_001", "루틴을 찾을 수 없습니다."),
    AI_ROUTINE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTINE_002", "AI 추천 루틴을 찾을 수 없습니다."),

    // 루틴 세션(RoutineSession)
    ROUTINE_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTINE_SESSION_001", "루틴 세션을 찾을 수 없습니다."),
    ROUTINE_SESSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "ROUTINE_SESSION_002", "이미 완료된 루틴 세션입니다."),
    ROUTINE_SESSION_EXERCISE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTINE_SESSION_003", "세션에 해당 운동이 존재하지 않습니다."),

    // 단일 운동 세션(WorkoutSession)
    WORKOUT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKOUT_SESSION_001", "운동 세션을 찾을 수 없습니다."),
    WORKOUT_SESSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "WORKOUT_SESSION_002", "이미 완료된 운동 세션입니다.");

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
