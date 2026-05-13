package ru.yandex.practicum.commerce.exception;

public class ProductInShoppingCartNotInWarehouse extends ApiException {

    public ProductInShoppingCartNotInWarehouse(String message) {
        super(org.springframework.http.HttpStatus.BAD_REQUEST, message);
    }
}
