package ru.yandex.practicum.commerce.dto.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;

public record DeliveryDto(
        UUID deliveryId,
        @NotNull @Valid AddressDto fromAddress,
        @NotNull @Valid AddressDto toAddress,
        @NotNull UUID orderId,
        @NotNull DeliveryState deliveryState
) {
}
