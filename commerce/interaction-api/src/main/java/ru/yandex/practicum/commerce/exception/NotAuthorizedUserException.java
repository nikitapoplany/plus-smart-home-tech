package ru.yandex.practicum.commerce.exception;

import org.springframework.http.HttpStatus;

public class NotAuthorizedUserException extends ApiException {

    public NotAuthorizedUserException() {
        super(HttpStatus.UNAUTHORIZED, "Имя пользователя не должно быть пустым");
    }
}
