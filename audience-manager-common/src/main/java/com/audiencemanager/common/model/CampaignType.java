package com.audiencemanager.common.model;

/**
 * Enumeration of different campaign types for multi-channel communication.
 */
public enum CampaignType {
    /**
     * Email marketing campaigns
     */
    EMAIL,

    /**
     * SMS marketing campaigns
     */
    SMS,

    /**
     * Push notification campaigns
     */
    PUSH_NOTIFICATION,

    /**
     * In-app message campaigns
     */
    IN_APP_MESSAGE,

    /**
     * Multi-channel campaigns combining multiple types
     */
    MULTI_CHANNEL
}