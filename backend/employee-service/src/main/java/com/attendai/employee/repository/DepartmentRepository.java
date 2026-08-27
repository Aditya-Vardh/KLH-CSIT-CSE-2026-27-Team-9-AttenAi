package com.attendai.employee.repository;

import com.attendai.employee.entity.Department;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Department> findByNameIgnoreCase(String name);

    @Query("""
            SELECT d FROM Department d
            WHERE (:search IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:active IS NULL OR d.active = :active)
            """)
    Page<Department> search(@Param("search") String search,
                            @Param("active") Boolean active,
                            Pageable pageable);
}
