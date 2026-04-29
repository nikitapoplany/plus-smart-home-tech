package ru.yandex.practicum.commerce.api.delivery;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.dto.order.OrderDto;

public interface DeliveryApi {

    @PutMapping("/api/v1/delivery")
    DeliveryDto planDelivery(@RequestBody @Valid DeliveryDto deliveryDto);

    @PostMapping("/api/v1/delivery/successful")
    void deliverySuccessful(@RequestBody UUID deliveryId);

    @PostMapping("/api/v1/delivery/picked")
    void deliveryPicked(@RequestBody UUID deliveryId);

    @PostMapping("/api/v1/delivery/failed")
    void deliveryFailed(@RequestBody UUID deliveryId);

    @PostMapping("/api/v1/delivery/cost")
    BigDecimal deliveryCost(@RequestBody @Valid OrderDto orderDto);
}
