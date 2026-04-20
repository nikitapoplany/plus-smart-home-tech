package ru.yandex.practicum.commerce.dto.warehouse;

public record BookedProductsDto(
        Double deliveryWeight,
        Double deliveryVolume,
        Boolean fragile
) {
}
