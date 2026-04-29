package ru.yandex.practicum.commerce.warehouse.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.warehouse.model.OrderBookingEntity;

public interface OrderBookingRepository extends JpaRepository<OrderBookingEntity, UUID> {
}
