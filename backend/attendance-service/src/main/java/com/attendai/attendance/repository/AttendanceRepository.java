package com.attendai.attendance.repository;

import com.attendai.attendance.entity.Attendance;
import com.attendai.attendance.entity.AttendanceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    boolean existsByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            Long employeeId, LocalDate from, LocalDate to);

    // Paginated history with optional status filter
    @Query("""
            SELECT a FROM Attendance a
            WHERE a.employeeId = :employeeId
            AND a.attendanceDate BETWEEN :from AND :to
            AND (:status IS NULL OR a.status = :status)
            ORDER BY a.attendanceDate DESC
            """)
    Page<Attendance> findHistory(@Param("employeeId") Long employeeId,
                                 @Param("from") LocalDate from,
                                 @Param("to") LocalDate to,
                                 @Param("status") AttendanceStatus status,
                                 Pageable pageable);

    // Monthly summary counts
    @Query("""
            SELECT COUNT(a) FROM Attendance a
            WHERE a.employeeId = :employeeId
            AND YEAR(a.attendanceDate) = :year
            AND MONTH(a.attendanceDate) = :month
            AND a.status IN ('PRESENT', 'LATE', 'HALF_DAY')
            """)
    long countPresentDays(@Param("employeeId") Long employeeId,
                          @Param("year") int year,
                          @Param("month") int month);

    @Query("""
            SELECT COUNT(a) FROM Attendance a
            WHERE a.employeeId = :employeeId
            AND YEAR(a.attendanceDate) = :year
            AND MONTH(a.attendanceDate) = :month
            """)
    long countTotalRecords(@Param("employeeId") Long employeeId,
                           @Param("year") int year,
                           @Param("month") int month);

    @Query("""
            SELECT COUNT(a) FROM Attendance a
            WHERE a.employeeId = :employeeId
            AND YEAR(a.attendanceDate) = :year
            AND MONTH(a.attendanceDate) = :month
            AND a.lateArrival = true
            """)
    long countLateDays(@Param("employeeId") Long employeeId,
                       @Param("year") int year,
                       @Param("month") int month);

    @Query("""
            SELECT SUM(a.workingHours) FROM Attendance a
            WHERE a.employeeId = :employeeId
            AND YEAR(a.attendanceDate) = :year
            AND MONTH(a.attendanceDate) = :month
            AND a.workingHours IS NOT NULL
            """)
    Double sumWorkingHours(@Param("employeeId") Long employeeId,
                           @Param("year") int year,
                           @Param("month") int month);

    // Department-level analytics (all employees in a list)
    @Query("""
            SELECT a FROM Attendance a
            WHERE a.employeeId IN :employeeIds
            AND a.attendanceDate BETWEEN :from AND :to
            ORDER BY a.attendanceDate DESC, a.employeeId
            """)
    List<Attendance> findByEmployeeIdsAndDateRange(@Param("employeeIds") List<Long> employeeIds,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);

    // Late arrivals for a date range
    @Query("""
            SELECT a FROM Attendance a
            WHERE a.lateArrival = true
            AND a.attendanceDate BETWEEN :from AND :to
            ORDER BY a.attendanceDate DESC
            """)
    List<Attendance> findLateArrivals(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // Absence detection — employees who have no record for today
    @Query("""
            SELECT DISTINCT a.employeeId FROM Attendance a
            WHERE a.attendanceDate = :date
            """)
    List<Long> findEmployeeIdsWithRecordOnDate(@Param("date") LocalDate date);
}
