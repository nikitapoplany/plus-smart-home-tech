package ru.yandex.practicum.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.telemetry.analyzer.domain.ScenarioConditionKey;
import ru.yandex.practicum.telemetry.analyzer.domain.ScenarioConditionLink;

import java.util.List;

public interface ScenarioConditionLinkRepository extends JpaRepository<ScenarioConditionLink, ScenarioConditionKey> {

    interface ConditionView {
        String getSensorId();
        String getType();
        String getOperation();
        Integer getValue();
    }

    @Modifying
    @Transactional
    @Query("delete from ScenarioConditionLink l where l.id.scenarioId = :scenarioId")
    void deleteByScenarioId(@Param("scenarioId") Long scenarioId);

    @Query("select l from ScenarioConditionLink l where l.id.scenarioId = :scenarioId")
    List<ScenarioConditionLink> findAllByScenarioId(@Param("scenarioId") Long scenarioId);

    @Query(value = "select sc.sensor_id as sensorId, c.type as type, c.operation as operation, c.value as value " +
            "from scenario_conditions sc join conditions c on c.id = sc.condition_id " +
            "where sc.scenario_id = :scenarioId", nativeQuery = true)
    List<ConditionView> findConditionsByScenarioId(@Param("scenarioId") Long scenarioId);
}
