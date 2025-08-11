package com.audiencemanager.api.controller;

import com.audiencemanager.api.dto.SegmentRuleDto;
import com.audiencemanager.api.service.SegmentRulesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for segment rules used by Flink processing
 */
@RestController
@RequestMapping("/api/v1/segments")
@Tag(name = "Segment Rules", description = "API for retrieving segment rules for streaming processing")
public class SegmentRulesController {

    private static final Logger logger = LoggerFactory.getLogger(SegmentRulesController.class);

    @Autowired
    private SegmentRulesService segmentRulesService;

    @Operation(
            summary = "Get all active segment rules",
            description = "Retrieves all active segment rules for real-time processing"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved segment rules")
    @GetMapping("/rules")
    public ResponseEntity<List<SegmentRuleDto>> getAllActiveRules() {
        logger.info("Fetching all active segment rules for Flink processing");
        
        try {
            List<SegmentRuleDto> rules = segmentRulesService.getAllActiveRules();
            logger.info("Successfully retrieved {} active segment rules", rules.size());
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("Failed to retrieve segment rules", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "Get rules by event type",
            description = "Retrieves segment rules filtered by event type"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered segment rules")
    @GetMapping("/rules/eventType/{eventType}")
    public ResponseEntity<List<SegmentRuleDto>> getRulesByEventType(@PathVariable String eventType) {
        logger.info("Fetching segment rules for event type: {}", eventType);
        
        try {
            List<SegmentRuleDto> rules = segmentRulesService.getRulesByEventType(eventType);
            logger.info("Successfully retrieved {} rules for event type {}", rules.size(), eventType);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("Failed to retrieve segment rules for event type: {}", eventType, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}