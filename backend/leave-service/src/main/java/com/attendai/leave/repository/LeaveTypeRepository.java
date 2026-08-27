package com.attendai.leave.repository;

import com.attendai.leave.entity.LeaveType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<LeaveType> findByActiveTrue();
}
