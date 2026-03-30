package ru.yandex.practicum.telemetry.collector.api.dto.hub;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO события удаления сценария в хабе.
 */
public class ScenarioRemovedEventDto extends HubEventDto {

    @NotBlank
    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
