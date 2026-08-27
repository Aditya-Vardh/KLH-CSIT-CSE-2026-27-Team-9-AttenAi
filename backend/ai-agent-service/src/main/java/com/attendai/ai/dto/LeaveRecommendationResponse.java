package com.attendai.ai.dto;

import java.util.List;

public record LeaveRecommendationResponse(
        String recommendation,    // APPROVE | REJECT
        double confidenceScore,   // 0.0 – 1.0
        String reason,
        List<String> factors,     // contributing factor labels
        double attendancePercentage,
        int remainingBalance,
        boolean teamConflict
) {}
