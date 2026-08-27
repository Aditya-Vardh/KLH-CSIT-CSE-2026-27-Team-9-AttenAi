package com.attendai.employee.dto;

import com.attendai.employee.entity.AuditLog;
import java.time.Instant;

public record AuditLogResponse(
        Long id,
        String actorEmail,
        String actorRole,
        String action,
        String entityType,
        Long entityId,
        String metadata,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLog a) {
        return new AuditLogResponse(
                a.getId(), a.getActorEmail(), a.getActorRole(),
                a.getAction(), a.getEntityType(), a.getEntityId(),
                a.getMetadata(), a.getCreatedAt());
    }
}
