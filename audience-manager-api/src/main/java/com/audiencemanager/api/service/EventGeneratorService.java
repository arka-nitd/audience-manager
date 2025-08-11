package com.audiencemanager.api.service;

import com.audiencemanager.api.dto.GenerateEventsRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for generating and sending test events to Kafka
 */
@Service
public class EventGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(EventGeneratorService.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String[] USER_IDS = {
        "user_001", "user_002", "user_003", "user_004", "user_005",
        "user_006", "user_007", "user_008", "user_009", "user_010"
    };

    private static final String[] SESSION_IDS = {
        "session_001", "session_002", "session_003", "session_004", "session_005"
    };

    public Map<String, Object> generateEvents(GenerateEventsRequest request) {
        try {
            List<Map<String, Object>> generatedEvents = new ArrayList<>();
            
            if (request.getEvents() != null && !request.getEvents().isEmpty()) {
                // Use provided events
                for (GenerateEventsRequest.EventDto eventDto : request.getEvents()) {
                    Map<String, Object> event = createEventFromDto(eventDto);
                    sendEventToKafka(request.getTopic(), event);
                    generatedEvents.add(event);
                }
            } else {
                // Generate random events
                for (int i = 0; i < request.getEventCount(); i++) {
                    Map<String, Object> event = generateRandomEvent(request.getTopic());
                    sendEventToKafka(request.getTopic(), event);
                    generatedEvents.add(event);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("topic", request.getTopic());
            response.put("eventCount", generatedEvents.size());
            response.put("events", generatedEvents);
            response.put("timestamp", Instant.now().toEpochMilli());

            logger.info("Generated {} events for topic: {}", generatedEvents.size(), request.getTopic());
            return response;

        } catch (Exception e) {
            logger.error("Error generating events: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to generate events: " + e.getMessage());
            errorResponse.put("timestamp", Instant.now().toEpochMilli());
            return errorResponse;
        }
    }

    private Map<String, Object> createEventFromDto(GenerateEventsRequest.EventDto eventDto) {
        Map<String, Object> event = new HashMap<>();
        event.put("userId", eventDto.getUserId());
        event.put("eventType", eventDto.getEventType());
        event.put("timestamp", eventDto.getTimestamp() != null ? eventDto.getTimestamp() : Instant.now().toEpochMilli());
        event.put("value", eventDto.getValue());
        event.put("sessionId", eventDto.getSessionId());
        if (eventDto.getMetadata() != null) {
            event.put("metadata", eventDto.getMetadata());
        }
        return event;
    }

    private Map<String, Object> generateRandomEvent(String topic) {
        Map<String, Object> event = new HashMap<>();
        
        // Basic event properties
        event.put("userId", USER_IDS[ThreadLocalRandom.current().nextInt(USER_IDS.length)]);
        event.put("timestamp", Instant.now().toEpochMilli());
        event.put("sessionId", SESSION_IDS[ThreadLocalRandom.current().nextInt(SESSION_IDS.length)]);
        
        // Topic-specific event generation
        switch (topic) {
            case "order_events":
                event.put("eventType", "orders");
                event.put("value", ThreadLocalRandom.current().nextDouble(10.0, 500.0));
                Map<String, Object> orderMetadata = new HashMap<>();
                orderMetadata.put("orderId", "order_" + System.currentTimeMillis());
                orderMetadata.put("productCount", ThreadLocalRandom.current().nextInt(1, 10));
                orderMetadata.put("currency", "USD");
                event.put("metadata", orderMetadata);
                break;
                
            case "activity_events":
                String[] activities = {"click", "view", "scroll", "hover"};
                event.put("eventType", activities[ThreadLocalRandom.current().nextInt(activities.length)]);
                event.put("value", ThreadLocalRandom.current().nextDouble(1.0, 10.0));
                Map<String, Object> activityMetadata = new HashMap<>();
                activityMetadata.put("page", "/product/" + ThreadLocalRandom.current().nextInt(1, 100));
                activityMetadata.put("element", "button_" + ThreadLocalRandom.current().nextInt(1, 20));
                event.put("metadata", activityMetadata);
                break;
                
            case "install_events":
                event.put("eventType", "app_install");
                event.put("value", 1.0);
                Map<String, Object> installMetadata = new HashMap<>();
                installMetadata.put("appVersion", "1.0." + ThreadLocalRandom.current().nextInt(1, 50));
                installMetadata.put("platform", ThreadLocalRandom.current().nextBoolean() ? "iOS" : "Android");
                installMetadata.put("source", "organic");
                event.put("metadata", installMetadata);
                break;
                
            default:
                event.put("eventType", "generic");
                event.put("value", ThreadLocalRandom.current().nextDouble(1.0, 100.0));
        }
        
        return event;
    }

    private void sendEventToKafka(String topic, Map<String, Object> event) throws JsonProcessingException {
        String eventJson = objectMapper.writeValueAsString(event);
        String userId = (String) event.get("userId");
        
        kafkaTemplate.send(topic, userId, eventJson);
        logger.debug("Sent event to topic {}: {}", topic, eventJson);
    }
}