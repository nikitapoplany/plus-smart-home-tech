package ru.yandex.practicum.telemetry.collector.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация администрирования Kafka.
 *
 * - Регистрирует KafkaAdmin с настройкой bootstrapServers из AppKafkaProperties
 * - Создаёт топики при старте приложения, если они отсутствуют
 */
@Configuration
public class KafkaAdminConfig {

    @Bean
    public KafkaAdmin kafkaAdmin(AppKafkaProperties props) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        return new KafkaAdmin(configs);
    }

    /**
     * Топик телеметрии сенсоров.
     */
    @Bean
    public NewTopic sensorsTopic(AppKafkaProperties props) {
        return new NewTopic(props.getTopics().getSensors(), 1, (short) 1);
    }

    /**
     * Топик событий хабов.
     */
    @Bean
    public NewTopic hubsTopic(AppKafkaProperties props) {
        return new NewTopic(props.getTopics().getHubs(), 1, (short) 1);
    }
}
