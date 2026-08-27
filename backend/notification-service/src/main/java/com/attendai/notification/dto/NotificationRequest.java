package com.attendai.notification.dto;

import com.attendai.notification.entity.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull Long recipientEmployeeId,
        @NotBlank @Email String recipientEmail,
        @NotNull NotificationType type,
        @NotBlank String subject,
        @NotBlank String body,
        Long referenceId
) {}
