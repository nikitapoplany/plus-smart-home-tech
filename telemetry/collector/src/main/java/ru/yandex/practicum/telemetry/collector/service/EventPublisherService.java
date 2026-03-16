package ru.yandex.practicum.telemetry.collector.service;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.api.dto.hub.HubEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.sensor.SensorEventDto;
import ru.yandex.practicum.telemetry.collector.config.AppKafkaProperties;
import ru.yandex.practicum.telemetry.collector.service.mapper.DtoToAvroMapper;
import ru.yandex.practicum.telemetry.collector.service.serialization.AvroEncoder;

import java.util.concurrent.CompletableFuture;

/**
 * Публикация событий в Kafka (в бинарном Avro-формате).
 */
@Service
public class EventPublisherService {

    private static final Logger log = LoggerFactory.getLogger(EventPublisherService.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final DtoToAvroMapper mapper;
    private final AvroEncoder encoder;
    private final AppKafkaProperties props;

    public EventPublisherService(KafkaTemplate<String, byte[]> kafkaTemplate,
                                 DtoToAvroMapper mapper,
                                 AvroEncoder encoder,
                                 AppKafkaProperties props) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
        this.encoder = encoder;
        this.props = props;
    }

    /**
     * Отправка события сенсора в топик telemetry.sensors.v1
     */
    public void publishSensorEvent(SensorEventDto dto) {
        SensorEventAvro avro = mapper.toAvro(dto);
        byte[] bytes = encoder.toBytes(avro);
        String key = dto.getId();
        String topic = props.getTopics().getSensors();
        log.info("Получено событие сенсора: type={}, id={}, hubId={}", dto.getType(), dto.getId(), dto.getHubId());
        CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(topic, key, bytes);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Ошибка отправки в Kafka (topic={}, key={})", topic, key, ex);
            } else if (result != null) {
                RecordMetadata meta = result.getRecordMetadata();
                log.info("Событие сенсора отправлено в Kafka: topic={}, partition={}, offset={}", meta.topic(), meta.partition(), meta.offset());
            }
        });
    }

    /**
     * Отправка события хаба в топик telemetry.hubs.v1
     */
    public void publishHubEvent(HubEventDto dto) {
        HubEventAvro avro = mapper.toAvro(dto);
        byte[] bytes = encoder.toBytes(avro);
        String key = dto.getHubId();
        String topic = props.getTopics().getHubs();
        log.info("Получено событие хаба: type={}, hubId={}", dto.getType(), dto.getHubId());
        CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(topic, key, bytes);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Ошибка отправки в Kafka (topic={}, key={})", topic, key, ex);
            } else if (result != null) {
                RecordMetadata meta = result.getRecordMetadata();
                log.info("Событие хаба отправлено в Kafka: topic={}, partition={}, offset={}", meta.topic(), meta.partition(), meta.offset());
            }
        });
    }
}
