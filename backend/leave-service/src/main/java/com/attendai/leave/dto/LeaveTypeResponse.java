package com.attendai.leave.dto;

import com.attendai.leave.entity.LeaveType;
import java.time.Instant;

public record LeaveTypeResponse(
        Long id,
        String name,
        String description,
        int defaultDays,
        boolean carryOver,
        boolean allowNegative,
        boolean requiresDocument,
        boolean active,
        Instant createdAt
) {
    public static LeaveTypeResponse from(LeaveType lt) {
        return new LeaveTypeResponse(lt.getId(), lt.getName(), lt.getDescription(),
                lt.getDefaultDays(), lt.isCarryOver(), lt.isAllowNegative(),
                lt.isRequiresDocument(), lt.isActive(), lt.getCreatedAt());
    }
}
