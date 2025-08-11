package com.audiencemanager.ingestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Result of segment rule evaluation for a user
 */
public class SegmentResult {
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("segmentId")
    private String segmentId;
    
    @JsonProperty("ruleId")
    private String ruleId;
    
    @JsonProperty("qualified")
    private Boolean qualified;
    
    @JsonProperty("value")
    private Double value;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("windowStart")
    private Long windowStart;
    
    @JsonProperty("windowEnd")
    private Long windowEnd;
    
    public SegmentResult() {}
    
    public SegmentResult(String userId, String segmentId, String ruleId, Boolean qualified, 
                        Double value, Long timestamp, Long windowStart, Long windowEnd) {
        this.userId = userId;
        this.segmentId = segmentId;
        this.ruleId = ruleId;
        this.qualified = qualified;
        this.value = value;
        this.timestamp = timestamp;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getSegmentId() {
        return segmentId;
    }
    
    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    
    public Boolean getQualified() {
        return qualified;
    }
    
    public void setQualified(Boolean qualified) {
        this.qualified = qualified;
    }
    
    public Double getValue() {
        return value;
    }
    
    public void setValue(Double value) {
        this.value = value;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getWindowStart() {
        return windowStart;
    }
    
    public void setWindowStart(Long windowStart) {
        this.windowStart = windowStart;
    }
    
    public Long getWindowEnd() {
        return windowEnd;
    }
    
    public void setWindowEnd(Long windowEnd) {
        this.windowEnd = windowEnd;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentResult that = (SegmentResult) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(segmentId, that.segmentId) &&
               Objects.equals(ruleId, that.ruleId) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, segmentId, ruleId, timestamp);
    }
    
    @Override
    public String toString() {
        return "SegmentResult{" +
                "userId='" + userId + '\'' +
                ", segmentId='" + segmentId + '\'' +
                ", ruleId='" + ruleId + '\'' +
                ", qualified=" + qualified +
                ", value=" + value +
                ", timestamp=" + timestamp +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                '}';
    }
}