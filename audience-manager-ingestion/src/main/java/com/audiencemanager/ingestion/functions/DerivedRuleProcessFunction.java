package com.audiencemanager.ingestion.functions;

import com.audiencemanager.ingestion.model.SegmentResult;
import com.audiencemanager.ingestion.model.SegmentRule;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Process function for evaluating derived segment rules (combinations of base rules)
 */
public class DerivedRuleProcessFunction extends KeyedBroadcastProcessFunction<String, SegmentResult, SegmentRule, SegmentResult> {
    
    private static final Logger LOG = LoggerFactory.getLogger(DerivedRuleProcessFunction.class);
    private static final long serialVersionUID = 1L;
    
    private final MapStateDescriptor<String, SegmentRule> ruleStateDescriptor;
    
    // State to store recent base rule results for a user
    private transient ValueState<Map<String, SegmentResult>> baseRuleResultsState;
    
    public DerivedRuleProcessFunction(MapStateDescriptor<String, SegmentRule> ruleStateDescriptor) {
        this.ruleStateDescriptor = ruleStateDescriptor;
    }
    
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        
        ValueStateDescriptor<Map<String, SegmentResult>> resultsDescriptor = new ValueStateDescriptor<>(
                "baseRuleResults",
                TypeInformation.of(new org.apache.flink.api.common.typeinfo.TypeHint<Map<String, SegmentResult>>() {})
        );
        baseRuleResultsState = getRuntimeContext().getState(resultsDescriptor);
    }
    
    @Override
    public void processElement(SegmentResult baseResult, ReadOnlyContext ctx, Collector<SegmentResult> out) throws Exception {
        LOG.debug("Processing base result: {}", baseResult);
        
        // Get current base results for this user
        Map<String, SegmentResult> userBaseResults = baseRuleResultsState.value();
        if (userBaseResults == null) {
            userBaseResults = new HashMap<>();
        }
        
        // Update the base result (use segmentId as key for now, in reality might need ruleId)
        userBaseResults.put(baseResult.getSegmentId(), baseResult);
        
        // Clean old results (keep only recent ones within reasonable time window)
        long currentTime = baseResult.getTimestamp();
        long maxAge = 60 * 60 * 1000; // 1 hour
        userBaseResults.entrySet().removeIf(entry -> 
                (currentTime - entry.getValue().getTimestamp()) > maxAge);
        
        baseRuleResultsState.update(userBaseResults);
        
        // Get all broadcast rules to check for derived rules
        org.apache.flink.api.common.state.ReadOnlyBroadcastState<String, SegmentRule> broadcastState = ctx.getBroadcastState(ruleStateDescriptor);
        
        // Check if this base result triggers any derived rules
        for (Map.Entry<String, SegmentRule> entry : broadcastState.immutableEntries()) {
            SegmentRule rule = entry.getValue();
            
            // Skip base rules
            if (!isDerivedRule(rule)) {
                continue;
            }
            
            // Evaluate derived rule
            SegmentResult derivedResult = evaluateDerivedRule(baseResult.getUserId(), rule, userBaseResults, currentTime);
            if (derivedResult != null && derivedResult.getQualified()) {
                LOG.info("Derived rule qualified: {}", derivedResult);
                out.collect(derivedResult);
            }
        }
    }
    
    @Override
    public void processBroadcastElement(SegmentRule rule, Context ctx, Collector<SegmentResult> out) throws Exception {
        LOG.info("Received broadcast rule for derived processing: {}", rule);
        ctx.getBroadcastState(ruleStateDescriptor).put(rule.getId(), rule);
    }
    
    private boolean isDerivedRule(SegmentRule rule) {
        // For this demo, we'll identify derived rules by naming convention
        // In reality, you'd have proper metadata or dependencies field
        // Let's assume derived rules have segment IDs starting with "derived_"
        return rule.getSegmentId() != null && rule.getSegmentId().startsWith("derived_");
    }
    
    private SegmentResult evaluateDerivedRule(String userId, SegmentRule derivedRule, 
                                            Map<String, SegmentResult> baseResults, long currentTime) {
        try {
            // For demo purposes, let's implement a simple AND logic between two base segments
            // In reality, you'd parse the rule dependencies and implement proper logic
            
            // Example: derived rule "high_clickers_and_installers" requires both 
            // "high_clickers" and "high_installers" base segments to be qualified
            
            String[] requiredSegments = getDerivedRuleDependencies(derivedRule);
            if (requiredSegments.length < 2) {
                LOG.warn("Derived rule {} has insufficient dependencies", derivedRule.getId());
                return null;
            }
            
            // Check that all required base segments are qualified and within the same window
            boolean allQualified = true;
            double combinedValue = 0.0;
            long minWindowStart = Long.MAX_VALUE;
            long maxWindowEnd = Long.MIN_VALUE;
            
            for (String requiredSegment : requiredSegments) {
                SegmentResult baseResult = baseResults.get(requiredSegment);
                
                if (baseResult == null || !baseResult.getQualified()) {
                    allQualified = false;
                    break;
                }
                
                // Check if window sizes match (requirement from the specification)
                if (!windowSizesMatch(derivedRule, baseResult)) {
                    allQualified = false;
                    break;
                }
                
                combinedValue += baseResult.getValue();
                minWindowStart = Math.min(minWindowStart, baseResult.getWindowStart());
                maxWindowEnd = Math.max(maxWindowEnd, baseResult.getWindowEnd());
            }
            
            if (!allQualified) {
                return null;
            }
            
            LOG.debug("Derived rule {} qualified for user {} with combined value {}", 
                    derivedRule.getId(), userId, combinedValue);
            
            return new SegmentResult(
                    userId,
                    derivedRule.getSegmentId(),
                    derivedRule.getId(),
                    true, // Already checked qualification
                    combinedValue,
                    currentTime,
                    minWindowStart,
                    maxWindowEnd
            );
            
        } catch (Exception e) {
            LOG.error("Failed to evaluate derived rule {} for user {}", derivedRule.getId(), userId, e);
            return null;
        }
    }
    
    private String[] getDerivedRuleDependencies(SegmentRule derivedRule) {
        // For demo, hardcode some dependencies based on naming
        // In reality, this would come from the rule metadata or API
        String segmentId = derivedRule.getSegmentId();
        
        if ("derived_high_clickers_and_installers".equals(segmentId)) {
            return new String[]{"high_clickers", "high_installers"};
        } else if ("derived_active_purchasers".equals(segmentId)) {
            return new String[]{"high_activity", "high_purchasers"};
        }
        
        // Default: return empty array
        return new String[0];
    }
    
    private boolean windowSizesMatch(SegmentRule derivedRule, SegmentResult baseResult) {
        // Check if the window sizes match
        // For derived rules, we require that all base rules have the same window size
        // This is a simplification - in reality you might have more complex logic
        
        long derivedWindowMs = derivedRule.getWindowMinutes() * 60L * 1000L;
        long baseWindowMs = baseResult.getWindowEnd() - baseResult.getWindowStart();
        
        // Allow some tolerance for window matching (within 5% difference)
        double tolerance = 0.05;
        double diff = Math.abs(derivedWindowMs - baseWindowMs) / (double) derivedWindowMs;
        
        return diff <= tolerance;
    }
}