package com.attendai.notification.controller;

import com.attendai.notification.dto.NotificationRequest;
import com.attendai.notification.dto.NotificationResponse;
import com.attendai.notification.dto.PagedResponse;
import com.attendai.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Send and query notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Send a notification (generic)")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.send(req));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get notification history for an employee")
    public PagedResponse<NotificationResponse> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.getByEmployee(employeeId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/employee/{employeeId}/unread-count")
    @Operation(summary = "Count unread notifications for an employee")
    public java.util.Map<String, Long> unreadCount(@PathVariable Long employeeId) {
        return java.util.Map.of("unreadCount", service.countUnread(employeeId));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    public NotificationResponse markRead(
            @PathVariable Long id,
            @RequestParam Long employeeId) {
        return service.markRead(id, employeeId);
    }

    @PatchMapping("/employee/{employeeId}/read-all")
    @Operation(summary = "Mark all notifications as read for an employee")
    public ResponseEntity<Void> markAllRead(@PathVariable Long employeeId) {
        service.markAllRead(employeeId);
        return ResponseEntity.noContent().build();
    }

    // ── Typed endpoints (called by other services) ────────────────────────────

    @PostMapping("/leave-approved")
    @Operation(summary = "Send leave-approved email")
    public ResponseEntity<Void> leaveApproved(
            @RequestParam Long employeeId, @RequestParam String email,
            @RequestParam String employeeName, @RequestParam String leaveType,
            @RequestParam String startDate, @RequestParam String endDate,
            @RequestParam int days, @RequestParam Long leaveRequestId) {
        service.sendLeaveApproved(employeeId, email, employeeName, leaveType, startDate, endDate, days, leaveRequestId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/leave-rejected")
    @Operation(summary = "Send leave-rejected email")
    public ResponseEntity<Void> leaveRejected(
            @RequestParam Long employeeId, @RequestParam String email,
            @RequestParam String employeeName, @RequestParam String leaveType,
            @RequestParam String startDate, @RequestParam String endDate,
            @RequestParam int days, @RequestParam(required = false) String reason,
            @RequestParam Long leaveRequestId) {
        service.sendLeaveRejected(employeeId, email, employeeName, leaveType, startDate, endDate, days, reason, leaveRequestId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/leave-submitted")
    @Operation(summary = "Send leave-submitted confirmation email")
    public ResponseEntity<Void> leaveSubmitted(
            @RequestParam Long employeeId, @RequestParam String email,
            @RequestParam String employeeName, @RequestParam String leaveType,
            @RequestParam String startDate, @RequestParam String endDate,
            @RequestParam int days, @RequestParam Long leaveRequestId) {
        service.sendLeaveSubmitted(employeeId, email, employeeName, leaveType, startDate, endDate, days, leaveRequestId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/late-arrival")
    @Operation(summary = "Send late-arrival alert email")
    public ResponseEntity<Void> lateArrival(
            @RequestParam Long employeeId, @RequestParam String email,
            @RequestParam String employeeName, @RequestParam String date,
            @RequestParam int lateMinutes, @RequestParam Long attendanceId) {
        service.sendLateArrivalAlert(employeeId, email, employeeName, date, lateMinutes, attendanceId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/attendance-reminder")
    @Operation(summary = "Send attendance reminder email")
    public ResponseEntity<Void> attendanceReminder(
            @RequestParam Long employeeId, @RequestParam String email,
            @RequestParam String employeeName, @RequestParam String date) {
        service.sendAttendanceReminder(employeeId, email, employeeName, date);
        return ResponseEntity.accepted().build();
    }
}
