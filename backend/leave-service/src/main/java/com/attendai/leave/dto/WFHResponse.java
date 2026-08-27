package com.attendai.leave.dto;

import com.attendai.leave.entity.LeaveStatus;
import com.attendai.leave.entity.WFHRequest;
import java.time.Instant;
import java.time.LocalDate;

public record WFHResponse(
        Long id,
        Long employeeId,
        LocalDate startDate,
        LocalDate endDate,
        LeaveStatus status,
        String reason,
        String reviewComment,
        Long reviewedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static WFHResponse from(WFHRequest request) {
        return new WFHResponse(
                request.getId(),
                request.getEmployeeId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus(),
                request.getReason(),
                request.getReviewComment(),
                request.getReviewedBy(),
                request.getReviewedAt(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
