package ru.yandex.practicum.commerce.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddProductToWarehouseRequest(
        UUID productId,
        @NotNull @Min(1) Long quantity
) {
}
