package ru.yandex.practicum.commerce.order.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import ru.yandex.practicum.commerce.dto.order.OrderState;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "shopping_cart_id")
    private UUID shoppingCartId;

    @Column(name = "username")
    private String username;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    private Map<UUID, Long> products = new LinkedHashMap<>();

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState state;

    @Column(name = "delivery_weight")
    private Double deliveryWeight;

    @Column(name = "delivery_volume")
    private Double deliveryVolume;

    private Boolean fragile;

    @Column(name = "total_price", precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "delivery_price", precision = 19, scale = 2)
    private BigDecimal deliveryPrice;

    @Column(name = "product_price", precision = 19, scale = 2)
    private BigDecimal productPrice;

    @Embedded
    private DeliveryAddressEmbeddable deliveryAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getShoppingCartId() {
        return shoppingCartId;
    }

    public void setShoppingCartId(UUID shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<UUID, Long> getProducts() {
        return products;
    }

    public void setProducts(Map<UUID, Long> products) {
        this.products = products;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public Double getDeliveryWeight() {
        return deliveryWeight;
    }

    public void setDeliveryWeight(Double deliveryWeight) {
        this.deliveryWeight = deliveryWeight;
    }

    public Double getDeliveryVolume() {
        return deliveryVolume;
    }

    public void setDeliveryVolume(Double deliveryVolume) {
        this.deliveryVolume = deliveryVolume;
    }

    public Boolean getFragile() {
        return fragile;
    }

    public void setFragile(Boolean fragile) {
        this.fragile = fragile;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(BigDecimal deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public DeliveryAddressEmbeddable getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(DeliveryAddressEmbeddable deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Embeddable
    public static class DeliveryAddressEmbeddable {

        @Column(name = "delivery_country")
        private String country;

        @Column(name = "delivery_city")
        private String city;

        @Column(name = "delivery_street")
        private String street;

        @Column(name = "delivery_house")
        private String house;

        @Column(name = "delivery_flat")
        private String flat;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getHouse() {
            return house;
        }

        public void setHouse(String house) {
            this.house = house;
        }

        public String getFlat() {
            return flat;
        }

        public void setFlat(String flat) {
            this.flat = flat;
        }
    }
}
