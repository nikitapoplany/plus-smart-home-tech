package ru.yandex.practicum.commerce.store.controller;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.store.ShoppingStoreApi;
import ru.yandex.practicum.commerce.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.dto.store.ProductDto;
import ru.yandex.practicum.commerce.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.store.service.ShoppingStoreService;

@RestController
public class ShoppingStoreController implements ShoppingStoreApi {

    private final ShoppingStoreService shoppingStoreService;

    public ShoppingStoreController(ShoppingStoreService shoppingStoreService) {
        this.shoppingStoreService = shoppingStoreService;
    }

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Integer page, Integer size, String[] sort) {
        return shoppingStoreService.getProducts(category, page, size, sort);
    }

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        return shoppingStoreService.createNewProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        return shoppingStoreService.updateProduct(productDto);
    }

    @Override
    public Boolean removeProductFromStore(UUID productId) {
        return shoppingStoreService.removeProductFromStore(productId);
    }

    @Override
    public Boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        return shoppingStoreService.setProductQuantityState(request);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return shoppingStoreService.getProduct(productId);
    }
}
