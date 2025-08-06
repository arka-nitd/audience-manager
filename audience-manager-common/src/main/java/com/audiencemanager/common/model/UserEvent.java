package com.audiencemanager.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a user event that flows through the system.
 * This is the primary data structure for real-time processing.
 */
public class UserEvent {

    @NotBlank
    private final String eventId;

    @NotBlank
    private final String userId;

    @NotBlank
    private final String eventType;

    @NotNull
    private final LocalDateTime timestamp;

    private final String sessionId;

    private final Map<String, Object> properties;

    private final Map<String, Object> context;

    @JsonCreator
    public UserEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("userId") String userId,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("properties") Map<String, Object> properties,
            @JsonProperty("context") Map<String, Object> context) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.sessionId = sessionId;
        this.properties = properties;
        this.context = context;
    }

    public String getEventId() {
        return eventId;
    }

    public String getUserId() {
        return userId;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEvent userEvent = (UserEvent) o;
        return Objects.equals(eventId, userEvent.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "eventId='" + eventId + '\'' +
                ", userId='" + userId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Builder pattern for creating UserEvent instances
     */
    public static class Builder {
        private String eventId;
        private String userId;
        private String eventType;
        private LocalDateTime timestamp;
        private String sessionId;
        private Map<String, Object> properties;
        private Map<String, Object> context;

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public Builder context(Map<String, Object> context) {
            this.context = context;
            return this;
        }

        public UserEvent build() {
            return new UserEvent(eventId, userId, eventType, timestamp, sessionId, properties, context);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}