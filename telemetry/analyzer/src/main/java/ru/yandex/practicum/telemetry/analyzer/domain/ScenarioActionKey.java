package ru.yandex.practicum.telemetry.analyzer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ScenarioActionKey implements Serializable {

    @Column(name = "scenario_id")
    private Long scenarioId;

    @Column(name = "sensor_id")
    private String sensorId;

    @Column(name = "action_id")
    private Long actionId;

    public ScenarioActionKey() {}

    public ScenarioActionKey(Long scenarioId, String sensorId, Long actionId) {
        this.scenarioId = scenarioId;
        this.sensorId = sensorId;
        this.actionId = actionId;
    }

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioActionKey that = (ScenarioActionKey) o;
        return Objects.equals(scenarioId, that.scenarioId) && Objects.equals(sensorId, that.sensorId) && Objects.equals(actionId, that.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId, sensorId, actionId);
    }
}
