package ru.yandex.practicum.commerce.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

    @JsonIgnore
    private final HttpStatus status;
    private final String userMessage;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.userMessage = message;
    }

    public String getHttpStatus() {
        return status.toString();
    }

    public String getUserMessage() {
        return userMessage;
    }

    @JsonIgnore
    public HttpStatus status() {
        return status;
    }
}
