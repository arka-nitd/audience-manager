package com.audiencemanager.api.controller;

import com.audiencemanager.api.dto.GenerateEventsRequest;
import com.audiencemanager.api.service.EventGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * REST controller for event generation functionality
 */
@RestController
@RequestMapping("/api/v1/events")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:30280"})
public class EventGeneratorController {

    @Autowired
    private EventGeneratorService eventGeneratorService;

    /**
     * Generate and send events to Kafka
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateEvents(@Valid @RequestBody GenerateEventsRequest request) {
        Map<String, Object> result = eventGeneratorService.generateEvents(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Get available event topics
     */
    @GetMapping("/topics")
    public ResponseEntity<String[]> getEventTopics() {
        String[] topics = {"order_events", "activity_events", "install_events"};
        return ResponseEntity.ok(topics);
    }
}