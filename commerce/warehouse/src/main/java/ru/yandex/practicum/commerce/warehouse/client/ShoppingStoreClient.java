package ru.yandex.practicum.commerce.warehouse.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.api.store.ShoppingStoreApi;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreClient extends ShoppingStoreApi {
}
