package com.attendai.notification.dto;

import com.attendai.notification.entity.NotificationLog;
import com.attendai.notification.entity.NotificationStatus;
import com.attendai.notification.entity.NotificationType;
import java.time.Instant;

public record NotificationResponse(
        Long id, Long recipientEmployeeId, String recipientEmail,
        NotificationType type, String subject, NotificationStatus status,
        String errorMessage, Long referenceId,
        Instant createdAt, Instant sentAt,
        Instant readAt, boolean read
) {
    public static NotificationResponse from(NotificationLog l) {
        return new NotificationResponse(
                l.getId(), l.getRecipientEmployeeId(), l.getRecipientEmail(),
                l.getType(), l.getSubject(), l.getStatus(), l.getErrorMessage(),
                l.getReferenceId(), l.getCreatedAt(), l.getSentAt(),
                l.getReadAt(), l.isRead());
    }
}
