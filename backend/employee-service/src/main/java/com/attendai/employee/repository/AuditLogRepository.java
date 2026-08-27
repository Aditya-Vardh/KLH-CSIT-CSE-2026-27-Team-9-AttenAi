package com.attendai.employee.repository;

import com.attendai.employee.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Flexible search: filter by action keyword, entity type, or actor email.
     * All filters are optional (null means "any").
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:action     IS NULL OR UPPER(a.action)     LIKE UPPER(CONCAT('%', :action, '%')))
              AND (:entityType IS NULL OR UPPER(a.entityType) LIKE UPPER(CONCAT('%', :entityType, '%')))
              AND (:actor      IS NULL OR LOWER(a.actorEmail) LIKE LOWER(CONCAT('%', :actor, '%')))
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> search(
            @Param("action")     String action,
            @Param("entityType") String entityType,
            @Param("actor")      String actor,
            Pageable pageable);
}
