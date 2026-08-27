package com.attendai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AnalyticsQueryRequest(
        @NotBlank String question,
        List<Long> employeeIds,
        String fromDate,
        String toDate
) {}
