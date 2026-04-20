package ru.yandex.practicum.commerce.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiException> handleApiException(ApiException exception) {
        return ResponseEntity.status(exception.status()).body(exception);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiException> handleBadRequest(Exception exception) {
        ApiException apiException = new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getMessage()) {
        };
        return ResponseEntity.badRequest().body(apiException);
    }
}
