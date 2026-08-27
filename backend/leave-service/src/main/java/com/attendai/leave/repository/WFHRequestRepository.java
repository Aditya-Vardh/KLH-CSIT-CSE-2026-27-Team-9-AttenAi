package com.attendai.leave.repository;

import com.attendai.leave.entity.LeaveStatus;
import com.attendai.leave.entity.WFHRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WFHRequestRepository extends JpaRepository<WFHRequest, Long> {

    List<WFHRequest> findByStatus(LeaveStatus status);

    Page<WFHRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    @Query("SELECT w FROM WFHRequest w WHERE w.employeeId = :empId AND w.status = :status AND :date BETWEEN w.startDate AND w.endDate")
    Optional<WFHRequest> findActiveRequest(
            @Param("empId") Long employeeId,
            @Param("date") LocalDate date,
            @Param("status") LeaveStatus status);

    @Query("SELECT w FROM WFHRequest w WHERE w.employeeId = :empId AND w.status = 'APPROVED' AND :date BETWEEN w.startDate AND w.endDate")
    Optional<WFHRequest> findActiveApprovedRequest(
            @Param("empId") Long employeeId,
            @Param("date") LocalDate date);

    @Query("SELECT w FROM WFHRequest w WHERE w.employeeId = :empId AND w.status IN :statuses")
    Page<WFHRequest> findByEmployeeIdAndStatusIn(
            @Param("empId") Long employeeId,
            @Param("statuses") List<LeaveStatus> statuses,
            Pageable pageable);

    @Query("SELECT COUNT(w) > 0 FROM WFHRequest w WHERE w.employeeId = :empId " +
           "AND w.status = 'APPROVED' " +
           "AND ((w.startDate <= :end AND w.endDate >= :start))")
    boolean hasOverlappingApproved(
            @Param("empId") Long employeeId,
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate);
}
