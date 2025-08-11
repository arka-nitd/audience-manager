package com.audiencemanager.ingestion;

/**
 * This module has been converted to use the Flink streaming job.
 * The main entry point is now SegmentProcessingJob.java
 * 
 * @deprecated Use SegmentProcessingJob instead
 */
@Deprecated
public class EventIngestionApplication {

    public static void main(String[] args) {
        System.out.println("This application has been converted to Flink.");
        System.out.println("Please use SegmentProcessingJob instead.");
        System.out.println("Run: java -cp target/audience-manager-ingestion-1.0-SNAPSHOT.jar com.audiencemanager.ingestion.SegmentProcessingJob");
    }
}