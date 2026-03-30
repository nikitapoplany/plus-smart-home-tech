package ru.yandex.practicum.telemetry.collector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурационные параметры Kafka для сервиса Collector.
 */
@ConfigurationProperties(prefix = "app.kafka")
public class AppKafkaProperties {

    /**
     * Bootstrap servers для подключения к Kafka (например, localhost:9092).
     */
    private String bootstrapServers = "localhost:9092";

    private Topics topics = new Topics();

    public static class Topics {
        /** Топик для телеметрии датчиков. */
        private String sensors = "telemetry.sensors.v1";
        /** Топик для событий хабов. */
        private String hubs = "telemetry.hubs.v1";

        public String getSensors() {
            return sensors;
        }

        public void setSensors(String sensors) {
            this.sensors = sensors;
        }

        public String getHubs() {
            return hubs;
        }

        public void setHubs(String hubs) {
            this.hubs = hubs;
        }
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public Topics getTopics() {
        return topics;
    }

    public void setTopics(Topics topics) {
        this.topics = topics;
    }
}
