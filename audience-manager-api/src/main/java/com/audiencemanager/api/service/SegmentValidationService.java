package com.audiencemanager.api.service;

import com.audiencemanager.api.dto.CreateSegmentRequest;
import com.audiencemanager.api.entity.SegmentEntity;
import com.audiencemanager.api.exception.SegmentValidationException;
import com.audiencemanager.api.repository.SegmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Service for validating segment business rules and constraints.
 */
@Service
public class SegmentValidationService {

    private static final Logger logger = LoggerFactory.getLogger(SegmentValidationService.class);

    private final SegmentRepository segmentRepository;

    @Autowired
    public SegmentValidationService(SegmentRepository segmentRepository) {
        this.segmentRepository = segmentRepository;
    }

    /**
     * Validate segment creation request
     */
    public void validateCreateSegmentRequest(CreateSegmentRequest request) {
        logger.debug("Validating segment creation request: {}", request.getName());

        // Basic validation
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new SegmentValidationException("Segment name is required");
        }

        if (request.getType() == null) {
            throw new SegmentValidationException("Segment type is required");
        }

        // Type-specific validation
        if (request.getType() == SegmentEntity.SegmentCategory.INDEPENDENT) {
            validateIndependentSegmentRequest(request);
        } else if (request.getType() == SegmentEntity.SegmentCategory.DERIVED) {
            validateDerivedSegmentRequest(request);
        }

