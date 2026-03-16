package ru.yandex.practicum.telemetry.collector.api.dto.hub;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO события удаления устройства из хаба.
 */
public class DeviceRemovedEventDto extends HubEventDto {

    @NotBlank
    private String id;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
