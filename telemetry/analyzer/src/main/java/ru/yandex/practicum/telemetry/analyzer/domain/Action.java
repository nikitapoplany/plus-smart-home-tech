package ru.yandex.practicum.telemetry.analyzer.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "actions")
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "value")
    private Integer value;

    public Action() {}

    public Action(String type, Integer value) {
        this.type = type;
        this.value = value;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
}
