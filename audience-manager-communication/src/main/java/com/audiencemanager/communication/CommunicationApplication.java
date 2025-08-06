package com.audiencemanager.communication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the Communication Service.
 * This service handles multi-channel communication and campaign management for audience segments.
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
@ComponentScan(basePackages = {
    "com.audiencemanager.communication",
    "com.audiencemanager.common"
})
public class CommunicationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunicationApplication.class, args);
    }
}