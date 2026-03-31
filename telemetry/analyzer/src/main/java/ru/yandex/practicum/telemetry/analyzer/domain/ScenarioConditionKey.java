package ru.yandex.practicum.telemetry.analyzer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ScenarioConditionKey implements Serializable {

    @Column(name = "scenario_id")
    private Long scenarioId;

    @Column(name = "sensor_id")
    private String sensorId;

    @Column(name = "condition_id")
    private Long conditionId;

    public ScenarioConditionKey() {}

    public ScenarioConditionKey(Long scenarioId, String sensorId, Long conditionId) {
        this.scenarioId = scenarioId;
        this.sensorId = sensorId;
        this.conditionId = conditionId;
    }

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
    public Long getConditionId() { return conditionId; }
    public void setConditionId(Long conditionId) { this.conditionId = conditionId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioConditionKey that = (ScenarioConditionKey) o;
        return Objects.equals(scenarioId, that.scenarioId) && Objects.equals(sensorId, that.sensorId) && Objects.equals(conditionId, that.conditionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId, sensorId, conditionId);
    }
}
