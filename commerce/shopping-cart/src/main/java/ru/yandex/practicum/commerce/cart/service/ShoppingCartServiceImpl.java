package ru.yandex.practicum.commerce.cart.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.cart.client.WarehouseClient;
import ru.yandex.practicum.commerce.cart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.commerce.cart.model.ShoppingCartEntity;
import ru.yandex.practicum.commerce.cart.repository.ShoppingCartRepository;
import ru.yandex.practicum.commerce.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.commerce.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.exception.WarehouseUnavailableException;

@Service
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final WarehouseClient warehouseClient;

    public ShoppingCartServiceImpl(ShoppingCartRepository shoppingCartRepository,
                                   WarehouseClient warehouseClient) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.warehouseClient = warehouseClient;
    }

    @Override
    @Transactional
    public ShoppingCartDto getShoppingCart(String username) {
        String normalizedUsername = validateUsername(username);
        ShoppingCartEntity cart = shoppingCartRepository.findFirstByUsernameOrderByCreatedAtDesc(normalizedUsername)
                .orElseGet(() -> createCart(normalizedUsername));
        return ShoppingCartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        String normalizedUsername = validateUsername(username);
        validateIncomingProducts(products);
        ShoppingCartEntity cart = shoppingCartRepository.findFirstByUsernameAndActiveTrueOrderByCreatedAtDesc(normalizedUsername)
                .orElseGet(() -> createCart(normalizedUsername));

        Map<UUID, Long> candidateProducts = new LinkedHashMap<>(cart.getProducts());
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            candidateProducts.merge(entry.getKey(), entry.getValue(), Long::sum);
        }

        ensureWarehouseHasProducts(cart.getShoppingCartId(), candidateProducts);
        cart.setProducts(candidateProducts);
        return ShoppingCartMapper.toDto(shoppingCartRepository.save(cart));
    }

    @Override
    @Transactional
    public void deactivateCurrentShoppingCart(String username) {
        String normalizedUsername = validateUsername(username);
        shoppingCartRepository.findFirstByUsernameAndActiveTrueOrderByCreatedAtDesc(normalizedUsername)
                .ifPresent(cart -> {
                    cart.setActive(false);
                    shoppingCartRepository.save(cart);
                });
    }

    @Override
    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products) {
        String normalizedUsername = validateUsername(username);
        ShoppingCartEntity cart = getActiveCart(normalizedUsername);
        boolean removed = false;
        for (UUID productId : products) {
            removed = cart.getProducts().remove(productId) != null || removed;
        }
        if (!removed) {
            throw new NoProductsInShoppingCartException("В корзине нет указанных товаров");
        }
        return ShoppingCartMapper.toDto(shoppingCartRepository.save(cart));
    }

    @Override
    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        String normalizedUsername = validateUsername(username);
        ShoppingCartEntity cart = getActiveCart(normalizedUsername);
        if (!cart.getProducts().containsKey(request.productId())) {
            throw new NoProductsInShoppingCartException("Товар отсутствует в корзине");
        }

        Map<UUID, Long> candidateProducts = new LinkedHashMap<>(cart.getProducts());
        candidateProducts.put(request.productId(), request.newQuantity());

        ensureWarehouseHasProducts(cart.getShoppingCartId(), candidateProducts);
        cart.setProducts(candidateProducts);
        return ShoppingCartMapper.toDto(shoppingCartRepository.save(cart));
    }

    private ShoppingCartEntity getActiveCart(String username) {
        return shoppingCartRepository.findFirstByUsernameAndActiveTrueOrderByCreatedAtDesc(username)
                .orElseThrow(() -> new NoProductsInShoppingCartException("Активная корзина пользователя не найдена"));
    }

    private ShoppingCartEntity createCart(String username) {
        ShoppingCartEntity cart = new ShoppingCartEntity();
        cart.setUsername(username);
        cart.setActive(true);
        return shoppingCartRepository.save(cart);
    }

    private String validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }
        return username.trim();
    }

    private void validateIncomingProducts(Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Список товаров для корзины не должен быть пустым");
        }
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException("Идентификатор товара не должен быть пустым");
            }
            if (entry.getValue() == null || entry.getValue() < 1) {
                throw new IllegalArgumentException("Количество товара должно быть больше нуля");
            }
        }
    }

    private void ensureWarehouseHasProducts(UUID shoppingCartId, Map<UUID, Long> products) {
        warehouseClient.checkProductQuantityEnoughForShoppingCart(new ShoppingCartDto(shoppingCartId, products));
    }
}
