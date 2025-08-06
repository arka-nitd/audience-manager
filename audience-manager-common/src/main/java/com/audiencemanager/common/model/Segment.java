package com.audiencemanager.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an audience segment with rules and metadata.
 * This is a shared model used across all modules.
 */
public class Segment {

    @NotBlank
    private final String id;

    @NotBlank
    private final String name;

    private final String description;

    @NotNull
    private final SegmentType type;

    @NotNull
    private final Map<String, Object> rules;

    private final boolean active;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    @JsonCreator
    public Segment(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("type") SegmentType type,
            @JsonProperty("rules") Map<String, Object> rules,
            @JsonProperty("active") boolean active,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.rules = rules;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SegmentType getType() {
        return type;
    }

    public Map<String, Object> getRules() {
        return rules;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Segment segment = (Segment) o;
        return Objects.equals(id, segment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Segment{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", active=" + active +
                '}';
    }

    /**
     * Builder pattern for creating Segment instances
     */
    public static class Builder {
        private String id;
        private String name;
        private String description;
        private SegmentType type;
        private Map<String, Object> rules;
        private boolean active = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(SegmentType type) {
            this.type = type;
            return this;
        }

        public Builder rules(Map<String, Object> rules) {
            this.rules = rules;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Segment build() {
            return new Segment(id, name, description, type, rules, active, createdAt, updatedAt);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}