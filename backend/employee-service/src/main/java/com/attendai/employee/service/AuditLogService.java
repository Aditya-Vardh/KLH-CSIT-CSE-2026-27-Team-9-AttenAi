package com.attendai.employee.service;

import com.attendai.employee.entity.AuditLog;
import com.attendai.employee.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository repo;

    public AuditLogService(AuditLogRepository repo) {
        this.repo = repo;
    }

    /**
     * Record an audit event. Called internally by EmployeeService and DepartmentService.
     *
     * @param actorEmail  email extracted from X-Auth-User-Email header
     * @param actorRole   role extracted from X-Auth-User-Role header
     * @param action      e.g. EMPLOYEE_CREATED, DEPARTMENT_UPDATED
     * @param entityType  e.g. "Employee", "Department"
     * @param entityId    primary key of the affected record
     * @param metadata    optional JSON string with extra context
     */
    @Transactional
    public AuditLog record(String actorEmail, String actorRole,
                            String action, String entityType,
                            Long entityId, String metadata) {
        AuditLog log = new AuditLog();
        log.setActorEmail(actorEmail != null ? actorEmail : "system");
        log.setActorRole(actorRole);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setMetadata(metadata);
        return repo.save(log);
    }

    /**
     * Read-only paginated search for the admin audit log page.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> search(String action, String entityType,
                                  String actor, Pageable pageable) {
        return repo.search(action, entityType, actor, pageable);
    }
}
