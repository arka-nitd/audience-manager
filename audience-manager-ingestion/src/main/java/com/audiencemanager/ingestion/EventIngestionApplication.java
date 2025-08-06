package com.audiencemanager.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main application class for the Event Ingestion Service.
 * This service handles high-throughput ingestion of user events into Kafka.
 */
@SpringBootApplication
@EnableKafka
@ComponentScan(basePackages = {
    "com.audiencemanager.ingestion",
    "com.audiencemanager.common"
})
public class EventIngestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventIngestionApplication.class, args);
    }
}