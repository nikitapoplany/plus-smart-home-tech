package ru.yandex.practicum.commerce.delivery.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.delivery.client.OrderClient;
import ru.yandex.practicum.commerce.delivery.client.WarehouseClient;
import ru.yandex.practicum.commerce.delivery.model.DeliveryEntity;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryState;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.exception.NoDeliveryFoundException;
import ru.yandex.practicum.commerce.exception.NotEnoughInfoInOrderToCalculateException;

@Service
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceImpl.class);
    private static final BigDecimal BASE_COST = new BigDecimal("5.0");
    private static final BigDecimal FRAGILE_RATE = new BigDecimal("0.2");
    private static final BigDecimal WEIGHT_RATE = new BigDecimal("0.3");
    private static final BigDecimal VOLUME_RATE = new BigDecimal("0.2");
    private static final BigDecimal SINGLE_WAREHOUSE_RATE = BigDecimal.ONE;
    private static final BigDecimal DOUBLE_WAREHOUSE_RATE = BigDecimal.valueOf(2L);
    private static final BigDecimal ZERO_VALUE = BigDecimal.ZERO;
    private static final int COST_SCALE = 2;
    private static final String ADDRESS_1 = "ADDRESS_1";
    private static final String ADDRESS_2 = "ADDRESS_2";

    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
                               OrderClient orderClient,
                               WarehouseClient warehouseClient) {
        this.deliveryRepository = deliveryRepository;
        this.orderClient = orderClient;
        this.warehouseClient = warehouseClient;
    }

    @Override
    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        log.info("Планирование доставки для заказа {}", deliveryDto.orderId());
        DeliveryEntity deliveryEntity = new DeliveryEntity();
        deliveryEntity.setFromAddress(toEmbeddable(deliveryDto.fromAddress()));
        deliveryEntity.setToAddress(toEmbeddable(deliveryDto.toAddress()));
        deliveryEntity.setOrderId(deliveryDto.orderId());
        deliveryEntity.setDeliveryState(deliveryDto.deliveryState());
        return toDto(deliveryRepository.save(deliveryEntity));
    }

    @Override
    @Transactional
    public void deliverySuccessful(UUID deliveryId) {
        DeliveryEntity deliveryEntity = getDelivery(deliveryId);
        log.info("Доставка {} успешно завершена", deliveryId);
        deliveryEntity.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(deliveryEntity);
        orderClient.delivery(deliveryEntity.getOrderId());
    }

    @Override
    @Transactional
    public void deliveryPicked(UUID deliveryId) {
        DeliveryEntity deliveryEntity = getDelivery(deliveryId);
        log.info("Доставка {} принята в работу", deliveryId);
        deliveryEntity.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(deliveryEntity);
        warehouseClient.shippedToDelivery(new ShippedToDeliveryRequest(deliveryEntity.getOrderId(), deliveryId));
    }

    @Override
    @Transactional
    public void deliveryFailed(UUID deliveryId) {
        DeliveryEntity deliveryEntity = getDelivery(deliveryId);
        log.info("Доставка {} завершилась ошибкой", deliveryId);
        deliveryEntity.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(deliveryEntity);
        orderClient.deliveryFailed(deliveryEntity.getOrderId());
    }

    @Override
    @Transactional
    public BigDecimal deliveryCost(OrderDto orderDto) {
        DeliveryEntity deliveryEntity = deliveryRepository.findByOrderId(orderDto.orderId())
                .orElseThrow(() -> new NoDeliveryFoundException(orderDto.deliveryId()));
        log.info("Расчёт стоимости доставки для заказа {}", orderDto.orderId());

        deliveryEntity.setDeliveryWeight(toBigDecimal(orderDto.deliveryWeight()));
        deliveryEntity.setDeliveryVolume(toBigDecimal(orderDto.deliveryVolume()));
        deliveryEntity.setFragile(requireFragileFlag(orderDto));
        deliveryRepository.save(deliveryEntity);

        BigDecimal total = BASE_COST;
        BigDecimal warehouseMultiplier = resolveWarehouseMultiplier(deliveryEntity.getFromAddress());
        total = total.add(BASE_COST.multiply(warehouseMultiplier));

        if (deliveryEntity.isFragile()) {
            total = total.add(total.multiply(FRAGILE_RATE));
        }

        total = total.add(defaultValue(deliveryEntity.getDeliveryWeight()).multiply(WEIGHT_RATE));
        total = total.add(defaultValue(deliveryEntity.getDeliveryVolume()).multiply(VOLUME_RATE));

        if (!sameStreet(deliveryEntity)) {
            total = total.add(total.multiply(FRAGILE_RATE));
        }

        return total.setScale(COST_SCALE, RoundingMode.HALF_UP);
    }

    private DeliveryEntity getDelivery(UUID deliveryId) {
        if (deliveryId == null) {
            throw new NoDeliveryFoundException(null);
        }
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException(deliveryId));
    }

    private BigDecimal resolveWarehouseMultiplier(DeliveryEntity.AddressEmbeddable address) {
        if (containsIgnoreCase(address.getCountry(), ADDRESS_2)
                || containsIgnoreCase(address.getCity(), ADDRESS_2)
                || containsIgnoreCase(address.getStreet(), ADDRESS_2)
                || containsIgnoreCase(address.getHouse(), ADDRESS_2)
                || containsIgnoreCase(address.getFlat(), ADDRESS_2)) {
            return DOUBLE_WAREHOUSE_RATE;
        }
        if (containsIgnoreCase(address.getCountry(), ADDRESS_1)
                || containsIgnoreCase(address.getCity(), ADDRESS_1)
                || containsIgnoreCase(address.getStreet(), ADDRESS_1)
                || containsIgnoreCase(address.getHouse(), ADDRESS_1)
                || containsIgnoreCase(address.getFlat(), ADDRESS_1)) {
            return SINGLE_WAREHOUSE_RATE;
        }
        return SINGLE_WAREHOUSE_RATE;
    }

    private boolean containsAddress2(DeliveryEntity.AddressEmbeddable address) {
        return containsIgnoreCase(address.getCountry(), ADDRESS_2)
                || containsIgnoreCase(address.getCity(), ADDRESS_2)
                || containsIgnoreCase(address.getStreet(), ADDRESS_2)
                || containsIgnoreCase(address.getHouse(), ADDRESS_2)
                || containsIgnoreCase(address.getFlat(), ADDRESS_2);
    }

    private boolean sameStreet(DeliveryEntity deliveryEntity) {
        String fromStreet = normalize(deliveryEntity.getFromAddress().getStreet());
        String toStreet = normalize(deliveryEntity.getToAddress().getStreet());
        return fromStreet.equals(toStreet);
    }

    private boolean containsIgnoreCase(String source, String candidate) {
        return normalize(source).contains(candidate.toLowerCase());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private BigDecimal defaultValue(BigDecimal value) {
        return value == null ? ZERO_VALUE : value;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private boolean requireFragileFlag(OrderDto orderDto) {
        if (orderDto.fragile() == null) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Для расчёта стоимости доставки требуется признак хрупкости заказа"
            );
        }
        return orderDto.fragile();
    }

    private DeliveryDto toDto(DeliveryEntity entity) {
        return new DeliveryDto(
                entity.getDeliveryId(),
                toAddressDto(entity.getFromAddress()),
                toAddressDto(entity.getToAddress()),
                entity.getOrderId(),
                entity.getDeliveryState()
        );
    }

    private AddressDto toAddressDto(DeliveryEntity.AddressEmbeddable embeddable) {
        return new AddressDto(
                embeddable.getCountry(),
                embeddable.getCity(),
                embeddable.getStreet(),
                embeddable.getHouse(),
                embeddable.getFlat()
        );
    }

    private DeliveryEntity.AddressEmbeddable toEmbeddable(AddressDto addressDto) {
        DeliveryEntity.AddressEmbeddable embeddable = new DeliveryEntity.AddressEmbeddable();
        embeddable.setCountry(addressDto.country());
        embeddable.setCity(addressDto.city());
        embeddable.setStreet(addressDto.street());
        embeddable.setHouse(addressDto.house());
        embeddable.setFlat(addressDto.flat());
        return embeddable;
    }
}
