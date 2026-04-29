package ru.yandex.practicum.commerce.dto.store;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SetProductQuantityStateRequest(
        @NotNull UUID productId,
        @NotNull QuantityState quantityState
) {
}
