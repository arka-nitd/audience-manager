package com.audiencemanager.api.entity;


import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity representing a segment in the database.
 * Supports both independent and derived segments.
 */
@Entity
@Table(name = "segments", indexes = {
        @Index(name = "idx_segments_type", columnList = "type"),
        @Index(name = "idx_segments_active", columnList = "active"),
        @Index(name = "idx_segments_name", columnList = "name", unique = true)
})
public class SegmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SegmentCategory type;

    // Enums
    public enum SegmentCategory {
        INDEPENDENT, DERIVED
    }

    @Column(name = "logical_expression", columnDefinition = "TEXT")
    private String logicalExpression;

    @Positive
    @Column(name = "window_minutes")
    private Integer windowMinutes = 5;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "segment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SegmentRuleEntity> rules = new ArrayList<>();

    @OneToMany(mappedBy = "derivedSegment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SegmentDependencyEntity> dependencies = new ArrayList<>();

    // Constructors
    public SegmentEntity() {}

    public SegmentEntity(String name, String description, SegmentCategory type, Integer windowMinutes) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.windowMinutes = windowMinutes;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SegmentCategory getType() {
        return type;
    }

    public void setType(SegmentCategory type) {
        this.type = type;
    }



    public String getLogicalExpression() {
        return logicalExpression;
    }

    public void setLogicalExpression(String logicalExpression) {
        this.logicalExpression = logicalExpression;
    }

    public Integer getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(Integer windowMinutes) {
        this.windowMinutes = windowMinutes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<SegmentRuleEntity> getRules() {
        return rules;
    }

    public void setRules(List<SegmentRuleEntity> rules) {
        this.rules = rules;
    }

    public List<SegmentDependencyEntity> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<SegmentDependencyEntity> dependencies) {
        this.dependencies = dependencies;
    }

    // Helper methods
    public void addRule(SegmentRuleEntity rule) {
        rules.add(rule);
        rule.setSegment(this);
    }

    public void removeRule(SegmentRuleEntity rule) {
        rules.remove(rule);
        rule.setSegment(null);
    }

    public void addDependency(SegmentDependencyEntity dependency) {
        dependencies.add(dependency);
        dependency.setDerivedSegment(this);
    }

    public void removeDependency(SegmentDependencyEntity dependency) {
        dependencies.remove(dependency);
        dependency.setDerivedSegment(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SegmentEntity)) return false;
        SegmentEntity that = (SegmentEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SegmentEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", active=" + active +
                '}';
    }
}