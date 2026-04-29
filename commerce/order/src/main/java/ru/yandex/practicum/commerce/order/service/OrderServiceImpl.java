package ru.yandex.practicum.commerce.order.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryState;
import ru.yandex.practicum.commerce.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.order.OrderState;
import ru.yandex.practicum.commerce.dto.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.order.client.DeliveryClient;
import ru.yandex.practicum.commerce.order.client.PaymentClient;
import ru.yandex.practicum.commerce.order.client.WarehouseClient;
import ru.yandex.practicum.commerce.order.model.OrderEntity;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;
    private final WarehouseClient warehouseClient;

    public OrderServiceImpl(OrderRepository orderRepository,
                            DeliveryClient deliveryClient,
                            PaymentClient paymentClient,
                            WarehouseClient warehouseClient) {
        this.orderRepository = orderRepository;
        this.deliveryClient = deliveryClient;
        this.paymentClient = paymentClient;
        this.warehouseClient = warehouseClient;
    }

    @Override
    public List<OrderDto> getClientOrders(String username) {
        String normalizedUsername = validateUsername(username);
        log.info("Получение заказов пользователя {}", normalizedUsername);
        List<OrderEntity> orders = orderRepository.findAllByUsernameOrderByCreatedAtDesc(normalizedUsername);
        if (orders.isEmpty()) {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        }
        return orders
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("Создание нового заказа по корзине {}", request.shoppingCart().shoppingCartId());
        warehouseClient.checkProductQuantityEnoughForShoppingCart(request.shoppingCart());

        OrderEntity order = new OrderEntity();
        order.setShoppingCartId(request.shoppingCart().shoppingCartId());
        order.setUsername(request.shoppingCart().username());
        order.setProducts(new LinkedHashMap<>(request.shoppingCart().products()));
        order.setState(OrderState.NEW);
        order.setDeliveryAddress(toEmbeddable(request.deliveryAddress()));
        OrderEntity savedOrder = orderRepository.save(order);

        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
        DeliveryDto delivery = deliveryClient.planDelivery(new DeliveryDto(
                null,
                warehouseAddress,
                request.deliveryAddress(),
                savedOrder.getOrderId(),
                DeliveryState.CREATED
        ));
        savedOrder.setDeliveryId(delivery.deliveryId());

        log.debug("Заказ {} создан, доставка {} запланирована", savedOrder.getOrderId(), savedOrder.getDeliveryId());
        return toDto(orderRepository.save(savedOrder));
    }

    @Override
    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        OrderEntity order = getOrder(request.orderId());
        log.info("Оформление возврата по заказу {}", request.orderId());
        warehouseClient.acceptReturn(request.products());
        order.setState(OrderState.PRODUCT_RETURNED);
        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto payment(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        log.info("Обработка оплаты заказа {}", orderId);

        if (order.getPaymentId() == null) {
            recalculateProductPriceIfNeeded(order);
            recalculateDeliveryPriceIfNeeded(order);
            recalculateTotalPriceIfNeeded(order);

            PaymentDto payment = paymentClient.payment(toDto(order));
            order.setPaymentId(payment.paymentId());
            order.setState(OrderState.ON_PAYMENT);
            log.debug("Для заказа {} создана оплата {}", orderId, payment.paymentId());
        } else if (order.getState() == OrderState.ON_PAYMENT) {
            order.setState(OrderState.PAID);
            log.debug("Заказ {} помечен как оплаченный", orderId);
        } else {
            log.debug("Повторное подтверждение оплаты для заказа {} проигнорировано в статусе {}",
                    orderId, order.getState());
        }

        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        log.info("Заказ {} получил статус ошибки оплаты", orderId);
        if (order.getState() == OrderState.ON_PAYMENT) {
            order.setState(OrderState.PAYMENT_FAILED);
        } else {
            log.debug("Повторный отказ в оплате для заказа {} проигнорирован в статусе {}",
                    orderId, order.getState());
        }
        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto delivery(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        log.info("Заказ {} доставлен", orderId);
        if (order.getState() == OrderState.ON_DELIVERY) {
            order.setState(OrderState.DELIVERED);
        } else {
            log.debug("Повторное подтверждение доставки для заказа {} проигнорировано в статусе {}",
                    orderId, order.getState());
        }
        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        log.info("Заказ {} получил статус ошибки доставки", orderId);
        if (order.getState() == OrderState.ON_DELIVERY) {
            order.setState(OrderState.DELIVERY_FAILED);
        } else {
            log.debug("Повторная ошибка доставки для заказа {} проигнорирована в статусе {}",
                    orderId, order.getState());
        }
        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto complete(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        log.info("Завершение обработки заказа {}", orderId);

        if (order.getState() == OrderState.DELIVERED) {
            order.setState(OrderState.COMPLETED);
        } else if (order.getState() == OrderState.PAID) {
            if (order.getDeliveryId() == null) {
                throw new NoOrderFoundException(orderId);
            }
            deliveryClient.deliveryPicked(order.getDeliveryId());
            order.setState(OrderState.ON_DELIVERY);
        } else {
            log.debug("Переход заказа {} в следующий этап пропущен для статуса {}",
                    orderId, order.getState());
        }

        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        log.info("Расчёт полной стоимости заказа {}", orderId);
        recalculateProductPriceIfNeeded(order);
        recalculateDeliveryPriceIfNeeded(order);
        order.setTotalPrice(paymentClient.getTotalCost(toDto(order)));
        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        log.info("Расчёт стоимости доставки заказа {}", orderId);
        populateDeliveryDetailsIfMissing(order);
        order.setDeliveryPrice(deliveryClient.deliveryCost(toDto(order)));
        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto assembly(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        log.info("Сборка заказа {}", orderId);
        BookedProductsDto bookedProducts = warehouseClient.assemblyProductsForOrder(
                new AssemblyProductsForOrderRequest(order.getProducts(), orderId)
        );
        applyBookedProducts(order, bookedProducts);
        order.setState(OrderState.ASSEMBLED);
        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        log.info("Заказ {} получил статус ошибки сборки", orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        return toDto(orderRepository.save(order));
    }

    private void recalculateProductPriceIfNeeded(OrderEntity order) {
        if (order.getProductPrice() == null) {
            order.setProductPrice(paymentClient.productCost(toDto(order)));
        }
    }

    private void recalculateDeliveryPriceIfNeeded(OrderEntity order) {
        if (order.getDeliveryPrice() == null) {
            populateDeliveryDetailsIfMissing(order);
            order.setDeliveryPrice(deliveryClient.deliveryCost(toDto(order)));
        }
    }

    private void recalculateTotalPriceIfNeeded(OrderEntity order) {
        if (order.getTotalPrice() == null) {
            order.setTotalPrice(paymentClient.getTotalCost(toDto(order)));
        }
    }

    private void populateDeliveryDetailsIfMissing(OrderEntity order) {
        if (order.getDeliveryWeight() != null && order.getDeliveryVolume() != null && order.getFragile() != null) {
            return;
        }

        BookedProductsDto bookedProducts = warehouseClient.checkProductQuantityEnoughForShoppingCart(
                new ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto(
                        order.getShoppingCartId(),
                        order.getProducts(),
                        order.getUsername()
                )
        );
        applyBookedProducts(order, bookedProducts);
    }

    private void applyBookedProducts(OrderEntity order, BookedProductsDto bookedProducts) {
        order.setDeliveryWeight(bookedProducts.deliveryWeight());
        order.setDeliveryVolume(bookedProducts.deliveryVolume());
        order.setFragile(bookedProducts.fragile());
    }

    private OrderEntity getOrder(UUID orderId) {
        if (orderId == null) {
            throw new NoOrderFoundException(null);
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(orderId));
    }

    private String validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }
        return username.trim();
    }

    private OrderDto toDto(OrderEntity entity) {
        return new OrderDto(
                entity.getOrderId(),
                entity.getShoppingCartId(),
                new LinkedHashMap<>(entity.getProducts()),
                entity.getPaymentId(),
                entity.getDeliveryId(),
                entity.getState(),
                entity.getDeliveryWeight(),
                entity.getDeliveryVolume(),
                entity.getFragile(),
                entity.getTotalPrice(),
                entity.getDeliveryPrice(),
                entity.getProductPrice()
        );
    }

    private OrderEntity.DeliveryAddressEmbeddable toEmbeddable(AddressDto addressDto) {
        OrderEntity.DeliveryAddressEmbeddable embeddable = new OrderEntity.DeliveryAddressEmbeddable();
        embeddable.setCountry(addressDto.country());
        embeddable.setCity(addressDto.city());
        embeddable.setStreet(addressDto.street());
        embeddable.setHouse(addressDto.house());
        embeddable.setFlat(addressDto.flat());
        return embeddable;
    }
}
