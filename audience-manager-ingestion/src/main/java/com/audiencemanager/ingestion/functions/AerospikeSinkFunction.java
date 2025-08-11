package com.audiencemanager.ingestion.functions;

import com.aerospike.client.*;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.audiencemanager.ingestion.model.SegmentResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sink function for writing segment results to Aerospike
 */
public class AerospikeSinkFunction extends RichSinkFunction<SegmentResult> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AerospikeSinkFunction.class);
    private static final long serialVersionUID = 1L;
    
    private final String host;
    private final int port;
    private final String namespace;
    private final String setName;
    
    private transient AerospikeClient client;
    private transient WritePolicy writePolicy;
    private transient ObjectMapper objectMapper;
    
    public AerospikeSinkFunction(String host, int port, String namespace, String setName) {
        this.host = host;
        this.port = port;
        this.namespace = namespace;
        this.setName = setName;
    }
    
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        
        LOG.info("Connecting to Aerospike at {}:{}, namespace: {}, set: {}", 
                host, port, namespace, setName);
        
        // Initialize Aerospike client
        ClientPolicy policy = new ClientPolicy();
        policy.timeout = 3000; // 3 seconds
        
        client = new AerospikeClient(policy, host, port);
        
        // Initialize write policy
        writePolicy = new WritePolicy();
        writePolicy.socketTimeout = 1000; // 1 second
        writePolicy.expiration = 3600; // 1 hour TTL as specified
        
        // Initialize Jackson mapper
        objectMapper = new ObjectMapper();
        
        LOG.info("Successfully connected to Aerospike");
    }
    
    @Override
    public void invoke(SegmentResult result, Context context) throws Exception {
        try {
            Key key = new Key(namespace, setName, result.getUserId());
            
            // Read existing record to merge with new result
            Record existingRecord = client.get(null, key);
            
            Map<String, Object> userData = new HashMap<>();
            List<String> qualifiedSegments = new ArrayList<>();
            Map<String, Object> ruleResults = new HashMap<>();
            
            // If record exists, read current data
            if (existingRecord != null) {
                Object segments = existingRecord.getValue("qualifiedSegments");
                if (segments instanceof List) {
                    qualifiedSegments = new ArrayList<>((List<String>) segments);
                }
                
                Object rules = existingRecord.getValue("ruleResults");
                if (rules instanceof Map) {
                    ruleResults = new HashMap<>((Map<String, Object>) rules);
                }
            }
            
            // Add/update the new segment if qualified
            if (result.getQualified()) {
                if (!qualifiedSegments.contains(result.getSegmentId())) {
                    qualifiedSegments.add(result.getSegmentId());
                }
                
                // Store detailed rule result
                Map<String, Object> ruleDetail = new HashMap<>();
                ruleDetail.put("qualified", result.getQualified());
                ruleDetail.put("value", result.getValue());
                ruleDetail.put("timestamp", result.getTimestamp());
                ruleDetail.put("windowStart", result.getWindowStart());
                ruleDetail.put("windowEnd", result.getWindowEnd());
                
                ruleResults.put(result.getRuleId(), ruleDetail);
            } else {
                // Remove segment if no longer qualified
                qualifiedSegments.remove(result.getSegmentId());
                ruleResults.remove(result.getRuleId());
            }
            
            // Prepare bins for Aerospike
            Bin userIdBin = new Bin("userId", result.getUserId());
            Bin segmentsBin = new Bin("qualifiedSegments", qualifiedSegments);
            Bin lastUpdatedBin = new Bin("lastUpdated", result.getTimestamp());
            Bin ruleResultsBin = new Bin("ruleResults", ruleResults);
            
            // Write to Aerospike
            client.put(writePolicy, key, userIdBin, segmentsBin, lastUpdatedBin, ruleResultsBin);
            
            LOG.debug("Successfully wrote segment result for user {} to Aerospike: segment={}, qualified={}", 
                    result.getUserId(), result.getSegmentId(), result.getQualified());
            
        } catch (AerospikeException e) {
            LOG.error("Failed to write segment result to Aerospike for user {}: {}", 
                    result.getUserId(), e.getMessage(), e);
            // Depending on your error handling strategy, you might want to:
            // - Throw the exception to fail the job
            // - Store failed writes for retry
            // - Continue processing (current approach)
        } catch (Exception e) {
            LOG.error("Unexpected error writing to Aerospike for user {}", result.getUserId(), e);
        }
    }
    
    @Override
    public void close() throws Exception {
        super.close();
        if (client != null) {
            LOG.info("Closing Aerospike client");
            client.close();
        }
    }
}