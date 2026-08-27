package com.attendai.leave.dto;

import com.attendai.leave.entity.LeaveRequest;
import com.attendai.leave.entity.LeaveStatus;
import java.time.Instant;
import java.time.LocalDate;

public record LeaveResponse(
        Long id,
        Long employeeId,
        LeaveTypeSummary leaveType,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        LeaveStatus status,
        String reason,
        String reviewComment,
        Long reviewedBy,
        Instant reviewedAt,
        String documentUrl,
        boolean aiRecommended,
        Double aiConfidenceScore,
        String aiRecommendation,
        Instant createdAt,
        Instant updatedAt
) {
    public record LeaveTypeSummary(Long id, String name) {}

    public static LeaveResponse from(LeaveRequest lr) {
        LeaveTypeSummary lt = lr.getLeaveType() == null ? null
                : new LeaveTypeSummary(lr.getLeaveType().getId(), lr.getLeaveType().getName());
        return new LeaveResponse(
                lr.getId(), lr.getEmployeeId(), lt,
                lr.getStartDate(), lr.getEndDate(), lr.getTotalDays(),
                lr.getStatus(), lr.getReason(), lr.getReviewComment(),
                lr.getReviewedBy(), lr.getReviewedAt(), lr.getDocumentUrl(),
                lr.isAiRecommended(), lr.getAiConfidenceScore(), lr.getAiRecommendation(),
                lr.getCreatedAt(), lr.getUpdatedAt());
    }
}
