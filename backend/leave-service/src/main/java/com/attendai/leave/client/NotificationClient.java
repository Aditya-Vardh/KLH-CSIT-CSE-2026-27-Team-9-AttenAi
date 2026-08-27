package com.attendai.leave.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client that calls the notification-service to trigger transactional emails
 * and create in-app notification log entries when leave requests change state.
 *
 * All calls are fire-and-forget: failures are logged but never propagate to the caller.
 */
@FeignClient(name = "notification-service", path = "/api/notifications")
public interface NotificationClient {

    @PostMapping("/leave-submitted")
    ResponseEntity<Void> leaveSubmitted(
            @RequestParam Long employeeId,
            @RequestParam String email,
            @RequestParam String employeeName,
            @RequestParam String leaveType,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam int days,
            @RequestParam Long leaveRequestId);

    @PostMapping("/leave-approved")
    ResponseEntity<Void> leaveApproved(
            @RequestParam Long employeeId,
            @RequestParam String email,
            @RequestParam String employeeName,
            @RequestParam String leaveType,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam int days,
            @RequestParam Long leaveRequestId);

    @PostMapping("/leave-rejected")
    ResponseEntity<Void> leaveRejected(
            @RequestParam Long employeeId,
            @RequestParam String email,
            @RequestParam String employeeName,
            @RequestParam String leaveType,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam int days,
            @RequestParam(required = false) String reason,
            @RequestParam Long leaveRequestId);
}
