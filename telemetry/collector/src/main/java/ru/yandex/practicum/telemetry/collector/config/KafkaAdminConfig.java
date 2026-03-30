package ru.yandex.practicum.telemetry.collector.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Конфигурация администрирования Kafka без Spring Kafka.
 * - Регистрирует AdminClient (kafka-clients)
 * - Создаёт необходимые топики при старте приложения, если они отсутствуют
 */
@Configuration
public class KafkaAdminConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminConfig.class);

    @Bean(destroyMethod = "close")
    public AdminClient adminClient(AppKafkaProperties props) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        return AdminClient.create(configs);
    }

    @Bean
    public ApplicationRunner ensureTopics(AdminClient adminClient, AppKafkaProperties props) {
        return args -> {
            String sensors = props.getTopics().getSensors();
            String hubs = props.getTopics().getHubs();

            // Определяем отсутствующие топики
            Set<String> existing = new HashSet<>(adminClient.listTopics().names().get());
            List<NewTopic> toCreate = new ArrayList<>();
            if (!existing.contains(sensors)) {
                toCreate.add(new NewTopic(sensors, 1, (short) 1));
            }
            if (!existing.contains(hubs)) {
                toCreate.add(new NewTopic(hubs, 1, (short) 1));
            }

            if (toCreate.isEmpty()) {
                log.info("Топики Kafka уже существуют: sensors='{}', hubs='{}'", sensors, hubs);
                return;
            }

            log.info("Создаём отсутствующие топики Kafka: {}", toCreate);
            Map<String, KafkaFuture<Void>> results = adminClient.createTopics(toCreate).values();
            for (Map.Entry<String, KafkaFuture<Void>> e : results.entrySet()) {
                try {
                    e.getValue().get();
                    log.info("Топик создан: {}", e.getKey());
                } catch (Exception ex) {
                    log.warn("Не удалось создать топик {}: {}", e.getKey(), ex.toString());
                }
            }
        };
    }
}
