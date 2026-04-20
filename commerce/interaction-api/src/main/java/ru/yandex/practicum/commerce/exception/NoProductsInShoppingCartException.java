package ru.yandex.practicum.commerce.exception;

import org.springframework.http.HttpStatus;

public class NoProductsInShoppingCartException extends ApiException {

    public NoProductsInShoppingCartException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
