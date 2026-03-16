package ru.yandex.practicum.telemetry.collector.api.dto.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO условия активации сценария.
 * Поле value может быть не задано или иметь числовое/логическое значение.
 */
public class ScenarioConditionDto {

    @NotBlank
    private String sensorId;

    @NotNull
    private ConditionType type;

    @NotNull
    private ConditionOperation operation;

    /**
     * Может быть null, Integer или Boolean согласно ТЗ.
     * В OpenAPI поле может быть ограничено типом int, но поддержим оба варианта для совместимости.
     */
    private Object value;

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public ConditionType getType() {
        return type;
    }

    public void setType(ConditionType type) {
        this.type = type;
    }

    public ConditionOperation getOperation() {
        return operation;
    }

    public void setOperation(ConditionOperation operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
