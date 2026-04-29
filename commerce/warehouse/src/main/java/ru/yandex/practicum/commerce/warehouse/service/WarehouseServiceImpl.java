package ru.yandex.practicum.commerce.warehouse.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.dto.store.QuantityState;
import ru.yandex.practicum.commerce.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.exception.ProductInShoppingCartNotInWarehouse;
import ru.yandex.practicum.commerce.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.OrderBookingEntity;
import ru.yandex.practicum.commerce.warehouse.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProductEntity;
import ru.yandex.practicum.commerce.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

@Service
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseServiceImpl.class);

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final long FEW_QUANTITY_THRESHOLD = 10L;
    private static final long ENOUGH_QUANTITY_THRESHOLD = 100L;
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    private final OrderBookingRepository orderBookingRepository;
    private final WarehouseProductRepository warehouseProductRepository;
    private final ShoppingStoreClient shoppingStoreClient;

    public WarehouseServiceImpl(OrderBookingRepository orderBookingRepository,
                                WarehouseProductRepository warehouseProductRepository,
                                ShoppingStoreClient shoppingStoreClient) {
        this.orderBookingRepository = orderBookingRepository;
        this.warehouseProductRepository = warehouseProductRepository;
        this.shoppingStoreClient = shoppingStoreClient;
    }

    @Override
    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        UUID productId = request.productId();
        log.info("Регистрация нового товара {} на складе", productId);
        if (warehouseProductRepository.existsById(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException(productId);
        }
        WarehouseProductEntity entity = new WarehouseProductEntity();
        entity.setProductId(productId);
        entity.setFragile(request.fragile());
        entity.setWidth(request.dimension().width());
        entity.setHeight(request.dimension().height());
        entity.setDepth(request.dimension().depth());
        entity.setWeight(request.weight());
        entity.setQuantity(0);
        warehouseProductRepository.save(entity);
        log.debug("Товар {} успешно зарегистрирован на складе", productId);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        Map<UUID, Long> products = shoppingCartDto.products();
        log.debug("Проверка наличия {} товаров на складе для корзины {}",
                products.size(), shoppingCartDto.shoppingCartId());
        BookedProductsDto bookedProducts = calculateBookedProducts(products, false);
        log.debug("Проверка корзины {} на складе завершена успешно", shoppingCartDto.shoppingCartId());
        return bookedProducts;
    }

    @Override
    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        UUID productId = request.productId();
        if (productId == null) {
            throw new IllegalArgumentException("Для пополнения склада требуется productId");
        }
        log.info("Пополнение товара {} на складе на {}", productId, request.quantity());
        WarehouseProductEntity product = warehouseProductRepository.findById(productId)
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));
        product.setQuantity(product.getQuantity() + request.quantity());
        warehouseProductRepository.save(product);
        shoppingStoreClient.setProductQuantityState(
                new SetProductQuantityStateRequest(productId, resolveQuantityState(product.getQuantity()))
        );
        log.debug("Остаток товара {} на складе обновлён до {}", productId, product.getQuantity());
    }

    @Override
    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("Передача заказа {} в доставку {}", request.orderId(), request.deliveryId());
        OrderBookingEntity orderBooking = orderBookingRepository.findById(request.orderId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(request.orderId()));
        orderBooking.setDeliveryId(request.deliveryId());
        orderBookingRepository.save(orderBooking);
    }

    @Override
    @Transactional
    public void acceptReturn(Map<UUID, Long> products) {
        log.info("Приём возврата по {} товарам", products.size());
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            WarehouseProductEntity product = warehouseProductRepository.findById(entry.getKey())
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(entry.getKey()));
            product.setQuantity(product.getQuantity() + entry.getValue());
            warehouseProductRepository.save(product);
            shoppingStoreClient.setProductQuantityState(
                    new SetProductQuantityStateRequest(entry.getKey(), resolveQuantityState(product.getQuantity()))
            );
        }
    }

    @Override
    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        log.info("Сборка заказа {} на складе", request.orderId());
        BookedProductsDto bookedProducts = calculateBookedProducts(request.products(), true);

        OrderBookingEntity orderBooking = new OrderBookingEntity();
        orderBooking.setOrderId(request.orderId());
        orderBooking.setProducts(new java.util.LinkedHashMap<>(request.products()));
        orderBookingRepository.save(orderBooking);
        return bookedProducts;
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.debug("Запрос адреса склада");
        return new AddressDto(
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS
        );
    }

    private BookedProductsDto calculateBookedProducts(Map<UUID, Long> products, boolean reserveProducts) {
        List<String> shortages = new ArrayList<>();
        double totalWeight = 0;
        double totalVolume = 0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            WarehouseProductEntity product = warehouseProductRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ProductInShoppingCartNotInWarehouse(
                            "На складе отсутствует товар " + entry.getKey()
                    ));
            long requestedQuantity = entry.getValue();
            if (product.getQuantity() < requestedQuantity) {
                shortages.add(entry.getKey() + " (запрошено=" + requestedQuantity
                        + ", доступно=" + product.getQuantity() + ")");
                continue;
            }
            totalWeight += product.getWeight() * requestedQuantity;
            totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requestedQuantity;
            fragile = fragile || product.isFragile();

            if (reserveProducts) {
                product.setQuantity(product.getQuantity() - requestedQuantity);
                warehouseProductRepository.save(product);
                shoppingStoreClient.setProductQuantityState(
                        new SetProductQuantityStateRequest(product.getProductId(), resolveQuantityState(product.getQuantity()))
                );
            }
        }

        if (!shortages.isEmpty()) {
            log.warn("На складе недостаточно товаров: {}", shortages);
            throw new ProductInShoppingCartLowQuantityInWarehouse(
                    "Недостаточное количество товаров на складе: " + String.join(", ", shortages)
            );
        }

        return new BookedProductsDto(totalWeight, totalVolume, fragile);
    }

    private QuantityState resolveQuantityState(long quantity) {
        if (quantity <= 0) {
            return QuantityState.ENDED;
        }
        if (quantity < FEW_QUANTITY_THRESHOLD) {
            return QuantityState.FEW;
        }
        if (quantity <= ENOUGH_QUANTITY_THRESHOLD) {
            return QuantityState.ENOUGH;
        }
        return QuantityState.MANY;
    }
}
