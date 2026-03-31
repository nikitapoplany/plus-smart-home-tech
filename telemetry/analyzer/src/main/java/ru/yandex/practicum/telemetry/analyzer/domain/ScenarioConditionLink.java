package ru.yandex.practicum.telemetry.analyzer.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "scenario_conditions")
public class ScenarioConditionLink {

    @EmbeddedId
    private ScenarioConditionKey id;

    public ScenarioConditionLink() {}

    public ScenarioConditionLink(ScenarioConditionKey id) {
        this.id = id;
    }

    public ScenarioConditionKey getId() { return id; }
    public void setId(ScenarioConditionKey id) { this.id = id; }
}
