package com.audiencemanager.ingestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Segment rule model for streaming processing
 */
public class SegmentRule {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("segmentId")
    private String segmentId;
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("attribute")
    private String attribute; // count, sum
    
    @JsonProperty("operator")
    private String operator; // GT, LT, EQ, etc.
    
    @JsonProperty("threshold")
    private Double threshold;
    
    @JsonProperty("windowMinutes")
    private Integer windowMinutes;
    
    public SegmentRule() {}
    
    public SegmentRule(String id, String segmentId, String eventType, String attribute, 
                      String operator, Double threshold, Integer windowMinutes) {
        this.id = id;
        this.segmentId = segmentId;
        this.eventType = eventType;
        this.attribute = attribute;
        this.operator = operator;
        this.threshold = threshold;
        this.windowMinutes = windowMinutes;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSegmentId() {
        return segmentId;
    }
    
    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getAttribute() {
        return attribute;
    }
    
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public Double getThreshold() {
        return threshold;
    }
    
    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }
    
    public Integer getWindowMinutes() {
        return windowMinutes;
    }
    
    public void setWindowMinutes(Integer windowMinutes) {
        this.windowMinutes = windowMinutes;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentRule that = (SegmentRule) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "SegmentRule{" +
                "id='" + id + '\'' +
                ", segmentId='" + segmentId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", attribute='" + attribute + '\'' +
                ", operator='" + operator + '\'' +
                ", threshold=" + threshold +
                ", windowMinutes=" + windowMinutes +
                '}';
    }
}