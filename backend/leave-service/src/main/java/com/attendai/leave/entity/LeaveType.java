package com.attendai.leave.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Configurable leave type catalogue (Annual, Sick, Casual, Maternity, etc.).
 * HR/Admin creates and manages these.
 */
@Getter
@Setter
@Entity
@Table(name = "leave_types")
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(length = 500)
    private String description;

    /** Default annual quota for this leave type (days). */
    @Column(nullable = false)
    private int defaultDays;

    /** If true, unused days can be carried over to the next year. */
    @Column(nullable = false)
    private boolean carryOver = false;

    /** If true, employees can request more days than their balance (goes negative). */
    @Column(nullable = false)
    private boolean allowNegative = false;

    /** If true, a supporting document is required for this leave type. */
    @Column(nullable = false)
    private boolean requiresDocument = false;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
