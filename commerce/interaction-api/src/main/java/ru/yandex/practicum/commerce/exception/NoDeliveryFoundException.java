package ru.yandex.practicum.commerce.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class NoDeliveryFoundException extends ApiException {

    public NoDeliveryFoundException(UUID deliveryId) {
        super(HttpStatus.NOT_FOUND, "Доставка с идентификатором " + deliveryId + " не найдена");
    }
}
