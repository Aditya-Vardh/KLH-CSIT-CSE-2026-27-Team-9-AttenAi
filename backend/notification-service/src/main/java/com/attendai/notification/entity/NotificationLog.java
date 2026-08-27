package com.attendai.notification.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter @Setter
@Entity
@Table(name = "notification_logs")
public class NotificationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long recipientEmployeeId;
    @Column(nullable = false, length = 150) private String recipientEmail;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private NotificationType type;
    @Column(nullable = false, length = 200) private String subject;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private NotificationStatus status = NotificationStatus.PENDING;
    @Column(length = 500) private String errorMessage;
    @Column private Long referenceId;   // leaveRequestId / attendanceId
    @CreationTimestamp @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column private Instant sentAt;

    /** Null = unread; set to the time the user viewed/acknowledged the notification. */
    @Column private Instant readAt;

    public boolean isRead() { return readAt != null; }
}
