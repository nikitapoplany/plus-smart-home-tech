package ru.yandex.practicum.commerce.payment.service;

import java.math.BigDecimal;
import java.util.UUID;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.payment.PaymentDto;

public interface PaymentService {

    PaymentDto payment(OrderDto orderDto);

    BigDecimal getTotalCost(OrderDto orderDto);

    void paymentSuccess(UUID paymentId);

    BigDecimal productCost(OrderDto orderDto);

    void paymentFailed(UUID paymentId);
}
