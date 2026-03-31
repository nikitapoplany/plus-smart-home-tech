package ru.yandex.practicum.telemetry.analyzer.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "scenarios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_scenarios_hub_name", columnNames = {"hub_id", "name"})
})
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    @Column(name = "name", nullable = false)
    private String name;

    public Scenario() {}

    public Scenario(String hubId, String name) {
        this.hubId = hubId;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHubId() { return hubId; }
    public void setHubId(String hubId) { this.hubId = hubId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
