package com.audiencemanager.api.exception;

/**
 * Exception thrown when segment validation fails.
 */
public class SegmentValidationException extends RuntimeException {

    public SegmentValidationException(String message) {
        super(message);
    }

    public SegmentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}