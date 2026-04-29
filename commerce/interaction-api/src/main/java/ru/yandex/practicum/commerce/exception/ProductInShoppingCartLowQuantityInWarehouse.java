package ru.yandex.practicum.commerce.exception;

import org.springframework.http.HttpStatus;

public class ProductInShoppingCartLowQuantityInWarehouse extends ApiException {

    public ProductInShoppingCartLowQuantityInWarehouse(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
