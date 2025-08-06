package com.audiencemanager.api.repository;

import com.audiencemanager.api.entity.SegmentRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for SegmentRuleEntity operations.
 */
@Repository
public interface SegmentRuleRepository extends JpaRepository<SegmentRuleEntity, UUID> {

    /**
     * Find all rules for a specific segment
     */
    List<SegmentRuleEntity> findBySegmentId(UUID segmentId);

    /**
     * Find all active rules for a specific segment
     */
    List<SegmentRuleEntity> findBySegmentIdAndActiveTrue(UUID segmentId);

    /**
     * Find rules by event type
     */
    List<SegmentRuleEntity> findByEventType(SegmentRuleEntity.EventType eventType);

    /**
     * Find rules by event type and active status
     */
    List<SegmentRuleEntity> findByEventTypeAndActive(SegmentRuleEntity.EventType eventType, Boolean active);

    /**
     * Count rules for a segment
     */
    long countBySegmentId(UUID segmentId);

    /**
     * Count active rules for a segment
     */
    long countBySegmentIdAndActiveTrue(UUID segmentId);

    /**
     * Delete all rules for a segment
     */
    void deleteBySegmentId(UUID segmentId);

    /**
     * Find rules with specific operator
     */
    List<SegmentRuleEntity> findByOperator(SegmentRuleEntity.Operator operator);

    /**
     * Find rules within a time window range
     */
    @Query("SELECT r FROM SegmentRuleEntity r WHERE r.windowMinutes BETWEEN :minWindow AND :maxWindow")
    List<SegmentRuleEntity> findByWindowMinutesBetween(@Param("minWindow") Integer minWindow, 
                                                       @Param("maxWindow") Integer maxWindow);
}