package ru.yandex.practicum.commerce.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.api.warehouse.WarehouseApi;

@FeignClient(name = "warehouse", fallbackFactory = WarehouseClientFallbackFactory.class)
public interface WarehouseClient extends WarehouseApi {
}
