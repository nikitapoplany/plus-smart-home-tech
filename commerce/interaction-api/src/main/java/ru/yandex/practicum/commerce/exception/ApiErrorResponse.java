package ru.yandex.practicum.commerce.exception;

public record ApiErrorResponse(
        String httpStatus,
        String userMessage,
        String message
) {
    public static ApiErrorResponse from(ApiException exception) {
        return new ApiErrorResponse(
                exception.getHttpStatus(),
                exception.getUserMessage(),
                exception.getMessage()
        );
    }
}
