package ru.yandex.practicum.commerce.exception;

import org.springframework.http.HttpStatus;

public class WarehouseUnavailableException extends ApiException {

    public WarehouseUnavailableException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "Склад временно недоступен. Повторите попытку позже.");
    }
}
