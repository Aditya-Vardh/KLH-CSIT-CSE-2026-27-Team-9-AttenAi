package com.attendai.attendance.entity;

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
 * Configurable office location.
 * Admin can create multiple offices; one is marked active for check-in validation.
 * Coordinates must be configured via Admin UI — never hard-coded.
 */
@Getter
@Setter
@Entity
@Table(name = "offices")
public class Office {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String officeName;

    /** WGS-84 latitude of the office center point. */
    @Column(nullable = false)
    private Double latitude;

    /** WGS-84 longitude of the office center point. */
    @Column(nullable = false)
    private Double longitude;

    /**
     * Radius in meters within which an employee is considered "at the office".
     * GPS accuracy is typically ±10–50 m; values < 50 m are not recommended.
     */
    @Column(nullable = false)
    private Integer allowedRadius = 100;

    @Column(nullable = false)
    private boolean active = true;

    /** Address for display purposes only. */
    @Column(length = 500)
    private String address;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
