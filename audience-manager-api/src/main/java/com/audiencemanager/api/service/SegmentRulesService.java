package com.audiencemanager.api.service;

import com.audiencemanager.api.dto.SegmentRuleDto;
import com.audiencemanager.api.entity.SegmentEntity;
import com.audiencemanager.api.entity.SegmentRuleEntity;
import com.audiencemanager.api.repository.SegmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for providing segment rules to Flink processing
 */
@Service
public class SegmentRulesService {

    private static final Logger logger = LoggerFactory.getLogger(SegmentRulesService.class);

    @Autowired
    private SegmentRepository segmentRepository;

    /**
     * Get all active segment rules for Flink processing
     */
    public List<SegmentRuleDto> getAllActiveRules() {
        logger.debug("Fetching all active segment rules");
        
        List<SegmentEntity> activeSegments = segmentRepository.findByActiveTrue();
        List<SegmentRuleDto> rules = new ArrayList<>();
        
        for (SegmentEntity segment : activeSegments) {
            for (SegmentRuleEntity rule : segment.getRules()) {
                SegmentRuleDto ruleDto = convertToDto(rule, segment);
                rules.add(ruleDto);
            }
        }
        
        logger.debug("Found {} active rules from {} segments", rules.size(), activeSegments.size());
        return rules;
    }

    /**
     * Get segment rules filtered by event type
     */
    public List<SegmentRuleDto> getRulesByEventType(String eventType) {
        logger.debug("Fetching segment rules for event type: {}", eventType);
        
        List<SegmentEntity> activeSegments = segmentRepository.findByActiveTrue();
        List<SegmentRuleDto> rules = new ArrayList<>();
        
        for (SegmentEntity segment : activeSegments) {
            for (SegmentRuleEntity rule : segment.getRules()) {
                if (eventType.equals(rule.getEventType().name().toLowerCase())) {
                    SegmentRuleDto ruleDto = convertToDto(rule, segment);
                    rules.add(ruleDto);
                }
            }
        }
        
        logger.debug("Found {} rules for event type {}", rules.size(), eventType);
        return rules;
    }

    private SegmentRuleDto convertToDto(SegmentRuleEntity rule, SegmentEntity segment) {
        return new SegmentRuleDto(
                rule.getId().toString(),
                segment.getId().toString(),
                rule.getEventType().name().toLowerCase(),
                rule.getAttribute().name().toLowerCase(),
                rule.getOperator().name(),
                rule.getValue().doubleValue(),
                segment.getWindowMinutes()
        );
    }
}