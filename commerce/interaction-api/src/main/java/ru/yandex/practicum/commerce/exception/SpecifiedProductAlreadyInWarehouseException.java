package ru.yandex.practicum.commerce.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class SpecifiedProductAlreadyInWarehouseException extends ApiException {

    public SpecifiedProductAlreadyInWarehouseException(UUID productId) {
        super(HttpStatus.BAD_REQUEST, "Товар " + productId + " уже зарегистрирован на складе");
    }
}
