package ru.yandex.practicum.commerce.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.api.order.OrderApi;

@FeignClient(name = "order")
public interface OrderClient extends OrderApi {
}
