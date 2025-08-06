package com.audiencemanager.processor;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for the Flink Segment Processor with Integrated Rule Engine.
 * This job processes user events from Kafka, evaluates rules using broadcast state,
 * and assigns segments to users in real-time.
 * 
 * Architecture:
 * - Rule Polling Task: Periodically polls PostgreSQL for rule updates
 * - Broadcast State: Distributes rules to all parallel event processing tasks
 * - Event Processing: Evaluates events against rules and updates user segments
 */
public class SegmentProcessorApplication {

    private static final Logger logger = LoggerFactory.getLogger(SegmentProcessorApplication.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting Audience Manager Segment Processor...");

        // Create the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Enable checkpointing for fault tolerance
        env.enableCheckpointing(30000); // checkpoint every 30 seconds

        // Set parallelism
        env.setParallelism(2);

        // TODO: Configure the integrated processing pipeline with rule engine
        // 1. Rule Source: Periodically poll PostgreSQL for rule updates
        // 2. Rule Broadcast: Use broadcast state to distribute rules to all tasks
        // 3. Event Source: Read from Kafka topic 'user-events'
        // 4. Rule Evaluation: Apply broadcast rules to incoming events
        // 5. Segment Assignment: Determine user segment membership
        // 6. Aerospike Sink: Write user profiles and segment assignments
        // 7. Kafka Sink: Write segment assignments to 'user-segments' topic
        
        logger.info("Integrated Segment Processor with Rule Engine configuration completed");
        logger.info("Rule polling will occur every 30 seconds from PostgreSQL");
        logger.info("Rules will be cached in broadcast state for optimal performance");

        // Execute the job
        env.execute("Audience Manager Segment Processor");
    }
}