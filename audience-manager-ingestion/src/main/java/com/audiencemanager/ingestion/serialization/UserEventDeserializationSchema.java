package com.audiencemanager.ingestion.serialization;

import com.audiencemanager.ingestion.model.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Deserialization schema for UserEvent from Kafka
 */
public class UserEventDeserializationSchema implements DeserializationSchema<UserEvent> {
    
    private static final Logger LOG = LoggerFactory.getLogger(UserEventDeserializationSchema.class);
    private static final long serialVersionUID = 1L;
    
    private transient ObjectMapper objectMapper;
    
    @Override
    public void open(InitializationContext context) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public UserEvent deserialize(byte[] message) throws IOException {
        try {
            UserEvent event = objectMapper.readValue(message, UserEvent.class);
            LOG.debug("Deserialized event: {}", event);
            return event;
        } catch (Exception e) {
            LOG.error("Failed to deserialize UserEvent from: {}", new String(message), e);
            // Return null or throw exception based on your error handling strategy
            return null;
        }
    }
    
    @Override
    public boolean isEndOfStream(UserEvent nextElement) {
        return false;
    }
    
    @Override
    public TypeInformation<UserEvent> getProducedType() {
        return TypeInformation.of(UserEvent.class);
    }
}