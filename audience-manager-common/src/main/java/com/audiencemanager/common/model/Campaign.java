package com.audiencemanager.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a communication campaign for targeted messaging to audience segments.
 */
public class Campaign {

    @NotBlank
    private final String id;

    @NotBlank
    private final String name;

    private final String description;

    @NotBlank
    private final String segmentId;

    @NotNull
    private final CampaignType type;

    @NotNull
    private final CampaignStatus status;

    private final String templateId;

    @NotNull
    private final Map<String, Object> content;

    private final LocalDateTime scheduledAt;

    private final LocalDateTime sentAt;

    private final int targetAudienceSize;

    private final int sentCount;

    private final int deliveredCount;

    private final int openCount;

    private final int clickCount;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    @JsonCreator
    public Campaign(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("segmentId") String segmentId,
            @JsonProperty("type") CampaignType type,
            @JsonProperty("status") CampaignStatus status,
            @JsonProperty("templateId") String templateId,
            @JsonProperty("content") Map<String, Object> content,
            @JsonProperty("scheduledAt") LocalDateTime scheduledAt,
            @JsonProperty("sentAt") LocalDateTime sentAt,
            @JsonProperty("targetAudienceSize") int targetAudienceSize,
            @JsonProperty("sentCount") int sentCount,
            @JsonProperty("deliveredCount") int deliveredCount,
            @JsonProperty("openCount") int openCount,
            @JsonProperty("clickCount") int clickCount,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.segmentId = segmentId;
        this.type = type;
        this.status = status;
        this.templateId = templateId;
        this.content = content;
        this.scheduledAt = scheduledAt;
        this.sentAt = sentAt;
        this.targetAudienceSize = targetAudienceSize;
        this.sentCount = sentCount;
        this.deliveredCount = deliveredCount;
        this.openCount = openCount;
        this.clickCount = clickCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSegmentId() { return segmentId; }
    public CampaignType getType() { return type; }
    public CampaignStatus getStatus() { return status; }
    public String getTemplateId() { return templateId; }
    public Map<String, Object> getContent() { return content; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public int getTargetAudienceSize() { return targetAudienceSize; }
    public int getSentCount() { return sentCount; }
    public int getDeliveredCount() { return deliveredCount; }
    public int getOpenCount() { return openCount; }
    public int getClickCount() { return clickCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Campaign campaign = (Campaign) o;
        return Objects.equals(id, campaign.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Campaign{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", segmentId='" + segmentId + '\'' +
                ", type=" + type +
                ", status=" + status +
                '}';
    }

    /**
     * Builder pattern for creating Campaign instances
     */
    public static class Builder {
        private String id;
        private String name;
        private String description;
        private String segmentId;
        private CampaignType type;
        private CampaignStatus status;
        private String templateId;
        private Map<String, Object> content;
        private LocalDateTime scheduledAt;
        private LocalDateTime sentAt;
        private int targetAudienceSize;
        private int sentCount;
        private int deliveredCount;
        private int openCount;
        private int clickCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder segmentId(String segmentId) { this.segmentId = segmentId; return this; }
        public Builder type(CampaignType type) { this.type = type; return this; }
        public Builder status(CampaignStatus status) { this.status = status; return this; }
        public Builder templateId(String templateId) { this.templateId = templateId; return this; }
        public Builder content(Map<String, Object> content) { this.content = content; return this; }
        public Builder scheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; return this; }
        public Builder sentAt(LocalDateTime sentAt) { this.sentAt = sentAt; return this; }
        public Builder targetAudienceSize(int targetAudienceSize) { this.targetAudienceSize = targetAudienceSize; return this; }
        public Builder sentCount(int sentCount) { this.sentCount = sentCount; return this; }
        public Builder deliveredCount(int deliveredCount) { this.deliveredCount = deliveredCount; return this; }
        public Builder openCount(int openCount) { this.openCount = openCount; return this; }
        public Builder clickCount(int clickCount) { this.clickCount = clickCount; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Campaign build() {
            return new Campaign(id, name, description, segmentId, type, status, templateId, content,
                              scheduledAt, sentAt, targetAudienceSize, sentCount, deliveredCount,
                              openCount, clickCount, createdAt, updatedAt);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}