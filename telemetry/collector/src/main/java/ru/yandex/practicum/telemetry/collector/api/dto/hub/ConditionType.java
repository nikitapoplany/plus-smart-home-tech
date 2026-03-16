package ru.yandex.practicum.telemetry.collector.api.dto.hub;

/**
 * Типы условий сценариев (соответствуют OpenAPI и Avro ConditionTypeAvro).
 */
public enum ConditionType {
    MOTION,
    LUMINOSITY,
    SWITCH,
    TEMPERATURE,
    CO2LEVEL,
    HUMIDITY
}
