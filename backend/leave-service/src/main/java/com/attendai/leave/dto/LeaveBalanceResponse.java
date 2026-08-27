package com.attendai.leave.dto;

import com.attendai.leave.entity.LeaveBalance;

public record LeaveBalanceResponse(
        Long id,
        Long employeeId,
        Long leaveTypeId,
        String leaveTypeName,
        int year,
        int allocatedDays,
        int usedDays,
        int pendingDays,
        int remainingDays
) {
    public static LeaveBalanceResponse from(LeaveBalance lb) {
        return new LeaveBalanceResponse(
                lb.getId(), lb.getEmployeeId(),
                lb.getLeaveType().getId(), lb.getLeaveType().getName(),
                lb.getYear(), lb.getAllocatedDays(),
                lb.getUsedDays(), lb.getPendingDays(), lb.getRemainingDays());
    }
}
