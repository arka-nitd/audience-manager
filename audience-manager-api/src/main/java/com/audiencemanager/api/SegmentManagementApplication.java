package com.audiencemanager.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for the Segment Management API.
 * This service handles CRUD operations for audience segments and metadata management.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.audiencemanager.api",
    "com.audiencemanager.common"
})
public class SegmentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SegmentManagementApplication.class, args);
    }
}