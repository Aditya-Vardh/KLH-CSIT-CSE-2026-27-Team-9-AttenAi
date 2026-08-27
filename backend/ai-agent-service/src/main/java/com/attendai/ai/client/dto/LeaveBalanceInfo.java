package com.attendai.ai.client.dto;

public record LeaveBalanceInfo(Long id, Long employeeId, Long leaveTypeId,
        String leaveTypeName, int year, int allocatedDays,
        int usedDays, int pendingDays, int remainingDays) {}
