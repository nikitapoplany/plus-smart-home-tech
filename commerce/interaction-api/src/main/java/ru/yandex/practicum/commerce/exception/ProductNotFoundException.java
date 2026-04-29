package ru.yandex.practicum.commerce.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends ApiException {

    public ProductNotFoundException(UUID productId) {
        super(HttpStatus.NOT_FOUND, "Товар с идентификатором " + productId + " не найден");
    }
}
