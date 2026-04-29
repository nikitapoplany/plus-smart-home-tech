package ru.yandex.practicum.commerce.cart.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.cart.model.ShoppingCartEntity;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCartEntity, java.util.UUID> {

    Optional<ShoppingCartEntity> findFirstByUsernameOrderByCreatedAtDesc(String username);

    Optional<ShoppingCartEntity> findFirstByUsernameAndActiveTrueOrderByCreatedAtDesc(String username);
}
