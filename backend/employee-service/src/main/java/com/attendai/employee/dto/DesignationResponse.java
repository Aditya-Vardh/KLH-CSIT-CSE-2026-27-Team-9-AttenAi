package com.attendai.employee.dto;

import com.attendai.employee.entity.Designation;
import java.time.Instant;

public record DesignationResponse(
        Long id,
        String title,
        String description,
        String grade,
        boolean active,
        long employeeCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static DesignationResponse from(Designation d, long employeeCount) {
        return new DesignationResponse(
                d.getId(), d.getTitle(), d.getDescription(),
                d.getGrade(), d.isActive(), employeeCount,
                d.getCreatedAt(), d.getUpdatedAt());
    }
}
