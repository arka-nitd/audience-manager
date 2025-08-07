package com.audiencemanager.api.controller;

import com.audiencemanager.api.dto.CreateSegmentRequest;
import com.audiencemanager.api.dto.SegmentResponse;
import com.audiencemanager.api.service.SegmentService;
import com.audiencemanager.api.entity.SegmentEntity;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for segment management operations.
 * Provides endpoints for creating, reading, updating, and deleting audience segments.
 */
@RestController
@RequestMapping("/api/v1/segments")
@Tag(name = "Segments", description = "Audience segment management API")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SegmentController {

    private static final Logger logger = LoggerFactory.getLogger(SegmentController.class);

    private final SegmentService segmentService;

    @Autowired
    public SegmentController(SegmentService segmentService) {
        this.segmentService = segmentService;
    }

    /**
     * Create a new segment (independent or derived)
     */
    @PostMapping
    @Operation(summary = "Create a new segment", description = "Create either an independent or derived segment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Segment created successfully",
                content = @Content(schema = @Schema(implementation = SegmentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Segment name already exists")
    })
    @Timed(value = "api.segment.create", description = "Time taken to create a segment via API")
    @Counted(value = "api.segment.create", description = "Number of segment creation API calls")
    public ResponseEntity<SegmentResponse> createSegment(
            @Valid @RequestBody CreateSegmentRequest request) {
        
        logger.info("Creating segment: {}", request.getName());
        
        SegmentResponse response = segmentService.createSegment(request);
        
        logger.info("Successfully created segment: {} with ID: {}", response.getName(), response.getId());
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get segment by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get segment by ID", description = "Retrieve a specific segment by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Segment found",
                content = @Content(schema = @Schema(implementation = SegmentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Segment not found")
    })
    @Timed(value = "api.segment.get", description = "Time taken to get a segment via API")
    public ResponseEntity<SegmentResponse> getSegment(
            @Parameter(description = "Segment UUID", required = true)
            @PathVariable UUID id) {
        
        logger.debug("Getting segment: {}", id);
        
        SegmentResponse response = segmentService.getSegment(id);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all segments with pagination and filtering
     */
    @GetMapping
    @Operation(summary = "List segments", description = "Get a paginated list of segments with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Segments retrieved successfully")
    })
    @Timed(value = "api.segment.list", description = "Time taken to list segments via API")
    public ResponseEntity<Page<SegmentResponse>> getSegments(
            @Parameter(description = "Filter by segment type (INDEPENDENT or DERIVED)")
            @RequestParam(required = false) SegmentEntity.SegmentCategory type,
            
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active,
            
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sort,
            
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "desc") String direction) {
        
        logger.debug("Listing segments - type: {}, active: {}, page: {}, size: {}", type, active, page, size);
        
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<SegmentResponse> response = segmentService.getSegments(type, active, pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Search segments by name or description
     */
    @GetMapping("/search")
    @Operation(summary = "Search segments", description = "Search segments by name or description")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @Timed(value = "api.segment.search", description = "Time taken to search segments via API")
    public ResponseEntity<Page<SegmentResponse>> searchSegments(
            @Parameter(description = "Search term", required = true)
            @RequestParam String q,
            
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("Searching segments with term: {}", q);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SegmentResponse> response = segmentService.searchSegments(q, pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all independent segments (for use in derived segments)
     */
    @GetMapping("/independent")
    @Operation(summary = "Get independent segments", description = "Get all active independent segments for use in derived segments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Independent segments retrieved successfully")
    })
    @Timed(value = "api.segment.independent", description = "Time taken to get independent segments via API")
    public ResponseEntity<List<SegmentResponse>> getIndependentSegments() {
        
        logger.debug("Getting all independent segments");
        
        List<SegmentResponse> response = segmentService.getIndependentSegments();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update a segment
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update segment", description = "Update an existing segment with new configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Segment updated successfully",
                content = @Content(schema = @Schema(implementation = SegmentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Segment not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @Timed(value = "segment.update.time", description = "Time taken to update segment")
    @Counted(value = "segment.update.count", description = "Total number of segment updates")
    public ResponseEntity<SegmentResponse> updateSegment(
            @Parameter(description = "Segment ID") @PathVariable UUID id,
            @Valid @RequestBody CreateSegmentRequest request) {
        
        logger.info("Updating segment with ID: {}", id);
        
        SegmentResponse updatedSegment = segmentService.updateSegment(id, request);
        
        logger.info("Successfully updated segment: {}", updatedSegment.getName());
        return ResponseEntity.ok(updatedSegment);
    }

    /**
     * Update segment status (activate/deactivate)
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update segment status", description = "Activate or deactivate a segment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Segment status updated successfully",
                content = @Content(schema = @Schema(implementation = SegmentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Segment not found")
    })
    @Timed(value = "api.segment.status", description = "Time taken to update segment status via API")
    public ResponseEntity<SegmentResponse> updateSegmentStatus(
            @Parameter(description = "Segment UUID", required = true)
            @PathVariable UUID id,
            
            @RequestBody Map<String, Boolean> statusRequest) {
        
        Boolean active = statusRequest.get("active");
        if (active == null) {
            throw new IllegalArgumentException("'active' field is required in request body");
        }
        
        logger.info("Updating segment {} status to: {}", id, active);
        
        SegmentResponse response = segmentService.updateSegmentStatus(id, active);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete segment
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete segment", description = "Delete a segment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Segment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Segment not found"),
        @ApiResponse(responseCode = "409", description = "Segment cannot be deleted (used in derived segments)")
    })
    @Timed(value = "api.segment.delete", description = "Time taken to delete a segment via API")
    public ResponseEntity<Void> deleteSegment(
            @Parameter(description = "Segment UUID", required = true)
            @PathVariable UUID id) {
        
        logger.info("Deleting segment: {}", id);
        
        segmentService.deleteSegment(id);
        
        logger.info("Successfully deleted segment: {}", id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the segment service is healthy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "segment-api",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}