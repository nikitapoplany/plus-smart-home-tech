package ru.yandex.practicum.telemetry.collector.api.dto.sensor;

/**
 * Типы событий датчиков (соответствуют OpenAPI).
 */
public enum SensorEventType {
    MOTION_SENSOR_EVENT,
    TEMPERATURE_SENSOR_EVENT,
    LIGHT_SENSOR_EVENT,
    CLIMATE_SENSOR_EVENT,
    SWITCH_SENSOR_EVENT
}
