package ru.yandex.practicum.telemetry.serialization.avro;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

/**
 * Десериализатор Avro для событий датчиков.
 */
public class SensorEventDeserializer extends BaseAvroDeserializer<SensorEventAvro> {
    public SensorEventDeserializer() {
        super(SensorEventAvro.getClassSchema());
    }
}
