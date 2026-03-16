package ru.yandex.practicum.telemetry.collector.service.serialization;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Кодировщик Avro SpecificRecord → byte[] (чистый бинарный Avro без контейнера).
 */
@Component
public class AvroEncoder {

    /**
     * Сериализует Avro SpecificRecord в массив байт.
     *
     * @param record Avro-объект
     * @return массив байт Avro
     */
    public byte[] toBytes(SpecificRecord record) {
        if (record == null) {
            return new byte[0];
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(256);
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            @SuppressWarnings("unchecked")
            SpecificDatumWriter<SpecificRecord> writer = new SpecificDatumWriter<>(record.getSchema());
            writer.write(record, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            // Оборачиваем в непроверяемое исключение для единообразной обработки на уровне сервиса
            throw new IllegalStateException("Ошибка сериализации Avro: " + e.getMessage(), e);
        }
    }
}
