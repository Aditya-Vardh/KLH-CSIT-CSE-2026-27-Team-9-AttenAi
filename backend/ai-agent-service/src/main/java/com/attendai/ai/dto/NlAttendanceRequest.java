package com.attendai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Employee types a natural-language message, e.g. "I'll be 30 minutes late today".
 */
public record NlAttendanceRequest(
        @NotNull Long employeeId,
        @NotBlank String message
) {}
