package ru.yandex.practicum.telemetry.collector.api.dto.hub;

/**
 * Типы действий для сценариев (соответствуют OpenAPI и Avro ActionTypeAvro).
 */
public enum ActionType {
    ACTIVATE,
    DEACTIVATE,
    INVERSE,
    SET_VALUE
}
