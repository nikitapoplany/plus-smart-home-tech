package ru.yandex.practicum.telemetry.collector.api.dto.hub;

/**
 * Типы событий хаба (соответствуют OpenAPI).
 */
public enum HubEventType {
    DEVICE_ADDED,
    DEVICE_REMOVED,
    SCENARIO_ADDED,
    SCENARIO_REMOVED
}
