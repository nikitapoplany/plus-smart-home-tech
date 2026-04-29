package ru.yandex.practicum.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.telemetry.analyzer.domain.ScenarioActionKey;
import ru.yandex.practicum.telemetry.analyzer.domain.ScenarioActionLink;

import java.util.List;

public interface ScenarioActionLinkRepository extends JpaRepository<ScenarioActionLink, ScenarioActionKey> {

    interface ActionView {
        String getSensorId();
        String getType();
        Integer getValue();
    }

    @Modifying
    @Transactional
    @Query("delete from ScenarioActionLink l where l.id.scenarioId = :scenarioId")
    void deleteByScenarioId(@Param("scenarioId") Long scenarioId);

    @Query("select l from ScenarioActionLink l where l.id.scenarioId = :scenarioId")
    List<ScenarioActionLink> findAllByScenarioId(@Param("scenarioId") Long scenarioId);

    @Query(value = "select sa.sensor_id as sensorId, a.type as type, a.value as value " +
            "from scenario_actions sa join actions a on a.id = sa.action_id " +
            "where sa.scenario_id = :scenarioId", nativeQuery = true)
    List<ActionView> findActionsByScenarioId(@Param("scenarioId") Long scenarioId);
}
