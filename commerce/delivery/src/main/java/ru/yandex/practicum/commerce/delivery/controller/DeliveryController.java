package ru.yandex.practicum.commerce.delivery.controller;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.delivery.DeliveryApi;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.dto.order.OrderDto;

@RestController
public class DeliveryController implements DeliveryApi {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Override
    public ResponseEntity<DeliveryDto> planDelivery(DeliveryDto deliveryDto) {
        return ResponseEntity.ok(deliveryService.planDelivery(deliveryDto));
    }

    @Override
    public void deliverySuccessful(UUID deliveryId) {
        deliveryService.deliverySuccessful(deliveryId);
    }

    @Override
    public void deliveryPicked(UUID deliveryId) {
        deliveryService.deliveryPicked(deliveryId);
    }

    @Override
    public void deliveryFailed(UUID deliveryId) {
        deliveryService.deliveryFailed(deliveryId);
    }

    @Override
    public BigDecimal deliveryCost(OrderDto orderDto) {
        return deliveryService.deliveryCost(orderDto);
    }
}
