package com.audiencemanager.api.exception;

/**
 * Exception thrown when a segment is not found.
 */
public class SegmentNotFoundException extends RuntimeException {

    public SegmentNotFoundException(String message) {
        super(message);
    }

    public SegmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}