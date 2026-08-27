package com.attendai.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Records attendance anomalies for admin review.
 * Anomalies are never silently ignored or auto-resolved.
 */
@Getter
@Setter
@Entity
@Table(name = "attendance_anomalies",
       indexes = @Index(name = "idx_anomaly_employee", columnList = "employee_id"))
public class AttendanceAnomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /** Optional reference to an attendance record. */
    @Column(name = "attendance_id")
    private Long attendanceId;

    /**
     * Issue type in SCREAMING_SNAKE_CASE.
     * Examples: DUPLICATE_CHECK_IN, OUTSIDE_GEOFENCE, EXPIRED_QR, QR_REPLAY,
     *           FACE_MISMATCH, INVALID_STATE_TRANSITION, MISSING_CHECKOUT, OVERLAPPING_SESSION
     */
    @Column(nullable = false, length = 80)
    private String issueType;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private boolean resolved = false;

    @Column(length = 500)
    private String resolutionNote;

    @Column
    private Long resolvedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
