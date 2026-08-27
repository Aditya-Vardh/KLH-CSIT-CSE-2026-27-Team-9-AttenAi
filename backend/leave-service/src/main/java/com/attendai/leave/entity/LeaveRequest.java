package com.attendai.leave.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** References employee-service Employee.id — no cross-service FK. */
    @Column(nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    /** Computed business days (Mon–Fri) between startDate and endDate inclusive. */
    @Column(nullable = false)
    private int totalDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(length = 1000)
    private String reason;

    /** HR / Manager comment added during approval or rejection. */
    @Column(length = 1000)
    private String reviewComment;

    /** userId of the HR/Manager who reviewed this request. */
    @Column
    private Long reviewedBy;

    @Column
    private Instant reviewedAt;

    /** URL or path to supporting document (for leave types that require it). */
    @Column(length = 500)
    private String documentUrl;

    /** Set by AI agent when auto-recommending leave approval. */
    @Column(nullable = false)
    private boolean aiRecommended = false;

    /** AI confidence score [0.0 – 1.0] when aiRecommended = true. */
    @Column
    private Double aiConfidenceScore;

    /** AI recommendation: APPROVE or REJECT. */
    @Column(length = 20)
    private String aiRecommendation;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
