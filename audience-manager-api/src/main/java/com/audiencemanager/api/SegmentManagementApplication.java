package com.audiencemanager.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for the Audience Manager API.
 * Provides REST endpoints for segment management and metadata operations.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
@EnableCaching

@EnableConfigurationProperties
public class SegmentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SegmentManagementApplication.class, args);
    }
}