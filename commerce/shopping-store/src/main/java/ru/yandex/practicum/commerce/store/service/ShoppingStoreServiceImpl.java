package ru.yandex.practicum.commerce.store.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.dto.store.ProductDto;
import ru.yandex.practicum.commerce.dto.store.ProductState;
import ru.yandex.practicum.commerce.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.store.mapper.ProductMapper;
import ru.yandex.practicum.commerce.store.model.ProductEntity;
import ru.yandex.practicum.commerce.store.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ShoppingStoreServiceImpl implements ShoppingStoreService {

    private final ProductRepository productRepository;

    public ShoppingStoreServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Integer page, Integer size, String[] sort) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        return productRepository.findAllByProductCategoryAndProductState(category, ProductState.ACTIVE, pageable)
                .map(ProductMapper::toDto);
    }

    @Override
    @Transactional
    public ProductDto createNewProduct(ProductDto productDto) {
        ProductEntity entity = ProductMapper.toNewEntity(productDto);
        return ProductMapper.toDto(productRepository.save(entity));
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        UUID productId = productDto.productId();
        if (productId == null) {
            throw new IllegalArgumentException("Для обновления товара требуется productId");
        }
        ProductEntity entity = getProductEntity(productId);
        ProductMapper.updateEntity(entity, productDto);
        return ProductMapper.toDto(productRepository.save(entity));
    }

    @Override
    @Transactional
    public Boolean removeProductFromStore(UUID productId) {
        ProductEntity entity = getProductEntity(productId);
        entity.setProductState(ProductState.DEACTIVATE);
        productRepository.save(entity);
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public Boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        ProductEntity entity = getProductEntity(request.productId());
        entity.setQuantityState(request.quantityState());
        productRepository.save(entity);
        return Boolean.TRUE;
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return ProductMapper.toDto(getProductEntity(productId));
    }

    private ProductEntity getProductEntity(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Sort buildSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String rawSort : sort) {
            if (rawSort == null || rawSort.isBlank()) {
                continue;
            }
            String[] parts = rawSort.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = parts.length > 1
                    ? Sort.Direction.fromString(parts[1].trim())
                    : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, property));
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }
}
