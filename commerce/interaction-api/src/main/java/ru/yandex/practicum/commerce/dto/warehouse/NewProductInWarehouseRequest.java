package ru.yandex.practicum.commerce.dto.warehouse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record NewProductInWarehouseRequest(
        @NotNull UUID productId,
        Boolean fragile,
        @NotNull @Valid DimensionDto dimension,
        @NotNull @DecimalMin("1.0") Double weight
) {
}
