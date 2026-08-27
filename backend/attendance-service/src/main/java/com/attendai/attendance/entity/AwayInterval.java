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
 * Tracks "Away" intervals for an attendance record.
 * An employee transitions to AWAY when they leave their desk area
 * but do not formally start a break.
 * Note: browser inactivity is NOT automatically treated as being away.
 */
@Getter
@Setter
@Entity
@Table(name = "away_intervals",
       indexes = @Index(name = "idx_away_attendance", columnList = "attendance_id"))
public class AwayInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attendance_id", nullable = false)
    private Long attendanceId;

    @Column(nullable = false)
    private LocalTime startTime;

    /** Null if employee is still away. */
    @Column
    private LocalTime endTime;
}
