package com.attendai.leave.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Tracks remaining leave days per employee per leave-type per year.
 * Automatically created/refreshed at the start of each year or when an employee is onboarded.
 */
@Getter
@Setter
@Entity
@Table(name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_employee_leavetype_year",
                columnNames = {"employee_id", "leave_type_id", "leave_year"}))
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "leave_year", nullable = false)
    private int year;

    /** Allocated days for the year (leaveType.defaultDays + carryOver). */
    @Column(nullable = false)
    private int allocatedDays;

    /** Days used (from approved leave requests). */
    @Column(nullable = false)
    private int usedDays = 0;

    /** Days pending (from PENDING requests). */
    @Column(nullable = false)
    private int pendingDays = 0;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public int getRemainingDays() {
        return allocatedDays - usedDays - pendingDays;
    }
}
