package com.audiencemanager.ingestion.serialization;

import com.audiencemanager.ingestion.model.SegmentRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Deserialization schema for SegmentRule from Kafka or API
 */
public class SegmentRuleDeserializationSchema implements DeserializationSchema<SegmentRule> {
    
    private static final Logger LOG = LoggerFactory.getLogger(SegmentRuleDeserializationSchema.class);
    private static final long serialVersionUID = 1L;
    
    private transient ObjectMapper objectMapper;
    
    @Override
    public void open(InitializationContext context) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public SegmentRule deserialize(byte[] message) throws IOException {
        try {
            SegmentRule rule = objectMapper.readValue(message, SegmentRule.class);
            LOG.debug("Deserialized rule: {}", rule);
            return rule;
        } catch (Exception e) {
            LOG.error("Failed to deserialize SegmentRule from: {}", new String(message), e);
            return null;
        }
    }
    
    @Override
    public boolean isEndOfStream(SegmentRule nextElement) {
        return false;
    }
    
    @Override
    public TypeInformation<SegmentRule> getProducedType() {
        return TypeInformation.of(SegmentRule.class);
    }
}