        // Window minutes validation
        if (request.getWindowMinutes() != null && (request.getWindowMinutes() <= 0 || request.getWindowMinutes() > 1440)) {
            throw new SegmentValidationException("Window minutes must be between 1 and 1440 (24 hours)");
        }
    }

    /**
     * Validate independent segment specific requirements
     */
    private void validateIndependentSegmentRequest(CreateSegmentRequest request) {
        if (request.getRules() == null || request.getRules().isEmpty()) {
            throw new SegmentValidationException("Independent segments must have at least one rule");
        }

        if (request.getRules().size() > 10) {
            throw new SegmentValidationException("Independent segments cannot have more than 10 rules");
        }

        // Validate each rule
        for (CreateSegmentRequest.RuleDto rule : request.getRules()) {
            validateRule(rule);
        }

        // Ensure no dependencies are provided for independent segments
        if (request.getDependencies() != null && !request.getDependencies().isEmpty()) {
            throw new SegmentValidationException("Independent segments cannot have dependencies");
        }
    }

    /**
     * Validate derived segment specific requirements
     */
    private void validateDerivedSegmentRequest(CreateSegmentRequest request) {
        if (request.getDependencies() == null || request.getDependencies().isEmpty()) {
            throw new SegmentValidationException("Derived segments must have at least 2 dependencies");
        }

        if (request.getDependencies().size() < 2) {
            throw new SegmentValidationException("Derived segments must have at least 2 dependencies");
        }

        if (request.getDependencies().size() > 20) {
            throw new SegmentValidationException("Derived segments cannot have more than 20 dependencies");
        }

        // Ensure no rules are provided for derived segments
        if (request.getRules() != null && !request.getRules().isEmpty()) {
            throw new SegmentValidationException("Derived segments cannot have rules");
        }

        // Validate dependencies
        validateDependencies(request);
    }

    /**
     * Validate individual rule
     */
    private void validateRule(CreateSegmentRequest.RuleDto rule) {
        if (rule.getEventType() == null) {
            throw new SegmentValidationException("Rule event type is required");
        }

        if (rule.getAttribute() == null || rule.getAttribute().trim().isEmpty()) {
            throw new SegmentValidationException("Rule attribute is required");
        }

        if (rule.getOperator() == null) {
            throw new SegmentValidationException("Rule operator is required");
        }

        if (rule.getValue() == null) {
            throw new SegmentValidationException("Rule value is required");
        }

        if (rule.getValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new SegmentValidationException("Rule value must be greater than 0");
        }

        if (rule.getWindowMinutes() != null && (rule.getWindowMinutes() <= 0 || rule.getWindowMinutes() > 1440)) {
            throw new SegmentValidationException("Rule window minutes must be between 1 and 1440 (24 hours)");
        }

        // Validate attribute names for specific event types
        validateAttributeForEventType(rule.getEventType(), rule.getAttribute());
    }

    /**
     * Validate attribute names for event types
     */
    private void validateAttributeForEventType(com.audiencemanager.api.entity.SegmentRuleEntity.EventType eventType, String attribute) {
        Set<String> validAttributes = Set.of("count", "sum", "avg", "min", "max");
        
        if (!validAttributes.contains(attribute.toLowerCase())) {
            throw new SegmentValidationException("Invalid attribute '" + attribute + "' for event type " + eventType + 
                    ". Valid attributes: " + validAttributes);
        }
    }

    /**
     * Validate dependencies for derived segments
     */
    private void validateDependencies(CreateSegmentRequest request) {
        Set<UUID> dependencyIds = new HashSet<>();

        for (CreateSegmentRequest.DependencyDto dependency : request.getDependencies()) {
            if (dependency.getIndependentSegmentId() == null) {
                throw new SegmentValidationException("Dependency independent segment ID is required");
            }

            if (dependency.getLogicalOperator() == null) {
                throw new SegmentValidationException("Dependency logical operator is required");
            }

            // Check for duplicate dependencies
            if (!dependencyIds.add(dependency.getIndependentSegmentId())) {
                throw new SegmentValidationException("Duplicate dependency on segment: " + dependency.getIndependentSegmentId());
            }

            // Validate that referenced segment exists and is independent
            validateIndependentSegmentExists(dependency.getIndependentSegmentId());
        }

        // Validate logical expression if provided
        if (request.getLogicalExpression() != null) {
            validateLogicalExpression(request.getLogicalExpression(), request.getDependencies().size());
        }
    }

    /**
     * Validate that referenced segment exists and is independent
     */
    private void validateIndependentSegmentExists(UUID segmentId) {
        SegmentEntity segment = segmentRepository.findById(segmentId)
                .orElseThrow(() -> new SegmentValidationException("Referenced segment not found: " + segmentId));

        if (segment.getType() != SegmentEntity.SegmentCategory.INDEPENDENT) {
            throw new SegmentValidationException("Referenced segment must be INDEPENDENT type: " + segmentId);
        }

        if (!segment.getActive()) {
            throw new SegmentValidationException("Referenced segment must be active: " + segmentId);
        }
    }

    /**
     * Validate logical expression format
     */
    private void validateLogicalExpression(String expression, int dependencyCount) {
        if (expression == null || expression.trim().isEmpty()) {
            return; // Optional field
        }

        // Basic validation - should contain segment references and logical operators
        String[] validOperators = {"AND", "OR", "NOT", "(", ")"};
        String normalizedExpression = expression.toUpperCase();

        // Check for balanced parentheses
        long openParens = normalizedExpression.chars().filter(ch -> ch == '(').count();
        long closeParens = normalizedExpression.chars().filter(ch -> ch == ')').count();
        
        if (openParens != closeParens) {
            throw new SegmentValidationException("Unbalanced parentheses in logical expression");
        }

        // Check for valid operators
        boolean hasValidOperator = false;
        for (String operator : validOperators) {
            if (normalizedExpression.contains(operator)) {
                hasValidOperator = true;
                break;
            }
        }

        if (!hasValidOperator && dependencyCount > 1) {
            throw new SegmentValidationException("Logical expression must contain at least one logical operator (AND, OR, NOT)");
        }
    }

    /**
     * Validate segment name uniqueness
     */
    public void validateSegmentNameUniqueness(String name, UUID excludeId) {
        boolean exists;
        if (excludeId != null) {
            exists = segmentRepository.findByNameIgnoreCase(name)
                    .map(segment -> !segment.getId().equals(excludeId))
                    .orElse(false);
        } else {
            exists = segmentRepository.existsByNameIgnoreCase(name);
        }

        if (exists) {
            throw new SegmentValidationException("Segment with name '" + name + "' already exists");
        }
    }

    /**
     * Validate that segment can be deleted
     */
    public void validateSegmentDeletion(UUID segmentId) {
        // Check if segment exists
        SegmentEntity segment = segmentRepository.findById(segmentId)
                .orElseThrow(() -> new SegmentValidationException("Segment not found: " + segmentId));

        // Additional business rules can be added here
        // For example: check if segment is used in active campaigns
    }
}