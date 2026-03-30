package ru.yandex.practicum.telemetry.collector.api.dto.sensor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO события переключателя.
 */
public class SwitchSensorEventDto extends SensorEventDto {

    @NotNull
    private Boolean state;

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }
}
