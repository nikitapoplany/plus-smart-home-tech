package ru.yandex.practicum.commerce.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.dto.payment.PaymentState;
import ru.yandex.practicum.commerce.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.commerce.payment.client.OrderClient;
import ru.yandex.practicum.commerce.payment.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

@Service
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final ShoppingStoreClient shoppingStoreClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderClient orderClient,
                              ShoppingStoreClient shoppingStoreClient) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.shoppingStoreClient = shoppingStoreClient;
    }

    @Override
    @Transactional
    public PaymentDto payment(OrderDto orderDto) {
        validateOrderForPayment(orderDto);
        log.info("Формирование оплаты для заказа {}", orderDto.orderId());

        BigDecimal productTotal = resolveProductTotal(orderDto);
        BigDecimal deliveryTotal = requireDeliveryPrice(orderDto);
        BigDecimal feeTotal = calculateFee(productTotal);
        BigDecimal totalPayment = resolveTotalPayment(orderDto, productTotal, deliveryTotal, feeTotal);

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setOrderId(orderDto.orderId());
        paymentEntity.setProductTotal(productTotal);
        paymentEntity.setDeliveryTotal(deliveryTotal);
        paymentEntity.setFeeTotal(feeTotal);
        paymentEntity.setTotalPayment(totalPayment);
        paymentEntity.setState(PaymentState.PENDING);

        PaymentEntity savedPayment = paymentRepository.save(paymentEntity);
        log.debug("Для заказа {} создана оплата {}", orderDto.orderId(), savedPayment.getPaymentId());
        return toDto(savedPayment);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto orderDto) {
        validateOrderForCalculation(orderDto);
        BigDecimal productTotal = resolveProductTotal(orderDto);
        BigDecimal deliveryTotal = requireDeliveryPrice(orderDto);
        BigDecimal feeTotal = calculateFee(productTotal);
        return productTotal.add(feeTotal).add(deliveryTotal);
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID paymentId) {
        PaymentEntity paymentEntity = getPayment(paymentId);
        log.info("Оплата {} подтверждена успешно", paymentId);
        paymentEntity.setState(PaymentState.SUCCESS);
        paymentRepository.save(paymentEntity);
        orderClient.payment(paymentEntity.getOrderId());
    }

    @Override
    public BigDecimal productCost(OrderDto orderDto) {
        validateOrderForCalculation(orderDto);
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<UUID, Long> entry : orderDto.products().entrySet()) {
            BigDecimal productPrice = shoppingStoreClient.getProduct(entry.getKey()).price();
            total = total.add(productPrice.multiply(BigDecimal.valueOf(entry.getValue())));
        }
        return total;
    }

    @Override
    @Transactional
    public void paymentFailed(UUID paymentId) {
        PaymentEntity paymentEntity = getPayment(paymentId);
        log.info("Оплата {} завершилась ошибкой", paymentId);
        paymentEntity.setState(PaymentState.FAILED);
        paymentRepository.save(paymentEntity);
        orderClient.paymentFailed(paymentEntity.getOrderId());
    }

    private PaymentEntity getPayment(UUID paymentId) {
        if (paymentId == null) {
            throw new NoOrderFoundException(null);
        }
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException(paymentId));
    }

    private void validateOrderForPayment(OrderDto orderDto) {
        validateOrderForCalculation(orderDto);
        if (orderDto.orderId() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Для создания оплаты требуется идентификатор заказа");
        }
    }

    private void validateOrderForCalculation(OrderDto orderDto) {
        if (orderDto == null || orderDto.products() == null || orderDto.products().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("Для расчёта стоимости заказа требуется список товаров");
        }
    }

    private BigDecimal resolveProductTotal(OrderDto orderDto) {
        return orderDto.productPrice() != null ? orderDto.productPrice() : productCost(orderDto);
    }

    private BigDecimal requireDeliveryPrice(OrderDto orderDto) {
        if (orderDto.deliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Для расчёта итоговой стоимости требуется предварительно рассчитанная стоимость доставки"
            );
        }
        return orderDto.deliveryPrice();
    }

    private BigDecimal calculateFee(BigDecimal productTotal) {
        return productTotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveTotalPayment(OrderDto orderDto,
                                           BigDecimal productTotal,
                                           BigDecimal deliveryTotal,
                                           BigDecimal feeTotal) {
        if (orderDto.totalPrice() != null) {
            return orderDto.totalPrice();
        }
        return productTotal.add(feeTotal).add(deliveryTotal);
    }

    private PaymentDto toDto(PaymentEntity paymentEntity) {
        return new PaymentDto(
                paymentEntity.getPaymentId(),
                paymentEntity.getTotalPayment(),
                paymentEntity.getDeliveryTotal(),
                paymentEntity.getFeeTotal()
        );
    }
}
