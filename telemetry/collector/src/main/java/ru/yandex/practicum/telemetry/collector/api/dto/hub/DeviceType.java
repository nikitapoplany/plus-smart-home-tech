package ru.yandex.practicum.telemetry.collector.api.dto.hub;

/**
 * Типы устройств (соответствуют OpenAPI и Avro DeviceTypeAvro).
 */
public enum DeviceType {
    MOTION_SENSOR,
    TEMPERATURE_SENSOR,
    LIGHT_SENSOR,
    CLIMATE_SENSOR,
    SWITCH_SENSOR
}
