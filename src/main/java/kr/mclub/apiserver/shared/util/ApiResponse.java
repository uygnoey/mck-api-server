package kr.mclub.apiserver.shared.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.mclub.apiserver.shared.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 표준 응답 형식
 * Standard API response wrapper
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private ErrorInfo error;
    private LocalDateTime timestamp;

    /**
     * 성공 응답 (데이터 포함)
     * Success response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "Success", null, LocalDateTime.now());
    }

    /**
     * 성공 응답 (데이터 + 메시지)
     * Success response with data and custom message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, LocalDateTime.now());
    }

    /**
     * 성공 응답 (메시지만)
     * Success response with message only
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, null, message, null, LocalDateTime.now());
    }

    /**
     * 에러 응답
     * Error response
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, null,
                new ErrorInfo(errorCode.getCode(), errorCode.getMessage(), null),
                LocalDateTime.now());
    }

    /**
     * 에러 응답 (상세 정보 포함)
     * Error response with detail
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String detail) {
        return new ApiResponse<>(false, null, null,
                new ErrorInfo(errorCode.getCode(), errorCode.getMessage(), detail),
                LocalDateTime.now());
    }

    /**
     * 에러 정보
     * Error information
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorInfo {
        private String code;
        private String message;
        private String detail;
    }
}
