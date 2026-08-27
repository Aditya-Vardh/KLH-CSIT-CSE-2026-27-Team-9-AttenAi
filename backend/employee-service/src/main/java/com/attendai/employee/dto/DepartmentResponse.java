package com.attendai.employee.dto;

import com.attendai.employee.entity.Department;
import java.time.Instant;

public record DepartmentResponse(
        Long id,
        String name,
        String description,
        Long headUserId,
        boolean active,
        long employeeCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static DepartmentResponse from(Department d, long employeeCount) {
        return new DepartmentResponse(
                d.getId(), d.getName(), d.getDescription(),
                d.getHeadUserId(), d.isActive(), employeeCount,
                d.getCreatedAt(), d.getUpdatedAt());
    }
}
