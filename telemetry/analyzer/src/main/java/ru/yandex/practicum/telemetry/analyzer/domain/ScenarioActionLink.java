package ru.yandex.practicum.telemetry.analyzer.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "scenario_actions")
public class ScenarioActionLink {

    @EmbeddedId
    private ScenarioActionKey id;

    public ScenarioActionLink() {}

    public ScenarioActionLink(ScenarioActionKey id) {
        this.id = id;
    }

    public ScenarioActionKey getId() { return id; }
    public void setId(ScenarioActionKey id) { this.id = id; }
}
