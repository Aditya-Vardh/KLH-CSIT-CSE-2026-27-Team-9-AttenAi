package com.attendai.ai.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record LeaveRecommendationRequest(
        @NotNull Long employeeId,
        @NotNull Long leaveTypeId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason
) {}
