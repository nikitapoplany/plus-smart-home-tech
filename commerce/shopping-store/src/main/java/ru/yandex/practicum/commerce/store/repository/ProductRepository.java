package ru.yandex.practicum.commerce.store.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.dto.store.ProductState;
import ru.yandex.practicum.commerce.store.model.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    Page<ProductEntity> findAllByProductCategoryAndProductState(
            ProductCategory productCategory,
            ProductState productState,
            Pageable pageable
    );
}
