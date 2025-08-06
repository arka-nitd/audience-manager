package com.audiencemanager.api.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing dependencies between derived and independent segments.
 * Tracks which independent segments are used in derived segment expressions.
 */
@Entity
@Table(name = "segment_dependencies", 
       indexes = {
           @Index(name = "idx_segment_dependencies_derived", columnList = "derived_segment_id"),
           @Index(name = "idx_segment_dependencies_independent", columnList = "independent_segment_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_segment_dependencies", 
                           columnNames = {"derived_segment_id", "independent_segment_id"})
       })
public class SegmentDependencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "derived_segment_id", nullable = false)
    private SegmentEntity derivedSegment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "independent_segment_id", nullable = false)
    private SegmentEntity independentSegment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "logical_operator", nullable = false, length = 10)
    private LogicalOperator logicalOperator;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Enum for logical operators
    public enum LogicalOperator {
        AND, OR, NOT
    }

    // Constructors
    public SegmentDependencyEntity() {}

    public SegmentDependencyEntity(SegmentEntity derivedSegment, SegmentEntity independentSegment, 
                                  LogicalOperator logicalOperator) {
        this.derivedSegment = derivedSegment;
        this.independentSegment = independentSegment;
        this.logicalOperator = logicalOperator;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public SegmentEntity getDerivedSegment() {
        return derivedSegment;
    }

    public void setDerivedSegment(SegmentEntity derivedSegment) {
        this.derivedSegment = derivedSegment;
    }

    public SegmentEntity getIndependentSegment() {
        return independentSegment;
    }

    public void setIndependentSegment(SegmentEntity independentSegment) {
        this.independentSegment = independentSegment;
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SegmentDependencyEntity)) return false;
        SegmentDependencyEntity that = (SegmentDependencyEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SegmentDependencyEntity{" +
                "id=" + id +
                ", logicalOperator=" + logicalOperator +
                ", derivedSegmentId=" + (derivedSegment != null ? derivedSegment.getId() : "null") +
                ", independentSegmentId=" + (independentSegment != null ? independentSegment.getId() : "null") +
                '}';
    }
}