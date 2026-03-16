package ru.yandex.practicum.telemetry.collector.api.dto.hub;

/**
 * Операторы для условий сценариев (соответствуют OpenAPI и Avro ConditionOperationAvro).
 */
public enum ConditionOperation {
    EQUALS,
    GREATER_THAN,
    LOWER_THAN
}
