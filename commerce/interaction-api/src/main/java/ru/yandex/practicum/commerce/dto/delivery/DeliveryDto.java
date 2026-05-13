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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID deliveryId;
        private AddressDto fromAddress;
        private AddressDto toAddress;
        private UUID orderId;
        private DeliveryState deliveryState;

        private Builder() {
        }

        public Builder deliveryId(UUID deliveryId) {
            this.deliveryId = deliveryId;
            return this;
        }

        public Builder fromAddress(AddressDto fromAddress) {
            this.fromAddress = fromAddress;
            return this;
        }

        public Builder toAddress(AddressDto toAddress) {
            this.toAddress = toAddress;
            return this;
        }

        public Builder orderId(UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder deliveryState(DeliveryState deliveryState) {
            this.deliveryState = deliveryState;
            return this;
        }

        public DeliveryDto build() {
            return new DeliveryDto(deliveryId, fromAddress, toAddress, orderId, deliveryState);
        }
    }
}
