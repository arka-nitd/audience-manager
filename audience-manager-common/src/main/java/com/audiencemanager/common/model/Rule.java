package com.audiencemanager.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a rule for boolean logic evaluation in audience segmentation.
 * Rules contain conditions and operators for complex expression evaluation.
 */
public class Rule {

    @NotBlank
    private final String id;

    @NotBlank
    private final String segmentId;

    @NotBlank
    private final String name;

    private final String description;

    @NotNull
    private final RuleType type;

    @NotBlank
    private final String expression;

    @NotNull
    private final Map<String, Object> metadata;

    private final int priority;

    private final boolean active;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    @JsonCreator
    public Rule(
            @JsonProperty("id") String id,
            @JsonProperty("segmentId") String segmentId,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("type") RuleType type,
            @JsonProperty("expression") String expression,
            @JsonProperty("metadata") Map<String, Object> metadata,
            @JsonProperty("priority") int priority,
            @JsonProperty("active") boolean active,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt) {
        this.id = id;
        this.segmentId = segmentId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.expression = expression;
        this.metadata = metadata;
        this.priority = priority;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public RuleType getType() {
        return type;
    }

    public String getExpression() {
        return expression;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public int getPriority() {
        return priority;
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
        Rule rule = (Rule) o;
        return Objects.equals(id, rule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Rule{" +
                "id='" + id + '\'' +
                ", segmentId='" + segmentId + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", active=" + active +
                '}';
    }

    /**
     * Builder pattern for creating Rule instances
     */
    public static class Builder {
        private String id;
        private String segmentId;
        private String name;
        private String description;
        private RuleType type;
        private String expression;
        private Map<String, Object> metadata;
        private int priority;
        private boolean active = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder segmentId(String segmentId) {
            this.segmentId = segmentId;
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

        public Builder type(RuleType type) {
            this.type = type;
            return this;
        }

        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
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

        public Rule build() {
            return new Rule(id, segmentId, name, description, type, expression, metadata, 
                           priority, active, createdAt, updatedAt);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}