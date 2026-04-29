package ru.yandex.practicum.commerce.cart.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.cart.ShoppingCartApi;
import ru.yandex.practicum.commerce.cart.service.ShoppingCartService;
import ru.yandex.practicum.commerce.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;

@RestController
public class ShoppingCartController implements ShoppingCartApi {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        return shoppingCartService.getShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        shoppingCartService.deactivateCurrentShoppingCart(username);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products) {
        return shoppingCartService.removeFromShoppingCart(username, products);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        return shoppingCartService.changeProductQuantity(username, request);
    }
}
