package com.attendai.employee.controller;

import com.attendai.employee.dto.AuditLogResponse;
import com.attendai.employee.dto.PagedResponse;
import com.attendai.employee.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit Logs", description = "System-wide audit trail — Admin only")
public class AuditLogController {

    private static final Set<String> ADMIN_ROLES = Set.of("ADMIN");

    private final AuditLogService service;

    public AuditLogController(AuditLogService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Paginated, searchable audit log (Admin only)")
    public PagedResponse<AuditLogResponse> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String actor,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestHeader("X-Auth-User-Role") String callerRole) {

        if (!ADMIN_ROLES.contains(callerRole.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Audit logs are restricted to Admins");
        }

        var paged = service.search(action, entityType, actor,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return new PagedResponse<>(
                paged.getContent().stream().map(AuditLogResponse::from).toList(),
                paged.getNumber(),
                paged.getSize(),
                paged.getTotalElements(),
                paged.getTotalPages(),
                paged.isLast());
    }

    /**
     * Internal endpoint: other services (leave-service, attendance-service) POST
     * audit events here. Not exposed to the frontend directly.
     * Protected by the gateway JWT filter — any authenticated service call is accepted.
     */
    @PostMapping
    @Operation(summary = "Record an audit event (internal — called by other services)")
    public AuditLogResponse record(
            @RequestParam String actorEmail,
            @RequestParam(required = false) String actorRole,
            @RequestParam String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long   entityId,
            @RequestParam(required = false) String metadata) {

        var saved = service.record(actorEmail, actorRole, action, entityType, entityId, metadata);
        return AuditLogResponse.from(saved);
    }
}
