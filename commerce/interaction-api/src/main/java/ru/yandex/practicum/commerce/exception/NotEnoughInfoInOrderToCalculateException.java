package ru.yandex.practicum.commerce.exception;

import org.springframework.http.HttpStatus;

public class NotEnoughInfoInOrderToCalculateException extends ApiException {

    public NotEnoughInfoInOrderToCalculateException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
