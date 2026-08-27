package com.attendai.employee.entity;

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

/**
 * Immutable audit log entry. Once written it is never updated or deleted.
 * Covers important actions across the system (employee CRUD, department changes,
 * leave approvals, role changes). Recorded by whichever service handled the action.
 */
@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Email of the user who performed the action (from X-Auth-User-Email). */
    @Column(nullable = false, length = 150)
    private String actorEmail;

    /** Role of the actor at the time of the action. */
    @Column(length = 30)
    private String actorRole;

    /**
     * Action verb in SCREAMING_SNAKE_CASE.
     * Examples: EMPLOYEE_CREATED, LEAVE_APPROVED, DEPARTMENT_UPDATED, ROLE_CHANGED.
     */
    @Column(nullable = false, length = 80)
    private String action;

    /** Entity type that was affected. Examples: Employee, Department, LeaveRequest. */
    @Column(length = 60)
    private String entityType;

    /** Primary key of the affected entity. */
    @Column
    private Long entityId;

    /**
     * Free-form JSON string with relevant before/after details or context.
     * Kept short — not a full row dump.
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
