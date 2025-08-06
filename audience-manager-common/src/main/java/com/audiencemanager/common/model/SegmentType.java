package com.audiencemanager.common.model;

/**
 * Enumeration of different segment types supported by the platform.
 */
public enum SegmentType {
    /**
     * Static segments with predefined user lists
     */
    STATIC,

    /**
     * Dynamic segments based on real-time behavior rules
     */
    DYNAMIC,

    /**
     * Computed segments based on machine learning models
     */
    COMPUTED,

    /**
     * Lookalike segments based on similarity to seed audiences
     */
    LOOKALIKE;

    /**
     * Enumeration for distinguishing between independent and derived segments
     */
    public enum SegmentCategory {
        /**
         * Independent segments based on single rule conditions
         */
        INDEPENDENT,
        
        /**
         * Derived segments combining multiple independent segments
         */
        DERIVED
    }
}