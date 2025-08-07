package com.audiencemanager.api.service;

import com.audiencemanager.api.dto.CreateSegmentRequest;
import com.audiencemanager.api.dto.SegmentResponse;
import com.audiencemanager.api.entity.SegmentDependencyEntity;
import com.audiencemanager.api.entity.SegmentEntity;
import com.audiencemanager.api.entity.SegmentRuleEntity;
import com.audiencemanager.api.exception.SegmentNotFoundException;
import com.audiencemanager.api.exception.SegmentValidationException;
import com.audiencemanager.api.repository.SegmentDependencyRepository;
import com.audiencemanager.api.repository.SegmentRepository;
import com.audiencemanager.api.repository.SegmentRuleRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for segment management operations.
 * Handles business logic for creating, updating, and managing segments.
 */
@Service
@Transactional
public class SegmentService {

    private static final Logger logger = LoggerFactory.getLogger(SegmentService.class);

    private final SegmentRepository segmentRepository;
    private final SegmentRuleRepository segmentRuleRepository;
    private final SegmentDependencyRepository segmentDependencyRepository;
    private final SegmentValidationService validationService;

    @Autowired
    public SegmentService(SegmentRepository segmentRepository,
                         SegmentRuleRepository segmentRuleRepository,
                         SegmentDependencyRepository segmentDependencyRepository,
                         SegmentValidationService validationService) {
        this.segmentRepository = segmentRepository;
        this.segmentRuleRepository = segmentRuleRepository;
        this.segmentDependencyRepository = segmentDependencyRepository;
        this.validationService = validationService;
    }

    /**
     * Create a new segment (independent or derived)
     */
    @Timed(value = "segment.create", description = "Time taken to create a segment")
    @Counted(value = "segment.create", description = "Number of segments created")
    public SegmentResponse createSegment(CreateSegmentRequest request) {
        logger.info("Creating segment: {}", request.getName());

        // Validate the request
        validationService.validateCreateSegmentRequest(request);

        // Check if name already exists
        if (segmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new SegmentValidationException("Segment with name '" + request.getName() + "' already exists");
        }

        // Create the segment entity
        SegmentEntity segment = new SegmentEntity(
                request.getName(),
                request.getDescription(),
                request.getType(),
                request.getWindowMinutes()
        );

        if (request.getType() == SegmentEntity.SegmentCategory.DERIVED) {
            segment.setLogicalExpression(request.getLogicalExpression());
        }

        // Save the segment first to get ID
        segment = segmentRepository.save(segment);
        logger.info("Created segment with ID: {}", segment.getId());

        // Add rules for independent segments
        if (request.getType() == SegmentEntity.SegmentCategory.INDEPENDENT && request.getRules() != null) {
            for (CreateSegmentRequest.RuleDto ruleDto : request.getRules()) {
                SegmentRuleEntity rule = new SegmentRuleEntity(
                        ruleDto.getEventType(),
                        ruleDto.getAttribute(),
                        ruleDto.getOperator(),
                        ruleDto.getValue(),
                        ruleDto.getWindowMinutes()
                );
                segment.addRule(rule);
            }
        }

        // Add dependencies for derived segments
        if (request.getType() == SegmentEntity.SegmentCategory.DERIVED && request.getDependencies() != null) {
            for (CreateSegmentRequest.DependencyDto depDto : request.getDependencies()) {
                SegmentEntity independentSegment = segmentRepository.findById(depDto.getIndependentSegmentId())
                        .orElseThrow(() -> new SegmentNotFoundException("Independent segment not found: " + depDto.getIndependentSegmentId()));

                SegmentDependencyEntity dependency = new SegmentDependencyEntity(
                        segment,
                        independentSegment,
                        depDto.getLogicalOperator()
                );
                segment.addDependency(dependency);
            }
        }

        // Save with relationships
        segment = segmentRepository.save(segment);



        return convertToResponse(segment);
    }

    /**
     * Get segment by ID
     */
    @Timed(value = "segment.get", description = "Time taken to get a segment")
    public SegmentResponse getSegment(UUID id) {
        logger.debug("Getting segment: {}", id);

        SegmentEntity segment = segmentRepository.findById(id)
                .orElseThrow(() -> new SegmentNotFoundException("Segment not found: " + id));

        return convertToResponse(segment);
    }

