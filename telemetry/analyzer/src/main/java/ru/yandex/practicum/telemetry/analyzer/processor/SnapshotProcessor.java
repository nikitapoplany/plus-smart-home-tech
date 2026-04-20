package ru.yandex.practicum.telemetry.analyzer.processor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.config.AppKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.integration.HubRouterClient;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioActionLinkRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioConditionLinkRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.time.Duration;
import java.util.*;

/**
 * Обработчик снимков состояния: читает снапшоты, загружает сценарии по hubId,
 * проверяет условия и исполняет действия через gRPC Hub Router.
 */
@Component
public class SnapshotProcessor {

    private static final Logger log = LoggerFactory.getLogger(SnapshotProcessor.class);

    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final AppKafkaProperties props;

    private final ScenarioRepository scenarioRepository;
    private final ScenarioConditionLinkRepository scLinkRepository;
    private final ScenarioActionLinkRepository saLinkRepository;

    private final HubRouterClient hubRouterClient;

    public SnapshotProcessor(KafkaConsumer<String, SensorsSnapshotAvro> snapshotsConsumer,
                             AppKafkaProperties props,
                             ScenarioRepository scenarioRepository,
                             ScenarioConditionLinkRepository scLinkRepository,
                             ScenarioActionLinkRepository saLinkRepository,
                             HubRouterClient hubRouterClient) {
        this.consumer = snapshotsConsumer;
        this.props = props;
        this.scenarioRepository = scenarioRepository;
        this.scLinkRepository = scLinkRepository;
        this.saLinkRepository = saLinkRepository;
        this.hubRouterClient = hubRouterClient;
    }

    public void start() {
        try {
            log.info("[SnapshotProcessor] Подписываемся на топик снапшотов: {}", props.getTopics().getSnapshots());
            consumer.subscribe(Collections.singletonList(props.getTopics().getSnapshots()));
            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofSeconds(1));
                if (records.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, SensorsSnapshotAvro> rec : records) {
                    SensorsSnapshotAvro snapshot = rec.value();
                    if (snapshot == null) continue;
                    processSnapshot(snapshot);
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            // graceful shutdown
        } catch (Exception e) {
            log.error("[SnapshotProcessor] Ошибка обработки снапшотов", e);
        } finally {
            try {
                consumer.commitSync();
            } catch (Exception e) {
                log.warn("[SnapshotProcessor] Ошибка commitSync при завершении: {}", e.getMessage());
            }
            try { consumer.close(); } catch (Exception ignore) {}
        }
    }

    private void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        Map<String, SensorStateAvro> map = snapshot.getSensorsState();
        final Map<String, SensorStateAvro> states = (map != null) ? map : Collections.emptyMap();

        var scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.debug("[SnapshotProcessor] Нет сценариев для hubId={}, пропускаем", hubId);
            return;
        }

        for (var scenario : scenarios) {
            Long scenarioId = scenario.getId();
            // Загружаем условия и действия
            var conds = scLinkRepository.findConditionsByScenarioId(scenarioId);
            boolean ok = conds.stream().allMatch(c -> evaluateCondition(states.get(c.getSensorId()), c.getType(), c.getOperation(), c.getValue()));
            if (!ok) {
                continue;
            }
            var actions = saLinkRepository.findActionsByScenarioId(scenarioId);
            for (var a : actions) {
                hubRouterClient.sendAction(hubId, scenario.getName(), a.getSensorId(), a.getType(), a.getValue());
            }
            log.info("[SnapshotProcessor] Сценарий сработал: hubId={}, name={}, actions={}", hubId, scenario.getName(), actions.size());
        }
    }

    private boolean evaluateCondition(SensorStateAvro state, String type, String operation, Integer value) {
        if (state == null) return false;
        Object data = state.getData();
        // Нормализуем ожидаемое значение (null -> 0 для чисел/ложь для булевых)
        int expected = (value == null) ? 0 : value;

        return switch (type) {
            case "MOTION" -> compareBoolean(extractMotion(data), operation, expected);
            case "LUMINOSITY" -> compareNumber(extractLuminosity(data), operation, expected);
            case "SWITCH" -> compareBoolean(extractSwitch(data), operation, expected);
            case "TEMPERATURE" -> compareNumber(extractTemperatureC(data), operation, expected);
            case "CO2LEVEL" -> compareNumber(extractCo2(data), operation, expected);
            case "HUMIDITY" -> compareNumber(extractHumidity(data), operation, expected);
            default -> false;
        };
    }

    private boolean compareBoolean(boolean actual, String op, int expected) {
        int act = actual ? 1 : 0;
        return switch (op) {
            case "EQUALS" -> act == expected;
            case "GREATER_THAN" -> act > expected;
            case "LOWER_THAN" -> act < expected;
            default -> false;
        };
    }

    private boolean compareNumber(int actual, String op, int expected) {
        return switch (op) {
            case "EQUALS" -> actual == expected;
            case "GREATER_THAN" -> actual > expected;
            case "LOWER_THAN" -> actual < expected;
            default -> false;
        };
    }

    private boolean extractMotion(Object data) {
        if (data instanceof MotionSensorAvro m) return m.getMotion();
        return false;
    }

    private int extractLuminosity(Object data) {
        if (data instanceof LightSensorAvro l) return l.getLuminosity();
        return 0;
    }

    private boolean extractSwitch(Object data) {
        if (data instanceof SwitchSensorAvro s) return s.getState();
        return false;
    }

    private int extractTemperatureC(Object data) {
        if (data instanceof ClimateSensorAvro c) return c.getTemperatureC();
        if (data instanceof TemperatureSensorAvro t) return t.getTemperatureC();
        return 0;
    }

    private int extractCo2(Object data) {
        if (data instanceof ClimateSensorAvro c) return c.getCo2Level();
        return 0;
    }

    private int extractHumidity(Object data) {
        if (data instanceof ClimateSensorAvro c) return c.getHumidity();
        return 0;
    }
}
