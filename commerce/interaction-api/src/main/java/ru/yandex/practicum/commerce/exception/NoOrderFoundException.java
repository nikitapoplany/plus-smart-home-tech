package ru.yandex.practicum.commerce.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class NoOrderFoundException extends ApiException {

    public NoOrderFoundException(UUID orderId) {
        super(HttpStatus.NOT_FOUND, "Заказ с идентификатором " + orderId + " не найден");
    }
}
