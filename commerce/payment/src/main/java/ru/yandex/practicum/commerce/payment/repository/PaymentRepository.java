package ru.yandex.practicum.commerce.payment.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
}
