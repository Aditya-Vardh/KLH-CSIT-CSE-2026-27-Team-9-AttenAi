package com.attendai.leave.repository;

import com.attendai.leave.entity.LeaveBalance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
            Long employeeId, Long leaveTypeId, int year);

    @Query("SELECT lb FROM LeaveBalance lb LEFT JOIN FETCH lb.leaveType WHERE lb.employeeId = :employeeId AND lb.year = :year")
    List<LeaveBalance> findByEmployeeIdAndYear(@Param("employeeId") Long employeeId,
                                               @Param("year") int year);
}
