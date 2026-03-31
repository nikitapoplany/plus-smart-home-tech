package ru.yandex.practicum.telemetry.collector.service.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

/**
 * Маппинг Protobuf сообщений → Avro SpecificRecord для публикации в Kafka.
 */
@Component
public class ProtoToAvroMapper {

    // ===== Sensors =====

    public SensorEventAvro toAvro(SensorEventProto proto) {
        Instant ts = toInstant(proto.getTimestamp());
        Object payload = switch (proto.getPayloadCase()) {
            case MOTION_SENSOR -> map(proto.getMotionSensor());
            case TEMPERATURE_SENSOR -> map(proto.getTemperatureSensor(), proto, ts);
            case LIGHT_SENSOR -> map(proto.getLightSensor());
            case CLIMATE_SENSOR -> map(proto.getClimateSensor());
            case SWITCH_SENSOR -> map(proto.getSwitchSensor());
            case PAYLOAD_NOT_SET -> null;
        };
        return SensorEventAvro.newBuilder()
                .setId(proto.getId())
                .setHubId(proto.getHubId())
                .setTimestamp(ts)
                .setPayload(payload)
                .build();
    }

    private MotionSensorAvro map(MotionSensorProto p) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(p.getLinkQuality())
                .setMotion(p.getMotion())
                .setVoltage(p.getVoltage())
                .build();
    }

    private TemperatureSensorAvro map(TemperatureSensorProto p, SensorEventProto root, Instant ts) {
        // В Avro-модели TemperatureSensorAvro содержит дополнительные поля id/hubId/timestamp
        return TemperatureSensorAvro.newBuilder()
                .setId(root.getId())
                .setHubId(root.getHubId())
                .setTimestamp(ts)
                .setTemperatureC(p.getTemperatureC())
                .setTemperatureF(p.getTemperatureF())
                .build();
    }

    private LightSensorAvro map(LightSensorProto p) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(p.getLinkQuality())
                .setLuminosity(p.getLuminosity())
                .build();
    }

    private ClimateSensorAvro map(ClimateSensorProto p) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(p.getTemperatureC())
                .setHumidity(p.getHumidity())
                .setCo2Level(p.getCo2Level())
                .build();
    }

    private SwitchSensorAvro map(SwitchSensorProto p) {
        return SwitchSensorAvro.newBuilder()
                .setState(p.getState())
                .build();
    }

    // ===== Hubs =====

    public HubEventAvro toAvro(HubEventProto proto) {
        Instant ts = toInstant(proto.getTimestamp());
        Object payload = switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> map(proto.getDeviceAdded());
            case DEVICE_REMOVED -> map(proto.getDeviceRemoved());
            case SCENARIO_ADDED -> map(proto.getScenarioAdded());
            case SCENARIO_REMOVED -> map(proto.getScenarioRemoved());
            case PAYLOAD_NOT_SET -> null;
        };
        return HubEventAvro.newBuilder()
                .setHubId(proto.getHubId())
                .setTimestamp(ts)
                .setPayload(payload)
                .build();
    }

    private DeviceAddedEventAvro map(DeviceAddedEventProto p) {
        DeviceTypeAvro t = DeviceTypeAvro.valueOf(p.getType().name());
        return DeviceAddedEventAvro.newBuilder()
                .setId(p.getId())
                .setType(t)
                .build();
    }

    private DeviceRemovedEventAvro map(DeviceRemovedEventProto p) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(p.getId())
                .build();
    }

    private ScenarioAddedEventAvro map(ScenarioAddedEventProto p) {
        var conds = p.getConditionList().stream()
                .map(this::map)
                .toList();
        var acts = p.getActionList().stream()
                .map(this::map)
                .toList();
        return ScenarioAddedEventAvro.newBuilder()
                .setName(p.getName())
                .setConditions(conds)
                .setActions(acts)
                .build();
    }

    private ScenarioRemovedEventAvro map(ScenarioRemovedEventProto p) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(p.getName())
                .build();
    }

    private ScenarioConditionAvro map(ScenarioConditionProto p) {
        ConditionTypeAvro type = ConditionTypeAvro.valueOf(p.getType().name());
        ConditionOperationAvro op = ConditionOperationAvro.valueOf(p.getOperation().name());
        ScenarioConditionAvro.Builder b = ScenarioConditionAvro.newBuilder()
                .setSensorId(p.getSensorId())
                .setType(type)
                .setOperation(op);
        switch (p.getValueCase()) {
            case BOOL_VALUE -> b.setValue(p.getBoolValue());
            case INT_VALUE -> b.setValue(p.getIntValue());
            case VALUE_NOT_SET -> b.setValue(null);
        }
        return b.build();
    }

    private DeviceActionAvro map(DeviceActionProto p) {
        ActionTypeAvro at = ActionTypeAvro.valueOf(p.getType().name());
        DeviceActionAvro.Builder b = DeviceActionAvro.newBuilder()
                .setSensorId(p.getSensorId())
                .setType(at);
        if (p.hasValue()) {
            b.setValue(p.getValue());
        } else {
            b.setValue(null);
        }
        return b.build();
    }

    private Instant toInstant(com.google.protobuf.Timestamp ts) {
        long seconds = ts.getSeconds();
        int nanos = ts.getNanos();
        return Instant.ofEpochSecond(seconds, nanos);
    }
}
