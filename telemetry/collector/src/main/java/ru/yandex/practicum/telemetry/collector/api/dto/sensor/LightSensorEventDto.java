package ru.yandex.practicum.telemetry.collector.api.dto.sensor;

/**
 * DTO события датчика света.
 */
public class LightSensorEventDto extends SensorEventDto {

    /** Качество связи (опционально по OpenAPI). */
    private Integer linkQuality;

    /** Уровень освещённости (опционально по OpenAPI). */
    private Integer luminosity;

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    public Integer getLinkQuality() {
        return linkQuality;
    }

    public void setLinkQuality(Integer linkQuality) {
        this.linkQuality = linkQuality;
    }

    public Integer getLuminosity() {
        return luminosity;
    }

    public void setLuminosity(Integer luminosity) {
        this.luminosity = luminosity;
    }
}
