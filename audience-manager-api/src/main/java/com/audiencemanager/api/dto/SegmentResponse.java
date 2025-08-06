package com.audiencemanager.api.dto;

import com.audiencemanager.api.entity.SegmentDependencyEntity;
import com.audiencemanager.api.entity.SegmentRuleEntity;
import com.audiencemanager.api.entity.SegmentEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for segment response data.
 */
@Schema(description = "Segment response with full details")
public class SegmentResponse {

    @Schema(description = "Unique identifier of the segment")
    private UUID id;

    @Schema(description = "Name of the segment")
    private String name;

    @Schema(description = "Description of the segment")
    private String description;

    @Schema(description = "Type of segment: INDEPENDENT or DERIVED")
    private SegmentEntity.SegmentCategory type;

    @Schema(description = "Segment category")
    private SegmentEntity.SegmentType segmentType;

    @Schema(description = "Logical expression for derived segments")
    private String logicalExpression;

    @Schema(description = "Time window in minutes for evaluation")
    private Integer windowMinutes;

    @Schema(description = "Whether the segment is active")
    private Boolean active;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Rules for independent segments")
    private List<RuleResponseDto> rules;

    @Schema(description = "Dependencies for derived segments")
    private List<DependencyResponseDto> dependencies;

    // Nested DTOs
    @Schema(description = "Rule details for independent segments")
    public static class RuleResponseDto {
        private UUID id;
        private SegmentRuleEntity.EventType eventType;
        private String attribute;
        private SegmentRuleEntity.Operator operator;
        private BigDecimal value;
        private Integer windowMinutes;
        private Boolean active;
        private LocalDateTime createdAt;

        // Constructors
        public RuleResponseDto() {}

        public RuleResponseDto(UUID id, SegmentRuleEntity.EventType eventType, String attribute,
                              SegmentRuleEntity.Operator operator, BigDecimal value, Integer windowMinutes,
                              Boolean active, LocalDateTime createdAt) {
            this.id = id;
            this.eventType = eventType;
            this.attribute = attribute;
            this.operator = operator;
            this.value = value;
            this.windowMinutes = windowMinutes;
            this.active = active;
            this.createdAt = createdAt;
        }

        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public SegmentRuleEntity.EventType getEventType() { return eventType; }
        public void setEventType(SegmentRuleEntity.EventType eventType) { this.eventType = eventType; }

        public String getAttribute() { return attribute; }
        public void setAttribute(String attribute) { this.attribute = attribute; }

        public SegmentRuleEntity.Operator getOperator() { return operator; }
        public void setOperator(SegmentRuleEntity.Operator operator) { this.operator = operator; }

        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }

        public Integer getWindowMinutes() { return windowMinutes; }
        public void setWindowMinutes(Integer windowMinutes) { this.windowMinutes = windowMinutes; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    @Schema(description = "Dependency details for derived segments")
    public static class DependencyResponseDto {
        private UUID id;
        private UUID independentSegmentId;
        private String independentSegmentName;
        private SegmentDependencyEntity.LogicalOperator logicalOperator;
        private LocalDateTime createdAt;

        // Constructors
        public DependencyResponseDto() {}

        public DependencyResponseDto(UUID id, UUID independentSegmentId, String independentSegmentName,
                                   SegmentDependencyEntity.LogicalOperator logicalOperator, LocalDateTime createdAt) {
            this.id = id;
            this.independentSegmentId = independentSegmentId;
            this.independentSegmentName = independentSegmentName;
            this.logicalOperator = logicalOperator;
            this.createdAt = createdAt;
        }

        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public UUID getIndependentSegmentId() { return independentSegmentId; }
        public void setIndependentSegmentId(UUID independentSegmentId) { this.independentSegmentId = independentSegmentId; }

        public String getIndependentSegmentName() { return independentSegmentName; }
        public void setIndependentSegmentName(String independentSegmentName) { this.independentSegmentName = independentSegmentName; }

        public SegmentDependencyEntity.LogicalOperator getLogicalOperator() { return logicalOperator; }
        public void setLogicalOperator(SegmentDependencyEntity.LogicalOperator logicalOperator) { this.logicalOperator = logicalOperator; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // Constructors
    public SegmentResponse() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public SegmentEntity.SegmentCategory getType() { return type; }
    public void setType(SegmentEntity.SegmentCategory type) { this.type = type; }

    public SegmentEntity.SegmentType getSegmentType() { return segmentType; }
    public void setSegmentType(SegmentEntity.SegmentType segmentType) { this.segmentType = segmentType; }

    public String getLogicalExpression() { return logicalExpression; }
    public void setLogicalExpression(String logicalExpression) { this.logicalExpression = logicalExpression; }

    public Integer getWindowMinutes() { return windowMinutes; }
    public void setWindowMinutes(Integer windowMinutes) { this.windowMinutes = windowMinutes; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<RuleResponseDto> getRules() { return rules; }
    public void setRules(List<RuleResponseDto> rules) { this.rules = rules; }

    public List<DependencyResponseDto> getDependencies() { return dependencies; }
    public void setDependencies(List<DependencyResponseDto> dependencies) { this.dependencies = dependencies; }
}