package ru.yandex.practicum.commerce.store.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import ru.yandex.practicum.commerce.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.dto.store.ProductDto;
import ru.yandex.practicum.commerce.dto.store.SetProductQuantityStateRequest;

public interface ShoppingStoreService {

    Page<ProductDto> getProducts(ProductCategory category, Integer page, Integer size, String[] sort);

    ProductDto createNewProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    Boolean removeProductFromStore(UUID productId);

    Boolean setProductQuantityState(SetProductQuantityStateRequest request);

    ProductDto getProduct(UUID productId);
}
