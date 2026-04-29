package ru.yandex.practicum.commerce.order.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.order.model.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    List<OrderEntity> findAllByOrderByCreatedAtDesc();

    List<OrderEntity> findAllByUsernameOrderByCreatedAtDesc(String username);
}
