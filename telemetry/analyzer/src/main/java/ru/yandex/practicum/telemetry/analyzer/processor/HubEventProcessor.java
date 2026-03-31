package ru.yandex.practicum.telemetry.analyzer.processor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.config.AppKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.domain.*;
import ru.yandex.practicum.telemetry.analyzer.repository.*;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Обработчик событий хаба: добавление/удаление устройств и сценариев.
 * Запускается в отдельном потоке.
 */
@Component
public class HubEventProcessor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(HubEventProcessor.class);

    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final AppKafkaProperties props;

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionLinkRepository scLinkRepository;
    private final ScenarioActionLinkRepository saLinkRepository;

    public HubEventProcessor(KafkaConsumer<String, HubEventAvro> hubsConsumer,
                             AppKafkaProperties props,
                             ScenarioRepository scenarioRepository,
                             SensorRepository sensorRepository,
                             ConditionRepository conditionRepository,
                             ActionRepository actionRepository,
                             ScenarioConditionLinkRepository scLinkRepository,
                             ScenarioActionLinkRepository saLinkRepository) {
        this.consumer = hubsConsumer;
        this.props = props;
        this.scenarioRepository = scenarioRepository;
        this.sensorRepository = sensorRepository;
        this.conditionRepository = conditionRepository;
        this.actionRepository = actionRepository;
        this.scLinkRepository = scLinkRepository;
        this.saLinkRepository = saLinkRepository;
    }

    @Override
    public void run() {
        try {
            log.info("[HubEventProcessor] Подписываемся на топик событий хабов: {}", props.getTopics().getHubs());
            consumer.subscribe(Collections.singletonList(props.getTopics().getHubs()));

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofSeconds(1));
                if (records.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, HubEventAvro> rec : records) {
                    HubEventAvro event = rec.value();
                    if (event == null) continue;
                    processEvent(event);
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            // graceful shutdown
        } catch (Exception e) {
            log.error("[HubEventProcessor] Ошибка обработки сообщений", e);
        } finally {
            try {
                consumer.commitSync();
            } catch (Exception e) {
                log.warn("[HubEventProcessor] Ошибка commitSync при завершении: {}", e.getMessage());
            }
            try { consumer.close(); } catch (Exception ignore) {}
        }
    }

    private void processEvent(HubEventAvro event) {
        String hubId = event.getHubId();
        Object payload = event.getPayload();
        if (payload instanceof DeviceAddedEventAvro da) {
            handleDeviceAdded(hubId, da);
        } else if (payload instanceof DeviceRemovedEventAvro dr) {
            handleDeviceRemoved(hubId, dr);
        } else if (payload instanceof ScenarioAddedEventAvro sa) {
            handleScenarioAdded(hubId, sa);
        } else if (payload instanceof ScenarioRemovedEventAvro sr) {
            handleScenarioRemoved(hubId, sr);
        } else {
            log.warn("[HubEventProcessor] Неизвестный тип события payload: {}", payload == null ? null : payload.getClass());
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro da) {
        String sensorId = da.getId();
        if (sensorRepository.findByIdAndHubId(sensorId, hubId).isEmpty()) {
            sensorRepository.save(new Sensor(sensorId, hubId));
            log.info("[HubEventProcessor] Добавлено устройство: sensorId={}, hubId={}", sensorId, hubId);
        } else {
            log.debug("[HubEventProcessor] Устройство уже существует: sensorId={}, hubId={}", sensorId, hubId);
        }
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro dr) {
        String sensorId = dr.getId();
        sensorRepository.findByIdAndHubId(sensorId, hubId).ifPresent(s -> {
            sensorRepository.delete(s);
            log.info("[HubEventProcessor] Удалено устройство: sensorId={}, hubId={}", sensorId, hubId);
        });
    }

    @Transactional
    protected void handleScenarioAdded(String hubId, ScenarioAddedEventAvro sa) {
        String name = sa.getName();
        Scenario scenario = upsertScenario(hubId, name);
        Long scenarioId = scenario.getId();

        // Заменяем связи: сначала очистим линки, затем создадим новые
        scLinkRepository.deleteByScenarioId(scenarioId);
        saLinkRepository.deleteByScenarioId(scenarioId);

        // Создаём условия и линки
        List<ScenarioConditionAvro> conditions = sa.getConditions();
        for (ScenarioConditionAvro c : conditions) {
            Integer val = null;
            Object v = c.getValue();
            if (v instanceof Integer i) val = i;
            else if (v instanceof Boolean b) val = b ? 1 : 0;
            Condition cond = conditionRepository.save(new Condition(c.getType().name(), c.getOperation().name(), val));
            ScenarioConditionKey key = new ScenarioConditionKey(scenarioId, c.getSensorId(), cond.getId());
            scLinkRepository.save(new ScenarioConditionLink(key));
        }

        // Создаём действия и линки
        List<DeviceActionAvro> actions = sa.getActions();
        for (DeviceActionAvro a : actions) {
            Integer val = (a.getValue() == null) ? null : (Integer) a.getValue();
            Action act = actionRepository.save(new Action(a.getType().name(), val));
            ScenarioActionKey key = new ScenarioActionKey(scenarioId, a.getSensorId(), act.getId());
            saLinkRepository.save(new ScenarioActionLink(key));
        }
        log.info("[HubEventProcessor] Обновлён сценарий: hubId={}, name={}, conditions={}, actions={}",
                hubId, name, conditions.size(), actions.size());
    }

    private Scenario upsertScenario(String hubId, String name) {
        Optional<Scenario> found = scenarioRepository.findByHubIdAndName(hubId, name);
        if (found.isPresent()) {
            return found.get();
        }
        return scenarioRepository.save(new Scenario(hubId, name));
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro sr) {
        String name = sr.getName();
        scenarioRepository.findByHubIdAndName(hubId, name).ifPresent(sc -> {
            scenarioRepository.delete(sc);
            log.info("[HubEventProcessor] Удалён сценарий: hubId={}, name={}", hubId, name);
        });
    }
}
