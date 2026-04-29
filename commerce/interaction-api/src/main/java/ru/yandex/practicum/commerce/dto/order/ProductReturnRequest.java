package ru.yandex.practicum.commerce.dto.order;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record ProductReturnRequest(
        @NotNull UUID orderId,
        @NotNull Map<UUID, Long> products
) {
}
