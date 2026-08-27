package com.attendai.notification.repository;

import com.attendai.notification.entity.NotificationLog;
import com.attendai.notification.entity.NotificationStatus;
import com.attendai.notification.entity.NotificationType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Page<NotificationLog> findByRecipientEmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);
    List<NotificationLog> findByStatus(NotificationStatus status);
    long countByRecipientEmployeeIdAndType(Long employeeId, NotificationType type);

    long countByRecipientEmployeeIdAndReadAtIsNull(Long employeeId);

    @Modifying
    @Query("UPDATE NotificationLog n SET n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id AND n.recipientEmployeeId = :employeeId AND n.readAt IS NULL")
    int markOneRead(@Param("id") Long id, @Param("employeeId") Long employeeId);

    @Modifying
    @Query("UPDATE NotificationLog n SET n.readAt = CURRENT_TIMESTAMP WHERE n.recipientEmployeeId = :employeeId AND n.readAt IS NULL")
    int markAllRead(@Param("employeeId") Long employeeId);
}
