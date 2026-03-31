package ru.yandex.practicum.telemetry.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.config.AppKafkaProperties;
import ru.yandex.practicum.telemetry.collector.service.mapper.ProtoToAvroMapper;
import ru.yandex.practicum.telemetry.collector.service.serialization.AvroEncoder;

/**
 * gRPC-сервер Collector для приёма событий от хабов и устройств.
 * Принимает Protobuf-сообщения, конвертирует в Avro и публикует в Kafka в бинарном виде.
 */
@GrpcService
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private static final Logger log = LoggerFactory.getLogger(CollectorGrpcService.class);

    private final KafkaProducer<String, byte[]> producer;
    private final ProtoToAvroMapper mapper;
    private final AvroEncoder encoder;
    private final AppKafkaProperties props;

    public CollectorGrpcService(KafkaProducer<String, byte[]> producer,
                                ProtoToAvroMapper mapper,
                                AvroEncoder encoder,
                                AppKafkaProperties props) {
        this.producer = producer;
        this.mapper = mapper;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            SensorEventAvro avro = mapper.toAvro(request);
            byte[] bytes = encoder.toBytes(avro);
            String key = request.getHubId();
            String topic = props.getTopics().getSensors();
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, bytes);
            producer.send(record, (RecordMetadata meta, Exception ex) -> {
                if (ex != null) {
                    log.error("Ошибка отправки sensor-event в Kafka (topic={}, key={})", topic, key, ex);
                } else if (meta != null) {
                    log.info("Sensor-event отправлен в Kafka: topic={}, partition={}, offset={}", meta.topic(), meta.partition(), meta.offset());
                }
            });
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка обработки gRPC collectSensorEvent", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            HubEventAvro avro = mapper.toAvro(request);
            byte[] bytes = encoder.toBytes(avro);
            String key = request.getHubId();
            String topic = props.getTopics().getHubs();
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, bytes);
            producer.send(record, (RecordMetadata meta, Exception ex) -> {
                if (ex != null) {
                    log.error("Ошибка отправки hub-event в Kafka (topic={}, key={})", topic, key, ex);
                } else if (meta != null) {
                    log.info("Hub-event отправлен в Kafka: topic={}, partition={}, offset={}", meta.topic(), meta.partition(), meta.offset());
                }
            });
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка обработки gRPC collectHubEvent", e);
            responseObserver.onError(e);
        }
    }
}
