package com.audiencemanager.api.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing segment rules for independent segments.
 * Each rule defines a condition: event_type.attribute operator value
 */
@Entity
@Table(name = "segment_rules", indexes = {
        @Index(name = "idx_segment_rules_segment_id", columnList = "segment_id"),
        @Index(name = "idx_segment_rules_event_type", columnList = "event_type")
})
public class SegmentRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", nullable = false)
    private SegmentEntity segment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String attribute;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Operator operator;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal value;

    @Positive
    @Column(name = "window_minutes", nullable = false)
    private Integer windowMinutes = 5;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Enums
    public enum EventType {
        clicks, installs, orders, addToCart
    }

    public enum Operator {
        GT(">"), LT("<"), GTE(">="), LTE("<="), EQ("="), NEQ("!=");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public static Operator fromSymbol(String symbol) {
            for (Operator op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown operator: " + symbol);
        }
    }

    // Constructors
    public SegmentRuleEntity() {}

    public SegmentRuleEntity(EventType eventType, String attribute, Operator operator, 
                            BigDecimal value, Integer windowMinutes) {
        this.eventType = eventType;
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
        this.windowMinutes = windowMinutes;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public SegmentEntity getSegment() {
        return segment;
    }

    public void setSegment(SegmentEntity segment) {
        this.segment = segment;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SegmentRuleEntity)) return false;
        SegmentRuleEntity that = (SegmentRuleEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SegmentRuleEntity{" +
                "id=" + id +
                ", eventType=" + eventType +
                ", attribute='" + attribute + '\'' +
                ", operator=" + operator +
                ", value=" + value +
                ", windowMinutes=" + windowMinutes +
                '}';
    }
}