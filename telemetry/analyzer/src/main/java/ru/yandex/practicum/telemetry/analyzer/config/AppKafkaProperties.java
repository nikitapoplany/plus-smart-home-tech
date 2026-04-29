package ru.yandex.practicum.telemetry.analyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public class AppKafkaProperties {
    private String bootstrapServers = "localhost:9092";
    private Topics topics = new Topics();
    private Groups groups = new Groups();

    public static class Topics {
        private String snapshots = "telemetry.snapshots.v1";
        private String hubs = "telemetry.hubs.v1";

        public String getSnapshots() { return snapshots; }
        public void setSnapshots(String snapshots) { this.snapshots = snapshots; }
        public String getHubs() { return hubs; }
        public void setHubs(String hubs) { this.hubs = hubs; }
    }

    public static class Groups {
        private String snapshots = "analyzer-snapshots";
        private String hubs = "analyzer-hubs";

        public String getSnapshots() { return snapshots; }
        public void setSnapshots(String snapshots) { this.snapshots = snapshots; }
        public String getHubs() { return hubs; }
        public void setHubs(String hubs) { this.hubs = hubs; }
    }

    public String getBootstrapServers() { return bootstrapServers; }
    public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }
    public Topics getTopics() { return topics; }
    public void setTopics(Topics topics) { this.topics = topics; }
    public Groups getGroups() { return groups; }
    public void setGroups(Groups groups) { this.groups = groups; }
}
