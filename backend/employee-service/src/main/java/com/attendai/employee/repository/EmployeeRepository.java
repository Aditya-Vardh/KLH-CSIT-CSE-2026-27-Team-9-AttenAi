package com.attendai.employee.repository;

import com.attendai.employee.entity.Employee;
import com.attendai.employee.entity.EmployeeStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    @Query("""
            SELECT e FROM Employee e
            LEFT JOIN FETCH e.department
            LEFT JOIN FETCH e.designation
            WHERE (:search IS NULL
                   OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(e.lastName)  LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(e.email)     LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:departmentId IS NULL OR e.department.id = :departmentId)
            AND (:designationId IS NULL OR e.designation.id = :designationId)
            AND (:status IS NULL OR e.status = :status)
            """)
    Page<Employee> search(@Param("search") String search,
                          @Param("departmentId") Long departmentId,
                          @Param("designationId") Long designationId,
                          @Param("status") EmployeeStatus status,
                          Pageable pageable);

    long countByDepartmentId(Long departmentId);

    long countByDesignationId(Long designationId);
}
