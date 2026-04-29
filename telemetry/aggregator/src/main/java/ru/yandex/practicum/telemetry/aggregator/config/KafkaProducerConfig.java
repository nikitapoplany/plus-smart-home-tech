package ru.yandex.practicum.telemetry.aggregator.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация Kafka Producer для отправки бинарных Avro-сообщений без Spring Kafka.
 */
@Configuration
public class KafkaProducerConfig {

    /**
     * Фабрика KafkaProducer, управляемая Spring. Контейнер корректно закроет producer при остановке приложения.
     */
    @Bean(destroyMethod = "close")
    public KafkaProducer<String, byte[]> kafkaProducer(AppKafkaProperties props) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        // Минимальные настройки производительности/надежности по умолчанию
        config.put(ProducerConfig.ACKS_CONFIG, "1");
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new KafkaProducer<>(config);
    }
}
