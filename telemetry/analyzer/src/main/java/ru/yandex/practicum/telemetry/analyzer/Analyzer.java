package ru.yandex.practicum.telemetry.analyzer;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import ru.yandex.practicum.telemetry.analyzer.processor.HubEventProcessor;
import ru.yandex.practicum.telemetry.analyzer.processor.SnapshotProcessor;

/**
 * Главный класс сервиса Analyzer.
 * Запускает два независимых цикла опроса Kafka:
 *  - обработчик событий хаба/сценариев (в отдельном потоке)
 *  - обработчик снимков состояния сенсоров (в текущем потоке)
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class Analyzer {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Analyzer.class, args);

        final HubEventProcessor hubEventProcessor = context.getBean(HubEventProcessor.class);
        final SnapshotProcessor snapshotProcessor = context.getBean(SnapshotProcessor.class);

        // запускаем обработчик событий хаба в отдельном потоке
        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();

        // запускаем обработку снимков в текущем потоке
        snapshotProcessor.start();
    }
}
