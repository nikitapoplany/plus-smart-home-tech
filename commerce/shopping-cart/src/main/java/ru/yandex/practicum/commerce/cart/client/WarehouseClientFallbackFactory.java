package ru.yandex.practicum.commerce.cart.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.exception.WarehouseUnavailableException;

@Component
public class WarehouseClientFallbackFactory implements FallbackFactory<WarehouseClient> {

    private final ObjectMapper objectMapper;

    public WarehouseClientFallbackFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public WarehouseClient create(Throwable cause) {
        return new WarehouseClient() {
            @Override
            public void newProductInWarehouse(NewProductInWarehouseRequest request) {
                throw mapToDomainException(cause);
            }

            @Override
            public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
                throw mapToDomainException(cause);
            }

            @Override
            public void addProductToWarehouse(AddProductToWarehouseRequest request) {
                throw mapToDomainException(cause);
            }

            @Override
            public void shippedToDelivery(ShippedToDeliveryRequest request) {
                throw mapToDomainException(cause);
            }

            @Override
            public void acceptReturn(Map<UUID, Long> products) {
                throw mapToDomainException(cause);
            }

            @Override
            public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
                throw mapToDomainException(cause);
            }

            @Override
            public AddressDto getWarehouseAddress() {
                throw mapToDomainException(cause);
            }
        };
    }

    private RuntimeException mapToDomainException(Throwable cause) {
        if (cause instanceof ProductInShoppingCartLowQuantityInWarehouse businessException) {
            return businessException;
        }
        if (cause instanceof WarehouseUnavailableException unavailableException) {
            return unavailableException;
        }
        if (cause instanceof FeignException.BadRequest badRequest) {
            return new ProductInShoppingCartLowQuantityInWarehouse(extractWarehouseMessage(badRequest));
        }
        return new WarehouseUnavailableException();
    }

    private String extractWarehouseMessage(FeignException.BadRequest exception) {
        String content = exception.contentUTF8();
        if (content == null || content.isBlank()) {
            return "Недостаточно товара на складе";
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode userMessage = root.get("userMessage");
            if (userMessage != null && !userMessage.isNull() && !userMessage.asText().isBlank()) {
                return userMessage.asText();
            }
        } catch (IOException ignored) {
            return "Недостаточно товара на складе";
        }
        return "Недостаточно товара на складе";
    }
}
