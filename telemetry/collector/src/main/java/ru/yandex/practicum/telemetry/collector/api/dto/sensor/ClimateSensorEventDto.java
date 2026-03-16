package ru.yandex.practicum.telemetry.collector.api.dto.sensor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO события климатического датчика.
 */
public class ClimateSensorEventDto extends SensorEventDto {

    @NotNull
    private Integer temperatureC;

    @NotNull
    private Integer humidity;

    @NotNull
    private Integer co2Level;

    @Override
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }

    public Integer getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(Integer temperatureC) {
        this.temperatureC = temperatureC;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public Integer getCo2Level() {
        return co2Level;
    }

    public void setCo2Level(Integer co2Level) {
        this.co2Level = co2Level;
    }
}
