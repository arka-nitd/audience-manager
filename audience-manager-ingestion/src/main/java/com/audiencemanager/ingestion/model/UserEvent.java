package com.audiencemanager.ingestion.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * User event model for streaming processing
 */
public class UserEvent {
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("eventType")
    private String eventType; // purchase, view, install
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("value")
    private Double value; // order amount, view duration, etc.
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    public UserEvent() {}
    
    public UserEvent(String userId, String eventType, Long timestamp, Double value, String sessionId, Map<String, Object> metadata) {
        this.userId = userId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.value = value;
        this.sessionId = sessionId;
        this.metadata = metadata;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Double getValue() {
        return value;
    }
    
    public void setValue(Double value) {
        this.value = value;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEvent userEvent = (UserEvent) o;
        return Objects.equals(userId, userEvent.userId) &&
               Objects.equals(eventType, userEvent.eventType) &&
               Objects.equals(timestamp, userEvent.timestamp) &&
               Objects.equals(value, userEvent.value) &&
               Objects.equals(sessionId, userEvent.sessionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, eventType, timestamp, value, sessionId);
    }
    
    @Override
    public String toString() {
        return "UserEvent{" +
                "userId='" + userId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", value=" + value +
                ", sessionId='" + sessionId + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}