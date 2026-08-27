package com.attendai.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_employee_date",
                columnNames = {"employee_id", "attendance_date"}))
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** References employee-service Employee.id — not a cross-service FK. */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column
    private LocalTime checkInTime;

    @Column
    private LocalTime checkOutTime;

    /** Working hours computed on check-out (hours as decimal, e.g. 7.5). */
    @Column(precision = 4)
    private Double workingHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    /** Company-configured check-in cutoff (e.g. 09:30); stored per record for auditability. */
    @Column
    private LocalTime expectedCheckIn;

    /** True when check-in was after expectedCheckIn. */
    @Column(nullable = false)
    private boolean lateArrival = false;

    /** Minutes late (0 if not late). */
    @Column(nullable = false)
    private int lateMinutes = 0;

    /** Free-text note from employee or HR (e.g. "Traffic jam", "Doctor appointment"). */
    @Column(length = 500)
    private String note;

    /** True if this record was set via the AI natural-language feature. */
    @Column(nullable = false)
    private boolean aiGenerated = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
