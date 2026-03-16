package ru.yandex.practicum.telemetry.collector.service.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.api.dto.hub.*;
import ru.yandex.practicum.telemetry.collector.api.dto.sensor.*;

import java.time.Instant;

/**
 * Маппинг HTTP DTO → Avro SpecificRecord.
 */
@Component
public class DtoToAvroMapper {

    // ===== Sensors =====

    public SensorEventAvro toAvro(SensorEventDto dto) {
        Instant ts = resolveInstant(dto.getTimestamp());
        Object payload = switch (dto.getType()) {
            case CLIMATE_SENSOR_EVENT -> map((ClimateSensorEventDto) dto);
            case LIGHT_SENSOR_EVENT -> map((LightSensorEventDto) dto);
            case MOTION_SENSOR_EVENT -> map((MotionSensorEventDto) dto);
            case SWITCH_SENSOR_EVENT -> map((SwitchSensorEventDto) dto);
            case TEMPERATURE_SENSOR_EVENT -> map((TemperatureSensorEventDto) dto);
        };
        return SensorEventAvro.newBuilder()
                .setId(dto.getId())
                .setHubId(dto.getHubId())
                .setTimestamp(ts)
                .setPayload(payload)
                .build();
    }

    private ClimateSensorAvro map(ClimateSensorEventDto dto) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(dto.getTemperatureC())
                .setHumidity(dto.getHumidity())
                .setCo2Level(dto.getCo2Level())
                .build();
    }

    private LightSensorAvro map(LightSensorEventDto dto) {
        int linkQuality = dto.getLinkQuality() != null ? dto.getLinkQuality() : 0;
        int luminosity = dto.getLuminosity() != null ? dto.getLuminosity() : 0;
        return LightSensorAvro.newBuilder()
                .setLinkQuality(linkQuality)
                .setLuminosity(luminosity)
                .build();
    }

    private MotionSensorAvro map(MotionSensorEventDto dto) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(dto.getLinkQuality())
                .setMotion(dto.getMotion())
                .setVoltage(dto.getVoltage())
                .build();
    }

    private SwitchSensorAvro map(SwitchSensorEventDto dto) {
        return SwitchSensorAvro.newBuilder()
                .setState(dto.getState())
                .build();
    }

    private TemperatureSensorAvro map(TemperatureSensorEventDto dto) {
        Instant ts = resolveInstant(dto.getTimestamp());
        return TemperatureSensorAvro.newBuilder()
                .setId(dto.getId())
                .setHubId(dto.getHubId())
                .setTimestamp(ts)
                .setTemperatureC(dto.getTemperatureC())
                .setTemperatureF(dto.getTemperatureF())
                .build();
    }

    // ===== Hubs =====

    public HubEventAvro toAvro(HubEventDto dto) {
        Instant ts = resolveInstant(dto.getTimestamp());
        Object payload = switch (dto.getType()) {
            case DEVICE_ADDED -> map((DeviceAddedEventDto) dto);
            case DEVICE_REMOVED -> map((DeviceRemovedEventDto) dto);
            case SCENARIO_ADDED -> map((ScenarioAddedEventDto) dto);
            case SCENARIO_REMOVED -> map((ScenarioRemovedEventDto) dto);
        };
        return HubEventAvro.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(ts)
                .setPayload(payload)
                .build();
    }

    private DeviceAddedEventAvro map(DeviceAddedEventDto dto) {
        DeviceTypeAvro t = DeviceTypeAvro.valueOf(dto.getDeviceType().name());
        return DeviceAddedEventAvro.newBuilder()
                .setId(dto.getId())
                .setType(t)
                .build();
    }

    private DeviceRemovedEventAvro map(DeviceRemovedEventDto dto) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(dto.getId())
                .build();
    }

    private ScenarioAddedEventAvro map(ScenarioAddedEventDto dto) {
        var conds = dto.getConditions().stream()
                .map(this::map)
                .toList();
        var acts = dto.getActions().stream()
                .map(this::map)
                .toList();
        return ScenarioAddedEventAvro.newBuilder()
                .setName(dto.getName())
                .setConditions(conds)
                .setActions(acts)
                .build();
    }

    private ScenarioRemovedEventAvro map(ScenarioRemovedEventDto dto) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(dto.getName())
                .build();
    }

    private ScenarioConditionAvro map(ScenarioConditionDto dto) {
        ConditionTypeAvro type = ConditionTypeAvro.valueOf(dto.getType().name());
        ConditionOperationAvro op = ConditionOperationAvro.valueOf(dto.getOperation().name());
        ScenarioConditionAvro.Builder b = ScenarioConditionAvro.newBuilder()
                .setSensorId(dto.getSensorId())
                .setType(type)
                .setOperation(op);
        Object v = dto.getValue();
        if (v == null) {
            b.setValue(null);
        } else if (v instanceof Integer i) {
            b.setValue(i);
        } else if (v instanceof Boolean bl) {
            b.setValue(bl);
        } else {
            // Некорректный тип — игнорируем и оставляем null
            b.setValue(null);
        }
        return b.build();
    }

    private DeviceActionAvro map(DeviceActionDto dto) {
        ActionTypeAvro at = ActionTypeAvro.valueOf(dto.getType().name());
        DeviceActionAvro.Builder b = DeviceActionAvro.newBuilder()
                .setSensorId(dto.getSensorId())
                .setType(at);
        if (dto.getValue() == null) {
            b.setValue(null);
        } else {
            b.setValue(dto.getValue());
        }
        return b.build();
    }

    private Instant resolveInstant(Instant ts) {
        return (ts != null ? ts : Instant.now());
    }
}
