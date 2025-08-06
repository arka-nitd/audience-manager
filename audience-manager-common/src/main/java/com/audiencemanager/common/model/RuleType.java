package com.audiencemanager.common.model;

/**
 * Enumeration of different rule types supported by the rule engine.
 */
public enum RuleType {
    /**
     * Behavioral rules based on user actions and events
     */
    BEHAVIORAL,

    /**
     * Demographic rules based on user profile attributes
     */
    DEMOGRAPHIC,

    /**
     * Geographic rules based on location data
     */
    GEOGRAPHIC,

    /**
     * Temporal rules based on time-based conditions
     */
    TEMPORAL,

    /**
     * Custom rules with user-defined logic
     */
    CUSTOM,

    /**
     * Composite rules that combine multiple rule types
     */
    COMPOSITE
}