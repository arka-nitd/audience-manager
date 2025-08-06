package com.audiencemanager.api.dto;

import com.audiencemanager.api.entity.SegmentDependencyEntity;
import com.audiencemanager.api.entity.SegmentRuleEntity;
import com.audiencemanager.api.entity.SegmentEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a new segment (independent or derived).
 */
@Schema(description = "Request to create a new segment")
public class CreateSegmentRequest {

    @NotBlank(message = "Segment name is required")
    @Size(min = 1, max = 255, message = "Segment name must be between 1 and 255 characters")
    @Schema(description = "Unique name for the segment", example = "High Clickers")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Optional description of the segment", example = "Users with more than 100 clicks in last 5 minutes")
    private String description;

    @NotNull(message = "Segment type is required")
    @Schema(description = "Type of segment: INDEPENDENT or DERIVED")
    private SegmentEntity.SegmentCategory type;

    @NotNull(message = "Segment category is required")
    @Schema(description = "Segment category", example = "DYNAMIC")
    private SegmentEntity.SegmentType segmentType = SegmentEntity.SegmentType.DYNAMIC;

    @Positive(message = "Window minutes must be positive")
    @Max(value = 1440, message = "Window minutes cannot exceed 1440 (24 hours)")
    @Schema(description = "Time window in minutes for evaluation", example = "5")
    private Integer windowMinutes = 5;

    @Valid
    @Schema(description = "Rules for independent segments (ignored for derived segments)")
    private List<RuleDto> rules;

    @Valid
    @Schema(description = "Dependencies for derived segments (ignored for independent segments)")
    private List<DependencyDto> dependencies;

    @Schema(description = "Logical expression for derived segments", example = "segment_1 AND segment_2")
    private String logicalExpression;

    // Nested DTOs
    @Schema(description = "Rule definition for independent segments")
    public static class RuleDto {
        
        @NotNull(message = "Event type is required")
        @Schema(description = "Type of event", allowableValues = {"clicks", "installs", "orders", "addToCart"})
        private SegmentRuleEntity.EventType eventType;

        @NotBlank(message = "Attribute is required")
        @Schema(description = "Attribute to evaluate", example = "count")
        private String attribute;

        @NotNull(message = "Operator is required")
        @Schema(description = "Comparison operator", allowableValues = {"GT", "LT", "GTE", "LTE", "EQ", "NEQ"})
        private SegmentRuleEntity.Operator operator;

        @NotNull(message = "Value is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than 0")
        @Schema(description = "Value to compare against", example = "100")
        private BigDecimal value;

        @Positive(message = "Window minutes must be positive")
        @Max(value = 1440, message = "Window minutes cannot exceed 1440 (24 hours)")
        @Schema(description = "Time window in minutes", example = "5")
        private Integer windowMinutes = 5;

        // Constructors
        public RuleDto() {}

        public RuleDto(SegmentRuleEntity.EventType eventType, String attribute, 
                      SegmentRuleEntity.Operator operator, BigDecimal value, Integer windowMinutes) {
            this.eventType = eventType;
            this.attribute = attribute;
            this.operator = operator;
            this.value = value;
            this.windowMinutes = windowMinutes;
        }

        // Getters and Setters
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
    }

    @Schema(description = "Dependency definition for derived segments")
    public static class DependencyDto {
        
        @NotNull(message = "Independent segment ID is required")
        @Schema(description = "UUID of the independent segment to reference")
        private UUID independentSegmentId;

        @NotNull(message = "Logical operator is required")
        @Schema(description = "Logical operator", allowableValues = {"AND", "OR", "NOT"})
        private SegmentDependencyEntity.LogicalOperator logicalOperator;

        // Constructors
        public DependencyDto() {}

        public DependencyDto(UUID independentSegmentId, SegmentDependencyEntity.LogicalOperator logicalOperator) {
            this.independentSegmentId = independentSegmentId;
            this.logicalOperator = logicalOperator;
        }

        // Getters and Setters
        public UUID getIndependentSegmentId() { return independentSegmentId; }
        public void setIndependentSegmentId(UUID independentSegmentId) { this.independentSegmentId = independentSegmentId; }

        public SegmentDependencyEntity.LogicalOperator getLogicalOperator() { return logicalOperator; }
        public void setLogicalOperator(SegmentDependencyEntity.LogicalOperator logicalOperator) { this.logicalOperator = logicalOperator; }
    }

    // Constructors
    public CreateSegmentRequest() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public SegmentEntity.SegmentCategory getType() { return type; }
    public void setType(SegmentEntity.SegmentCategory type) { this.type = type; }

    public SegmentEntity.SegmentType getSegmentType() { return segmentType; }
    public void setSegmentType(SegmentEntity.SegmentType segmentType) { this.segmentType = segmentType; }

    public Integer getWindowMinutes() { return windowMinutes; }
    public void setWindowMinutes(Integer windowMinutes) { this.windowMinutes = windowMinutes; }

    public List<RuleDto> getRules() { return rules; }
    public void setRules(List<RuleDto> rules) { this.rules = rules; }

    public List<DependencyDto> getDependencies() { return dependencies; }
    public void setDependencies(List<DependencyDto> dependencies) { this.dependencies = dependencies; }

    public String getLogicalExpression() { return logicalExpression; }
    public void setLogicalExpression(String logicalExpression) { this.logicalExpression = logicalExpression; }
}