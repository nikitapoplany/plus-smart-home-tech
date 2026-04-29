package ru.yandex.practicum.commerce.api.store;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.dto.store.ProductDto;
import ru.yandex.practicum.commerce.dto.store.SetProductQuantityStateRequest;

public interface ShoppingStoreApi {

    @GetMapping("/api/v1/shopping-store")
    Page<ProductDto> getProducts(@RequestParam ProductCategory category,
                                 @RequestParam Integer page,
                                 @RequestParam Integer size,
                                 @RequestParam String[] sort);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createNewProduct(@RequestBody @Valid ProductDto productDto);

    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@RequestBody @Valid ProductDto productDto);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    Boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    Boolean setProductQuantityState(@RequestBody @Valid SetProductQuantityStateRequest request);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);
}
