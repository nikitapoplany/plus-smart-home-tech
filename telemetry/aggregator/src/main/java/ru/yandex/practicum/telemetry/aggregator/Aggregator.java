package ru.yandex.practicum.telemetry.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Главный класс сервиса Aggregator.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class Aggregator {

    public static void main(String[] args) {
        // Запуск Spring Boot приложения при помощи SpringApplication
        // метод run возвращает контекст, из которого достанем бин AggregationStarter
        ConfigurableApplicationContext context = SpringApplication.run(Aggregator.class, args);

        // Получаем бин AggregationStarter из контекста и запускаем основную логику сервиса
        AggregationStarter starter = context.getBean(AggregationStarter.class);
        starter.start();
    }
}