    /**
     * Get all segments with pagination
     */
    @Timed(value = "segment.list", description = "Time taken to list segments")
    public Page<SegmentResponse> getSegments(SegmentEntity.SegmentCategory type, Boolean active, Pageable pageable) {
        logger.debug("Listing segments - type: {}, active: {}", type, active);

        Page<SegmentEntity> segments;
        
        if (type != null && active != null) {
            segments = segmentRepository.findByTypeAndActive(type, active, pageable);
        } else if (type != null) {
            segments = segmentRepository.findByType(type).stream()
                    .collect(Collectors.collectingAndThen(Collectors.toList(), 
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())));
        } else if (active != null) {
            if (active) {
                segments = segmentRepository.findByActiveTrue().stream()
                        .collect(Collectors.collectingAndThen(Collectors.toList(), 
                                list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())));
            } else {
                segments = segmentRepository.findByActiveFalse().stream()
                        .collect(Collectors.collectingAndThen(Collectors.toList(), 
                                list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())));
            }
        } else {
            segments = segmentRepository.findAll(pageable);
        }

        return segments.map(this::convertToResponse);
    }

    /**
     * Update an existing segment
     */
    @Timed(value = "segment.update", description = "Time taken to update segment")
    public SegmentResponse updateSegment(UUID id, CreateSegmentRequest request) {
        logger.info("Updating segment {} with new configuration", id);

        SegmentEntity segment = segmentRepository.findById(id)
                .orElseThrow(() -> new SegmentNotFoundException("Segment not found: " + id));

        // Update basic properties
        segment.setName(request.getName());
        segment.setDescription(request.getDescription());
        segment.setWindowMinutes(request.getWindowMinutes());
        segment.setLogicalExpression(request.getLogicalExpression());

        // Remove existing rules
        if (segment.getRules() != null) {
            segmentRuleRepository.deleteAll(segment.getRules());
            segment.getRules().clear();
        }

        // Remove existing dependencies
        if (segment.getDependencies() != null) {
            segmentDependencyRepository.deleteAll(segment.getDependencies());
            segment.getDependencies().clear();
        }

        // Add new rules for independent segments
        if (segment.getType() == SegmentEntity.SegmentCategory.INDEPENDENT && request.getRules() != null) {
            for (CreateSegmentRequest.RuleDto ruleDto : request.getRules()) {
                SegmentRuleEntity rule = new SegmentRuleEntity(
                        ruleDto.getEventType(),
                        ruleDto.getAttribute(),
                        ruleDto.getOperator(),
                        ruleDto.getValue(),
                        ruleDto.getWindowMinutes()
                );
                segment.addRule(rule);
            }
        }

        // Add new dependencies for derived segments
        if (segment.getType() == SegmentEntity.SegmentCategory.DERIVED && request.getDependencies() != null) {
            for (CreateSegmentRequest.DependencyDto depDto : request.getDependencies()) {
                SegmentEntity independentSegment = segmentRepository.findById(depDto.getIndependentSegmentId())
                        .orElseThrow(() -> new SegmentNotFoundException("Independent segment not found: " + depDto.getIndependentSegmentId()));

                SegmentDependencyEntity dependency = new SegmentDependencyEntity(
                        segment,
                        independentSegment,
                        depDto.getLogicalOperator()
                );
                segment.addDependency(dependency);
            }
        }

        // Save the updated segment
        segment = segmentRepository.save(segment);



        logger.info("Successfully updated segment: {}", segment.getName());
        return convertToResponse(segment);
    }

    /**
     * Update segment status (activate/deactivate)
     */
    @Timed(value = "segment.update.status", description = "Time taken to update segment status")
    public SegmentResponse updateSegmentStatus(UUID id, boolean active) {
        logger.info("Updating segment {} status to: {}", id, active);

        SegmentEntity segment = segmentRepository.findById(id)
                .orElseThrow(() -> new SegmentNotFoundException("Segment not found: " + id));

        segment.setActive(active);
        segment = segmentRepository.save(segment);



        return convertToResponse(segment);
    }

    /**
     * Delete segment
     */
    @Timed(value = "segment.delete", description = "Time taken to delete a segment")
    public void deleteSegment(UUID id) {
        logger.info("Deleting segment: {}", id);

        SegmentEntity segment = segmentRepository.findById(id)
                .orElseThrow(() -> new SegmentNotFoundException("Segment not found: " + id));

        // Check if segment is used in derived segments
        long dependentCount = segmentDependencyRepository.countByIndependentSegmentId(id);
        if (dependentCount > 0) {
            throw new SegmentValidationException("Cannot delete segment. It is used in " + dependentCount + " derived segments");
        }



        segmentRepository.delete(segment);
        logger.info("Deleted segment: {}", id);
    }

    /**
     * Get independent segments (for use in derived segments)
     */
    public List<SegmentResponse> getIndependentSegments() {
        logger.debug("Getting all active independent segments");

        List<SegmentEntity> segments = segmentRepository.findActiveIndependentSegments(SegmentEntity.SegmentCategory.INDEPENDENT);
        return segments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search segments by name or description
     */
    @Timed(value = "segment.search", description = "Time taken to search segments")
    public Page<SegmentResponse> searchSegments(String searchTerm, Pageable pageable) {
        logger.debug("Searching segments with term: {}", searchTerm);

        Page<SegmentEntity> segments = segmentRepository.searchByNameOrDescription(searchTerm, pageable);
        return segments.map(this::convertToResponse);
    }

    /**
     * Convert entity to response DTO
     */
    private SegmentResponse convertToResponse(SegmentEntity entity) {
        SegmentResponse response = new SegmentResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setType(entity.getType());
        response.setLogicalExpression(entity.getLogicalExpression());
        response.setWindowMinutes(entity.getWindowMinutes());
        response.setActive(entity.getActive());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        // Convert rules
        if (entity.getRules() != null && !entity.getRules().isEmpty()) {
            List<SegmentResponse.RuleResponseDto> rules = entity.getRules().stream()
                    .map(rule -> new SegmentResponse.RuleResponseDto(
                            rule.getId(),
                            rule.getEventType(),
                            rule.getAttribute(),
                            rule.getOperator(),
                            rule.getValue(),
                            rule.getWindowMinutes(),
                            rule.getActive(),
                            rule.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            response.setRules(rules);
        }

        // Convert dependencies
        if (entity.getDependencies() != null && !entity.getDependencies().isEmpty()) {
            List<SegmentResponse.DependencyResponseDto> dependencies = entity.getDependencies().stream()
                    .map(dep -> new SegmentResponse.DependencyResponseDto(
                            dep.getId(),
                            dep.getIndependentSegment().getId(),
                            dep.getIndependentSegment().getName(),
                            dep.getLogicalOperator(),
                            dep.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            response.setDependencies(dependencies);
        }

        return response;
    }


}