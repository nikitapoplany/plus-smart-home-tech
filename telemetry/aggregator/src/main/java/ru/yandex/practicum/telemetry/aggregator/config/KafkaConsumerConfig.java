package ru.yandex.practicum.telemetry.aggregator.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.telemetry.serialization.avro.SensorEventDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация Kafka Consumer для чтения Avro-событий датчиков без Spring Kafka.
 */
@Configuration
public class KafkaConsumerConfig {

    @Bean(destroyMethod = "close")
    public KafkaConsumer<String, ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro> kafkaConsumer(AppKafkaProperties props) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, props.getGroupId());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, props.getAutoOffsetReset());
        return new KafkaConsumer<>(config);
    }
}
