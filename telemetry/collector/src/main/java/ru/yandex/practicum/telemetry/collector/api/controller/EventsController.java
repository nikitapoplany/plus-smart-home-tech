package ru.yandex.practicum.telemetry.collector.api.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.collector.api.dto.hub.HubEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.sensor.SensorEventDto;
import ru.yandex.practicum.telemetry.collector.service.EventPublisherService;

/**
 * Контроллер событий согласно OpenAPI: POST /events/sensors и POST /events/hubs.
 */
@RestController
@RequestMapping("/events")
public class EventsController {

    private static final Logger log = LoggerFactory.getLogger(EventsController.class);

    private final EventPublisherService publisher;

    public EventsController(EventPublisherService publisher) {
        this.publisher = publisher;
    }

    /**
     * Приём событий датчиков.
     */
    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEventDto request) {
        publisher.publishSensorEvent(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Приём событий хаба.
     */
    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEventDto request) {
        publisher.publishHubEvent(request);
        return ResponseEntity.ok().build();
    }
}
