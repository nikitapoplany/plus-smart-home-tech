package ru.yandex.practicum.commerce.api.payment;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.payment.PaymentDto;

public interface PaymentApi {

    @PostMapping("/api/v1/payment")
    PaymentDto payment(@RequestBody @Valid OrderDto orderDto);

    @PostMapping("/api/v1/payment/totalCost")
    BigDecimal getTotalCost(@RequestBody @Valid OrderDto orderDto);

    @PostMapping("/api/v1/payment/refund")
    void paymentSuccess(@RequestBody UUID paymentId);

    @PostMapping("/api/v1/payment/productCost")
    BigDecimal productCost(@RequestBody @Valid OrderDto orderDto);

    @PostMapping("/api/v1/payment/failed")
    void paymentFailed(@RequestBody UUID paymentId);
}
