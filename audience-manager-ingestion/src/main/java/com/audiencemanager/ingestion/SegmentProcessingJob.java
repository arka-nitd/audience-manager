package com.audiencemanager.ingestion;

import com.audiencemanager.ingestion.functions.SegmentRuleBroadcastFunction;
import com.audiencemanager.ingestion.functions.SegmentRuleProcessFunction;
import com.audiencemanager.ingestion.functions.DerivedRuleProcessFunction;
import com.audiencemanager.ingestion.functions.AerospikeSinkFunction;
import com.audiencemanager.ingestion.model.UserEvent;
import com.audiencemanager.ingestion.model.SegmentRule;
import com.audiencemanager.ingestion.model.SegmentResult;
import com.audiencemanager.ingestion.serialization.UserEventDeserializationSchema;
import com.audiencemanager.ingestion.serialization.SegmentRuleDeserializationSchema;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;

/**
 * Flink job for processing user events and evaluating segment rules
 */
public class SegmentProcessingJob {
    
    private static final Logger LOG = LoggerFactory.getLogger(SegmentProcessingJob.class);
    
    // Kafka Configuration
    private static final String KAFKA_BOOTSTRAP_SERVERS = "kafka-service:9092";
    private static final String[] EVENT_TOPICS = {"order_events", "activity_events", "install_events"};
    private static final String RULES_TOPIC = "segment_rules";
    
    // Aerospike Configuration
    private static final String AEROSPIKE_HOST = "aerospike-service";
    private static final int AEROSPIKE_PORT = 3000;
    private static final String AEROSPIKE_NAMESPACE = "segments";
    private static final String AEROSPIKE_SET = "user_segments";
    
    // API Configuration
    private static final String API_BASE_URL = "http://audience-manager-api-service:8080/api/v1";
    
    public static void main(String[] args) throws Exception {
        LOG.info("Starting Segment Processing Job...");
        
        // Set up the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Enable checkpointing
        env.enableCheckpointing(60000); // 1 minute checkpoints
        
        // Set parallelism
        env.setParallelism(2);
        
        // Configure watermark strategy
        WatermarkStrategy<UserEvent> watermarkStrategy = WatermarkStrategy
                .<UserEvent>forBoundedOutOfOrderness(Duration.ofSeconds(30))
                .withTimestampAssigner((event, timestamp) -> event.getTimestamp());
        
        // Create Kafka source for user events
        KafkaSource<UserEvent> eventSource = KafkaSource.<UserEvent>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                .setTopics(Arrays.asList(EVENT_TOPICS))
                .setGroupId("segment-processing-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new UserEventDeserializationSchema())
                .build();
        
        // Create event stream
        DataStream<UserEvent> eventStream = env
                .fromSource(eventSource, watermarkStrategy, "Event Source")
                .name("User Events");
        
        // Create broadcast stream for segment rules
        MapStateDescriptor<String, SegmentRule> ruleStateDescriptor = new MapStateDescriptor<>(
                "SegmentRules",
                TypeInformation.of(String.class),
                TypeInformation.of(SegmentRule.class)
        );
        
        // Broadcast segment rules from API (periodic fetch)
        DataStream<SegmentRule> rulesStream = env
                .addSource(new SegmentRuleBroadcastFunction(API_BASE_URL), "Rules Source")
                .name("Segment Rules");
        
        BroadcastStream<SegmentRule> rulesBroadcast = rulesStream.broadcast(ruleStateDescriptor);
        
        // Process base rules
        DataStream<SegmentResult> baseRuleResults = eventStream
                .keyBy(UserEvent::getUserId)
                .connect(rulesBroadcast)
                .process(new SegmentRuleProcessFunction(ruleStateDescriptor))
                .name("Base Rule Processing");
        
        // Process derived rules
        DataStream<SegmentResult> derivedRuleResults = baseRuleResults
                .keyBy(SegmentResult::getUserId)
                .connect(rulesBroadcast)
                .process(new DerivedRuleProcessFunction(ruleStateDescriptor))
                .name("Derived Rule Processing");
        
        // Union base and derived results
        DataStream<SegmentResult> allResults = baseRuleResults.union(derivedRuleResults);
        
        // Filter only qualified results and write to Aerospike
        allResults
                .filter(result -> result.getQualified())
                .addSink(new AerospikeSinkFunction(AEROSPIKE_HOST, AEROSPIKE_PORT, AEROSPIKE_NAMESPACE, AEROSPIKE_SET))
                .name("Aerospike Sink");
        
        // Log all results for monitoring
        allResults.print("SegmentResults");
        
        LOG.info("Executing Segment Processing Job...");
        env.execute("Segment Processing Job");
    }
}