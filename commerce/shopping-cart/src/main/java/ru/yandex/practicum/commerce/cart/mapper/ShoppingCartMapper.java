package ru.yandex.practicum.commerce.cart.mapper;

import java.util.LinkedHashMap;
import ru.yandex.practicum.commerce.cart.model.ShoppingCartEntity;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;

public final class ShoppingCartMapper {

    private ShoppingCartMapper() {
    }

    public static ShoppingCartDto toDto(ShoppingCartEntity entity) {
        return new ShoppingCartDto(
                entity.getShoppingCartId(),
                new LinkedHashMap<>(entity.getProducts())
        );
    }
}
