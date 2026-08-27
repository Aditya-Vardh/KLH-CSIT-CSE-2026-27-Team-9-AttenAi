package com.attendai.employee.repository;

import com.attendai.employee.entity.Designation;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DesignationRepository extends JpaRepository<Designation, Long> {

    boolean existsByTitleIgnoreCase(String title);

    Optional<Designation> findByTitleIgnoreCase(String title);

    @Query("""
            SELECT d FROM Designation d
            WHERE (:search IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(d.grade) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:active IS NULL OR d.active = :active)
            """)
    Page<Designation> search(@Param("search") String search,
                             @Param("active") Boolean active,
                             Pageable pageable);
}
