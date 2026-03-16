package ru.yandex.practicum.telemetry.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import ru.yandex.practicum.telemetry.collector.config.AppKafkaProperties;

/**
 * Точка входа Spring Boot приложения Collector.
 * Сервис принимает события по HTTP (JSON), кодирует их в Avro и публикует в Kafka.
 */
@SpringBootApplication
@EnableConfigurationProperties(AppKafkaProperties.class)
public class CollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectorApplication.class, args);
    }

    /**
     * Регистрируем модуль JavaTime для корректной (де)сериализации Instant и др. типов JSR-310.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
