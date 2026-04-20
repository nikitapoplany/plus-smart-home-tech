package ru.yandex.practicum.telemetry.aggregator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурационные параметры Kafka для сервиса Aggregator.
 */
@ConfigurationProperties(prefix = "app.kafka")
public class AppKafkaProperties {

    /** Bootstrap servers для подключения к Kafka (например, localhost:9092). */
    private String bootstrapServers = "localhost:9092";

    /** Идентификатор группы консьюмера. */
    private String groupId = "aggregator-svc";

    /** Политика чтения оффсетов для консьюмера (earliest|latest). */
    private String autoOffsetReset = "latest";

    private Topics topics = new Topics();

    public static class Topics {
        /** Топик с сырыми событиями датчиков. */
        private String sensors = "telemetry.sensors.v1";
        /** Топик для публикации агрегированных снапшотов. */
        private String snapshots = "telemetry.snapshots.v1";

        public String getSensors() {
            return sensors;
        }

        public void setSensors(String sensors) {
            this.sensors = sensors;
        }

        public String getSnapshots() {
            return snapshots;
        }

        public void setSnapshots(String snapshots) {
            this.snapshots = snapshots;
        }
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public void setAutoOffsetReset(String autoOffsetReset) {
        this.autoOffsetReset = autoOffsetReset;
    }

    public Topics getTopics() {
        return topics;
    }

    public void setTopics(Topics topics) {
        this.topics = topics;
    }
}
