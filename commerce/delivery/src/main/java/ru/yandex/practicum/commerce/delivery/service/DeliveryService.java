package ru.yandex.practicum.commerce.delivery.service;

import java.math.BigDecimal;
import java.util.UUID;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.dto.order.OrderDto;

public interface DeliveryService {

    DeliveryDto planDelivery(DeliveryDto deliveryDto);

    void deliverySuccessful(UUID deliveryId);

    void deliveryPicked(UUID deliveryId);

    void deliveryFailed(UUID deliveryId);

    BigDecimal deliveryCost(OrderDto orderDto);
}
