package ru.yandex.practicum.telemetry.analyzer.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "conditions")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "value")
    private Integer value;

    public Condition() {}

    public Condition(String type, String operation, Integer value) {
        this.type = type;
        this.operation = operation;
        this.value = value;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
}
