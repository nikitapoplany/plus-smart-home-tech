package ru.yandex.practicum.commerce.warehouse.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.dto.store.QuantityState;
import ru.yandex.practicum.commerce.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProductEntity;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

@Service
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    private final WarehouseProductRepository warehouseProductRepository;
    private final ShoppingStoreClient shoppingStoreClient;

    public WarehouseServiceImpl(WarehouseProductRepository warehouseProductRepository,
                                ShoppingStoreClient shoppingStoreClient) {
        this.warehouseProductRepository = warehouseProductRepository;
        this.shoppingStoreClient = shoppingStoreClient;
    }

    @Override
    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        UUID productId = request.productId();
        if (warehouseProductRepository.existsById(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException(productId);
        }
        WarehouseProductEntity entity = new WarehouseProductEntity();
        entity.setProductId(productId);
        entity.setFragile(Boolean.TRUE.equals(request.fragile()));
        entity.setWidth(request.dimension().width());
        entity.setHeight(request.dimension().height());
        entity.setDepth(request.dimension().depth());
        entity.setWeight(request.weight());
        entity.setQuantity(0);
        warehouseProductRepository.save(entity);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        Map<UUID, Long> products = shoppingCartDto.products();
        List<String> shortages = new ArrayList<>();
        double totalWeight = 0;
        double totalVolume = 0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            WarehouseProductEntity product = warehouseProductRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ProductInShoppingCartLowQuantityInWarehouse(
                            "Недостаточно товара на складе: " + entry.getKey()
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
        }

        if (!shortages.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(
                    "Недостаточное количество товаров на складе: " + String.join(", ", shortages)
            );
        }

        return new BookedProductsDto(totalWeight, totalVolume, fragile);
    }

    @Override
    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        UUID productId = request.productId();
        if (productId == null) {
            throw new IllegalArgumentException("Для пополнения склада требуется productId");
        }
        WarehouseProductEntity product = warehouseProductRepository.findById(productId)
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));
        product.setQuantity(product.getQuantity() + request.quantity());
        warehouseProductRepository.save(product);
        shoppingStoreClient.setProductQuantityState(
                new SetProductQuantityStateRequest(productId, resolveQuantityState(product.getQuantity()))
        );
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return new AddressDto(
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS
        );
    }

    private QuantityState resolveQuantityState(long quantity) {
        if (quantity <= 0) {
            return QuantityState.ENDED;
        }
        if (quantity < 10) {
            return QuantityState.FEW;
        }
        if (quantity <= 100) {
            return QuantityState.ENOUGH;
        }
        return QuantityState.MANY;
    }
}
