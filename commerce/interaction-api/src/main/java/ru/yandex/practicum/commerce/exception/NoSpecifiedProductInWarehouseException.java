package ru.yandex.practicum.commerce.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class NoSpecifiedProductInWarehouseException extends ApiException {

    public NoSpecifiedProductInWarehouseException(UUID productId) {
        super(HttpStatus.BAD_REQUEST, "На складе нет сведений о товаре " + productId);
    }
}
