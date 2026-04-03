package com.fitally.backend.common.exception;

import com.fitally.backend.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 로직에서 직접 발생시키는 예외 처리 / 서비스 계층에서 직접 던진 비즈니스 예외처리
    // 예: 중복 이메일, 중복 닉네임, 로그인 실패, 잘못된 로그인 방식, 권한 없음 등
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException e,
        HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();

        ErrorResponse response = ErrorResponse.of(
                errorCode.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    // 지원하지 않는 HTTP 메서드로 요청했을 때 처리
    // 예: POST만 가능한 API에 GET 요청을 보낸 경우
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                ErrorCode.METHOD_NOT_ALLOWED.getMessage()
        );

        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
                .body(response);
    }

    // 요청값이 잘못되었을 때 처리하는 공통 예외
    // 예:
    // - @Valid 검증 실패
    // - 필수값 누락
    // - 잘못된 형식의 JSON 요청
    // - 파라미터 타입 불일치
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidInputException(
            Exception e,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                ErrorCode.INVALID_INPUT_VALUE.getMessage()
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(response);
    }

    // 위에서 처리하지 못한 모든 예외를 처리하는 최종 예외 핸들러
    // 예: 예상하지 못한 서버 내부 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INTERNAL_SEVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SEVER_ERROR.getMessage()
        );

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SEVER_ERROR.getStatus())
                .body(response);
    }
}
