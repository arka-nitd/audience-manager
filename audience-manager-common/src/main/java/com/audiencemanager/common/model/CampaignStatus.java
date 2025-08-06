package com.audiencemanager.common.model;

/**
 * Enumeration of campaign statuses throughout the campaign lifecycle.
 */
public enum CampaignStatus {
    /**
     * Campaign is being created and configured
     */
    DRAFT,

    /**
     * Campaign is scheduled to be sent at a future time
     */
    SCHEDULED,

    /**
     * Campaign is currently being processed and sent
     */
    SENDING,

    /**
     * Campaign has been successfully sent to all recipients
     */
    SENT,

    /**
     * Campaign was paused during sending
     */
    PAUSED,

    /**
     * Campaign was cancelled before completion
     */
    CANCELLED,

    /**
     * Campaign failed due to an error
     */
    FAILED,

    /**
     * Campaign completed successfully
     */
    COMPLETED
}