package com.algolog.global.exception;

import java.util.List;

public record ErrorResponse(
    String code,
    String message,
    List<FieldErrorResponse> fieldErrors
) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message, List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorResponse> fieldErrors) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), fieldErrors);
    }

    public record FieldErrorResponse(
        String field,
        String message
    ) {
    }
}