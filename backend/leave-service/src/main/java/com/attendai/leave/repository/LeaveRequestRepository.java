package com.attendai.leave.repository;

import com.attendai.leave.entity.LeaveRequest;
import com.attendai.leave.entity.LeaveStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /** Check if employee has an overlapping leave request that is not rejected/cancelled. */
    @Query("""
            SELECT COUNT(lr) > 0 FROM LeaveRequest lr
            WHERE lr.employeeId = :employeeId
            AND lr.status NOT IN ('REJECTED', 'CANCELLED', 'WITHDRAWN')
            AND lr.startDate <= :endDate
            AND lr.endDate   >= :startDate
            """)
    boolean hasOverlappingLeave(@Param("employeeId") Long employeeId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    /** Paginated history with optional filters. */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            LEFT JOIN FETCH lr.leaveType
            WHERE lr.employeeId = :employeeId
            AND (:status IS NULL OR lr.status = :status)
            AND (:year IS NULL OR YEAR(lr.startDate) = :year)
            ORDER BY lr.createdAt DESC
            """)
    Page<LeaveRequest> findByEmployee(@Param("employeeId") Long employeeId,
                                      @Param("status") LeaveStatus status,
                                      @Param("year") Integer year,
                                      Pageable pageable);

    /** All pending requests — for HR/manager dashboard. */
    @Query("SELECT lr FROM LeaveRequest lr LEFT JOIN FETCH lr.leaveType WHERE lr.status = 'PENDING' ORDER BY lr.createdAt ASC")
    List<LeaveRequest> findAllPending();

    /** All requests for HR with filters. */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            LEFT JOIN FETCH lr.leaveType
            WHERE (:employeeId IS NULL OR lr.employeeId = :employeeId)
            AND (:status IS NULL OR lr.status = :status)
            AND (:year IS NULL OR YEAR(lr.startDate) = :year)
            ORDER BY lr.createdAt DESC
            """)
    Page<LeaveRequest> findAll(@Param("employeeId") Long employeeId,
                               @Param("status") LeaveStatus status,
                               @Param("year") Integer year,
                               Pageable pageable);

    /** Sum of approved days for balance deduction. */
    @Query("""
            SELECT COALESCE(SUM(lr.totalDays), 0) FROM LeaveRequest lr
            WHERE lr.employeeId = :employeeId
            AND lr.leaveType.id = :leaveTypeId
            AND lr.status = 'APPROVED'
            AND YEAR(lr.startDate) = :year
            """)
    int sumApprovedDays(@Param("employeeId") Long employeeId,
                        @Param("leaveTypeId") Long leaveTypeId,
                        @Param("year") int year);

    /** Sum of pending days for balance hold. */
    @Query("""
            SELECT COALESCE(SUM(lr.totalDays), 0) FROM LeaveRequest lr
            WHERE lr.employeeId = :employeeId
            AND lr.leaveType.id = :leaveTypeId
            AND lr.status = 'PENDING'
            AND YEAR(lr.startDate) = :year
            """)
    int sumPendingDays(@Param("employeeId") Long employeeId,
                       @Param("leaveTypeId") Long leaveTypeId,
                       @Param("year") int year);

    /** Check if employee has approved leave covering a specific date. */
    @Query("""
            SELECT COUNT(lr) > 0 FROM LeaveRequest lr
            WHERE lr.employeeId = :employeeId
            AND lr.status = 'APPROVED'
            AND lr.startDate <= :date
            AND lr.endDate >= :date
            """)
    boolean hasApprovedLeaveOnDate(@Param("employeeId") Long employeeId,
                                   @Param("date") LocalDate date);

    /** Get approved leave request covering a specific date. */
    @Query("""
            SELECT lr FROM LeaveRequest lr LEFT JOIN FETCH lr.leaveType
            WHERE lr.employeeId = :employeeId
            AND lr.status = 'APPROVED'
            AND lr.startDate <= :date
            AND lr.endDate >= :date
            """)
    java.util.Optional<LeaveRequest> findApprovedLeaveForDate(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date);
}

