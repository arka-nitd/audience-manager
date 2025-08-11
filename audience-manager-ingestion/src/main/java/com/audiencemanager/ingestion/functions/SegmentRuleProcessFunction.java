package com.audiencemanager.ingestion.functions;

import com.audiencemanager.ingestion.model.UserEvent;
import com.audiencemanager.ingestion.model.SegmentRule;
import com.audiencemanager.ingestion.model.SegmentResult;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Process function for evaluating base segment rules on user events
 */
public class SegmentRuleProcessFunction extends KeyedBroadcastProcessFunction<String, UserEvent, SegmentRule, SegmentResult> {
    
    private static final Logger LOG = LoggerFactory.getLogger(SegmentRuleProcessFunction.class);
    private static final long serialVersionUID = 1L;
    
    private final MapStateDescriptor<String, SegmentRule> ruleStateDescriptor;
    
    // State to store user events for windowing
    private transient ValueState<List<UserEvent>> eventWindowState;
    
    public SegmentRuleProcessFunction(MapStateDescriptor<String, SegmentRule> ruleStateDescriptor) {
        this.ruleStateDescriptor = ruleStateDescriptor;
    }
    
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        
        ValueStateDescriptor<List<UserEvent>> eventDescriptor = new ValueStateDescriptor<>(
                "userEvents",
                TypeInformation.of(new org.apache.flink.api.common.typeinfo.TypeHint<List<UserEvent>>() {})
        );
        eventWindowState = getRuntimeContext().getState(eventDescriptor);
    }
    
    @Override
    public void processElement(UserEvent event, ReadOnlyContext ctx, Collector<SegmentResult> out) throws Exception {
        LOG.debug("Processing event: {}", event);
        
        // Get current events for this user
        List<UserEvent> userEvents = eventWindowState.value();
        if (userEvents == null) {
            userEvents = new ArrayList<>();
        }
        userEvents.add(event);
        
        // Clean old events and keep only relevant ones for all possible windows
        long currentTime = event.getTimestamp();
        long maxWindowMs = 60 * 60 * 1000; // 1 hour max window
        userEvents.removeIf(e -> (currentTime - e.getTimestamp()) > maxWindowMs);
        
        eventWindowState.update(userEvents);
        
        // Get all broadcast rules
        org.apache.flink.api.common.state.ReadOnlyBroadcastState<String, SegmentRule> broadcastState = ctx.getBroadcastState(ruleStateDescriptor);
        
        // Process each rule
        for (Map.Entry<String, SegmentRule> entry : broadcastState.immutableEntries()) {
            SegmentRule rule = entry.getValue();
            
            // Skip derived rules (they have dependencies)
            if (isDerivedRule(rule)) {
                continue;
            }
            
            // Evaluate this rule against the user's events
            SegmentResult result = evaluateRule(event.getUserId(), rule, userEvents, currentTime);
            if (result != null) {
                LOG.debug("Rule evaluation result: {}", result);
                out.collect(result);
            }
        }
    }
    
    @Override
    public void processBroadcastElement(SegmentRule rule, Context ctx, Collector<SegmentResult> out) throws Exception {
        LOG.info("Received broadcast rule: {}", rule);
        ctx.getBroadcastState(ruleStateDescriptor).put(rule.getId(), rule);
    }
    
    private boolean isDerivedRule(SegmentRule rule) {
        // For now, assume base rules have segment IDs that don't contain dependencies
        // In a real implementation, you'd check if the segment has dependencies
        return false; // Simplified for this implementation
    }
    
    private SegmentResult evaluateRule(String userId, SegmentRule rule, List<UserEvent> userEvents, long currentTime) {
        try {
            long windowMs = rule.getWindowMinutes() * 60L * 1000L;
            long windowStart = currentTime - windowMs;
            
            // Filter events for this rule's event type and window
            List<UserEvent> relevantEvents = userEvents.stream()
                    .filter(e -> e.getEventType().equals(rule.getEventType()))
                    .filter(e -> e.getTimestamp() >= windowStart && e.getTimestamp() <= currentTime)
                    .collect(java.util.stream.Collectors.toList());
            
            if (relevantEvents.isEmpty()) {
                return null;
            }
            
            // Calculate aggregate value
            double aggregateValue = calculateAggregate(relevantEvents, rule.getAttribute());
            
            // Check threshold
            boolean qualified = checkThreshold(aggregateValue, rule.getOperator(), rule.getThreshold());
            
            LOG.debug("Rule {} for user {}: aggregate={}, threshold={}, qualified={}", 
                    rule.getId(), userId, aggregateValue, rule.getThreshold(), qualified);
            
            return new SegmentResult(
                    userId,
                    rule.getSegmentId(),
                    rule.getId(),
                    qualified,
                    aggregateValue,
                    currentTime,
                    windowStart,
                    currentTime
            );
            
        } catch (Exception e) {
            LOG.error("Failed to evaluate rule {} for user {}", rule.getId(), userId, e);
            return null;
        }
    }
    
    private double calculateAggregate(List<UserEvent> events, String attribute) {
        switch (attribute.toLowerCase()) {
            case "count":
                return events.size();
            case "sum":
                return events.stream()
                        .mapToDouble(e -> e.getValue() != null ? e.getValue() : 0.0)
                        .sum();
            default:
                LOG.warn("Unknown attribute type: {}, defaulting to count", attribute);
                return events.size();
        }
    }
    
    private boolean checkThreshold(double value, String operator, double threshold) {
        switch (operator.toUpperCase()) {
            case "GT":
                return value > threshold;
            case "GTE":
                return value >= threshold;
            case "LT":
                return value < threshold;
            case "LTE":
                return value <= threshold;
            case "EQ":
                return Math.abs(value - threshold) < 0.001; // Float comparison
            case "NEQ":
                return Math.abs(value - threshold) >= 0.001;
            default:
                LOG.warn("Unknown operator: {}, defaulting to GT", operator);
                return value > threshold;
        }
    }
}