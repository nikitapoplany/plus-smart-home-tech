package ru.yandex.practicum.commerce.store.mapper;

import ru.yandex.practicum.commerce.dto.store.ProductDto;
import ru.yandex.practicum.commerce.store.model.ProductEntity;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductDto toDto(ProductEntity entity) {
        return new ProductDto(
                entity.getProductId(),
                entity.getProductName(),
                entity.getDescription(),
                entity.getImageSrc(),
                entity.getQuantityState(),
                entity.getProductState(),
                entity.getProductCategory(),
                entity.getPrice()
        );
    }

    public static ProductEntity toNewEntity(ProductDto dto) {
        ProductEntity entity = new ProductEntity();
        entity.setProductName(dto.productName());
        entity.setDescription(dto.description());
        entity.setImageSrc(dto.imageSrc());
        entity.setQuantityState(dto.quantityState());
        entity.setProductState(dto.productState());
        entity.setProductCategory(dto.productCategory());
        entity.setPrice(dto.price());
        return entity;
    }

    public static void updateEntity(ProductEntity entity, ProductDto dto) {
        entity.setProductName(dto.productName());
        entity.setDescription(dto.description());
        entity.setImageSrc(dto.imageSrc());
        entity.setQuantityState(dto.quantityState());
        entity.setProductState(dto.productState());
        entity.setProductCategory(dto.productCategory());
        entity.setPrice(dto.price());
    }
}
