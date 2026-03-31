package ru.yandex.practicum.telemetry.aggregator;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.aggregator.config.AppKafkaProperties;
import ru.yandex.practicum.telemetry.aggregator.service.serialization.AvroEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Component
public class AggregationStarter {

    private static final Logger log = LoggerFactory.getLogger(AggregationStarter.class);

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, byte[]> producer;
    private final AppKafkaProperties props;
    private final AvroEncoder avroEncoder;

    // Хранилище актуальных снапшотов по hubId
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public AggregationStarter(KafkaConsumer<String, SensorEventAvro> consumer,
                              KafkaProducer<String, byte[]> producer,
                              AppKafkaProperties props,
                              AvroEncoder avroEncoder) {
        this.consumer = consumer;
        this.producer = producer;
        this.props = props;
        this.avroEncoder = avroEncoder;
    }

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топик с событиями сенсоров, формирует снимок их состояния и записывает в Kafka.
     */
    public void start() {
        try {
            log.info("Подписываемся на топик с событиями датчиков: {}", props.getTopics().getSensors());
            consumer.subscribe(Collections.singletonList(props.getTopics().getSensors()));

            // Цикл обработки событий
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofSeconds(1));
                if (records.isEmpty()) {
                    continue; // ждём новые события
                }

                int updated = 0;
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();
                    if (event == null) {
                        continue;
                    }
                    Optional<SensorsSnapshotAvro> maybeSnapshot = updateState(event);
                    if (maybeSnapshot.isPresent()) {
                        SensorsSnapshotAvro snapshot = maybeSnapshot.get();
                        // Публикуем обновлённый снапшот в Kafka
                        byte[] payload = avroEncoder.toBytes(snapshot);
                        producer.send(new ProducerRecord<>(props.getTopics().getSnapshots(), snapshot.getHubId(), payload));
                        updated++;
                    }
                }

                // Фиксируем смещения только после успешной обработки батча
                consumer.commitSync();
                if (updated > 0) {
                    log.debug("За батч обновлено снапшотов: {}", updated);
                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                // Перед закрытием убеждаемся, что все данные отправлены и оффсеты зафиксированы
                try {
                    log.info("Сбрасываем буфер продюсера");
                    producer.flush();
                } catch (Exception e) {
                    log.warn("Ошибка при flush продюсера: {}", e.getMessage());
                }
                try {
                    log.info("Фиксируем смещения консьюмера");
                    consumer.commitSync();
                } catch (Exception e) {
                    log.warn("Ошибка при commitSync консьюмера: {}", e.getMessage());
                }
            } finally {
                log.info("Закрываем консьюмер");
                try { consumer.close(); } catch (Exception ignore) {}
                log.info("Закрываем продюсер");
                try { producer.close(); } catch (Exception ignore) {}
            }
        }
    }

    /**
     * Обновляет снапшот конкретного хаба на основе события датчика.
     * Возвращает Optional пустой, если снапшот не изменился.
     */
    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();
        Instant eventTs = event.getTimestamp();
        Object newData = event.getPayload();

        // Получаем или создаём новый снапшот для хаба
        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, h ->
                new SensorsSnapshotAvro(hubId, eventTs, new HashMap<>())
        );

        Map<String, SensorStateAvro> stateMap = snapshot.getSensorsState();
        SensorStateAvro oldState = stateMap.get(sensorId);

        if (oldState != null) {
            // Если старые данные новее текущего события — игнорируем
            Instant oldTs = oldState.getTimestamp();
            if (oldTs != null && oldTs.isAfter(eventTs)) {
                return Optional.empty();
            }
            // Если данные не изменились — тоже игнорируем
            if (Objects.equals(oldState.getData(), newData)) {
                return Optional.empty();
            }
        }

        // Создаём/обновляем состояние датчика
        SensorStateAvro newState = new SensorStateAvro(eventTs, newData);
        stateMap.put(sensorId, newState);

        // Обновляем таймстемп снапшота
        snapshot.setTimestamp(eventTs);

        return Optional.of(snapshot);
    }
}
