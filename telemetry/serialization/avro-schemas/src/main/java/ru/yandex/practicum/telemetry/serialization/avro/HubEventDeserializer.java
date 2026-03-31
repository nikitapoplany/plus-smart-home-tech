package ru.yandex.practicum.telemetry.serialization.avro;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

/**
 * Десериализатор Avro для {@link HubEventAvro}.
 */
public class HubEventDeserializer extends BaseAvroDeserializer<HubEventAvro> {

    public HubEventDeserializer() {
        super(HubEventAvro.getClassSchema());
    }

    public HubEventDeserializer(DecoderFactory factory, Schema schema) {
        super(factory, schema);
    }
}
