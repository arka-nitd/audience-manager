package com.audiencemanager.api.repository;

import com.audiencemanager.api.entity.SegmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SegmentEntity operations.
 * Provides data access methods for segment management.
 */
@Repository
public interface SegmentRepository extends JpaRepository<SegmentEntity, UUID> {

    /**
     * Find segment by name (case-insensitive)
     */
    Optional<SegmentEntity> findByNameIgnoreCase(String name);

    /**
     * Check if segment with name exists (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find all segments by type
     */
    List<SegmentEntity> findByType(SegmentEntity.SegmentCategory type);

    /**
     * Find all segments by segment type
     */
    List<SegmentEntity> findBySegmentType(SegmentEntity.SegmentType segmentType);

    /**
     * Find all active segments
     */
    List<SegmentEntity> findByActiveTrue();

    /**
     * Find all inactive segments
     */
    List<SegmentEntity> findByActiveFalse();

    /**
     * Find segments by type and active status
     */
    Page<SegmentEntity> findByTypeAndActive(SegmentEntity.SegmentCategory type, Boolean active, Pageable pageable);

    /**
     * Find segments by segment type and active status
     */
    Page<SegmentEntity> findBySegmentTypeAndActive(SegmentEntity.SegmentType segmentType, Boolean active, Pageable pageable);

    /**
     * Find all independent segments (for use in derived segments)
     */
    @Query("SELECT s FROM SegmentEntity s WHERE s.type = :type AND s.active = true ORDER BY s.name")
    List<SegmentEntity> findActiveIndependentSegments(@Param("type") SegmentEntity.SegmentCategory type);

    /**
     * Find segments with rules containing specific event type
     */
    @Query("SELECT DISTINCT s FROM SegmentEntity s JOIN s.rules r WHERE r.eventType = :eventType AND s.active = true")
    List<SegmentEntity> findByEventType(@Param("eventType") String eventType);

    /**
     * Find segments that depend on a specific independent segment
     */
    @Query("SELECT DISTINCT d.derivedSegment FROM SegmentDependencyEntity d WHERE d.independentSegment.id = :independentSegmentId")
    List<SegmentEntity> findDerivedSegmentsByIndependentSegment(@Param("independentSegmentId") UUID independentSegmentId);

    /**
     * Count segments by type
     */
    long countByType(SegmentEntity.SegmentCategory type);

    /**
     * Count active segments
     */
    long countByActiveTrue();

    /**
     * Search segments by name or description (case-insensitive)
     */
    @Query("SELECT s FROM SegmentEntity s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<SegmentEntity> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find segments created within a date range
     */
    @Query("SELECT s FROM SegmentEntity s WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate ORDER BY s.createdAt DESC")
    List<SegmentEntity> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                              @Param("endDate") java.time.LocalDateTime endDate);
}