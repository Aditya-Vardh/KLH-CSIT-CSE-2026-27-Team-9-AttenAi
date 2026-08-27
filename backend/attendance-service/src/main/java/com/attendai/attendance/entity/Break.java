package com.attendai.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Tracks individual break sessions within an attendance record.
 * Multiple breaks per attendance session are supported.
 */
@Getter
@Setter
@Entity
@Table(name = "breaks",
       indexes = @Index(name = "idx_break_attendance", columnList = "attendance_id"))
public class Break {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** References Attendance.id — not a JPA relationship to avoid cross-aggregate coupling. */
    @Column(name = "attendance_id", nullable = false)
    private Long attendanceId;

    @Column(nullable = false)
    private LocalTime startTime;

    /** Null if break is still ongoing. */
    @Column
    private LocalTime endTime;

    /** Computed when break ends; null if break is still ongoing. */
    @Column
    private Long durationMinutes;
}
