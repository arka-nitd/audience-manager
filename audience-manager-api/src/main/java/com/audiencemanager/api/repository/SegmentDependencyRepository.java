package com.audiencemanager.api.repository;

import com.audiencemanager.api.entity.SegmentDependencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for SegmentDependencyEntity operations.
 */
@Repository
public interface SegmentDependencyRepository extends JpaRepository<SegmentDependencyEntity, UUID> {

    /**
     * Find all dependencies for a derived segment
     */
    List<SegmentDependencyEntity> findByDerivedSegmentId(UUID derivedSegmentId);

    /**
     * Find all segments that depend on an independent segment
     */
    List<SegmentDependencyEntity> findByIndependentSegmentId(UUID independentSegmentId);

    /**
     * Check if a dependency exists between two segments
     */
    boolean existsByDerivedSegmentIdAndIndependentSegmentId(UUID derivedSegmentId, UUID independentSegmentId);

    /**
     * Count dependencies for a derived segment
     */
    long countByDerivedSegmentId(UUID derivedSegmentId);

    /**
     * Count how many derived segments depend on an independent segment
     */
    long countByIndependentSegmentId(UUID independentSegmentId);

    /**
     * Delete all dependencies for a derived segment
     */
    void deleteByDerivedSegmentId(UUID derivedSegmentId);

    /**
     * Delete dependencies where a segment is used as independent
     */
    void deleteByIndependentSegmentId(UUID independentSegmentId);

    /**
     * Find dependencies by logical operator
     */
    List<SegmentDependencyEntity> findByLogicalOperator(SegmentDependencyEntity.LogicalOperator operator);

    /**
     * Find circular dependencies (prevent derived segments from depending on other derived segments)
     */
    @Query("SELECT d FROM SegmentDependencyEntity d " +
           "WHERE d.independentSegment.type = com.audiencemanager.api.entity.SegmentEntity$SegmentCategory.DERIVED")
    List<SegmentDependencyEntity> findInvalidDependencies();

    /**
     * Get all independent segments used by a derived segment with their operators
     */
    @Query("SELECT d FROM SegmentDependencyEntity d " +
           "JOIN FETCH d.independentSegment " +
           "WHERE d.derivedSegment.id = :derivedSegmentId " +
           "ORDER BY d.createdAt")
    List<SegmentDependencyEntity> findDependenciesWithSegments(@Param("derivedSegmentId") UUID derivedSegmentId);
}