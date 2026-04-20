package ru.yandex.practicum.telemetry.serialization.avro;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

/**
 * Десериализатор Avro для {@link SensorsSnapshotAvro}.
 */
public class SensorsSnapshotDeserializer extends BaseAvroDeserializer<SensorsSnapshotAvro> {

    public SensorsSnapshotDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }

    public SensorsSnapshotDeserializer(DecoderFactory factory, Schema schema) {
        super(factory, schema);
    }
}
