package ru.yandex.practicum.telemetry.collector.api.dto.hub;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO события добавления сценария в хаб.
 */
public class ScenarioAddedEventDto extends HubEventDto {

    @NotBlank
    @Size(min = 3)
    private String name;

    @Valid
    @NotNull
    @NotEmpty
    private List<ScenarioConditionDto> conditions;

    @Valid
    @NotNull
    @NotEmpty
    private List<DeviceActionDto> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScenarioConditionDto> getConditions() {
        return conditions;
    }

    public void setConditions(List<ScenarioConditionDto> conditions) {
        this.conditions = conditions;
    }

    public List<DeviceActionDto> getActions() {
        return actions;
    }

    public void setActions(List<DeviceActionDto> actions) {
        this.actions = actions;
    }
}
