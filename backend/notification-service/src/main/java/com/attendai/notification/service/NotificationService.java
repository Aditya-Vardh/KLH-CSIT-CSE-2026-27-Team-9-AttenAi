package com.attendai.notification.service;

import com.attendai.notification.dto.NotificationRequest;
import com.attendai.notification.dto.NotificationResponse;
import com.attendai.notification.dto.PagedResponse;
import com.attendai.notification.entity.NotificationLog;
import com.attendai.notification.entity.NotificationStatus;
import com.attendai.notification.entity.NotificationType;
import com.attendai.notification.repository.NotificationLogRepository;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private final NotificationLogRepository repo;
    private final EmailService emailService;

    public NotificationService(NotificationLogRepository repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    /** Send a generic notification and persist the log entry. */
    public NotificationResponse send(NotificationRequest req) {
        NotificationLog log = buildLog(req);
        repo.save(log);

        emailService.sendHtml(req.recipientEmail(), req.subject(), req.body())
                .thenAccept(success -> updateStatus(log.getId(), success));

        return NotificationResponse.from(log);
    }

    // ── Typed helpers called by other services / scheduler ───────────────────

    public void sendLeaveApproved(Long employeeId, String email, String employeeName,
                                   String leaveType, String startDate, String endDate,
                                   int days, Long leaveRequestId) {
        String subject = "Leave Approved — " + leaveType;
        String body = buildLeaveDecisionHtml(employeeName, leaveType, startDate, endDate, days, "APPROVED", null);
        sendAndLog(employeeId, email, NotificationType.LEAVE_APPROVED, subject, body, leaveRequestId);
    }

    public void sendLeaveRejected(Long employeeId, String email, String employeeName,
                                   String leaveType, String startDate, String endDate,
                                   int days, String reason, Long leaveRequestId) {
        String subject = "Leave Rejected — " + leaveType;
        String body = buildLeaveDecisionHtml(employeeName, leaveType, startDate, endDate, days, "REJECTED", reason);
        sendAndLog(employeeId, email, NotificationType.LEAVE_REJECTED, subject, body, leaveRequestId);
    }

    public void sendLeaveSubmitted(Long employeeId, String email, String employeeName,
                                    String leaveType, String startDate, String endDate,
                                    int days, Long leaveRequestId) {
        String subject = "Leave Request Submitted — " + leaveType;
        String body = """
                <html><body style="font-family:sans-serif;padding:20px">
                <h2 style="color:#2563EB">Leave Request Submitted</h2>
                <p>Hi %s,</p>
                <p>Your leave request has been submitted successfully and is pending approval.</p>
                <table style="border-collapse:collapse;width:100%%">
                  <tr><td style="padding:8px;border:1px solid #e5e7eb"><strong>Type</strong></td><td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                  <tr><td style="padding:8px;border:1px solid #e5e7eb"><strong>From</strong></td><td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                  <tr><td style="padding:8px;border:1px solid #e5e7eb"><strong>To</strong></td><td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                  <tr><td style="padding:8px;border:1px solid #e5e7eb"><strong>Days</strong></td><td style="padding:8px;border:1px solid #e5e7eb">%d</td></tr>
                </table>
                <p style="color:#6b7280;font-size:12px;margin-top:24px">AttendAI — Attendance Management System</p>
                </body></html>
                """.formatted(employeeName, leaveType, startDate, endDate, days);
        sendAndLog(employeeId, email, NotificationType.LEAVE_SUBMITTED, subject, body, leaveRequestId);
    }

    public void sendLateArrivalAlert(Long employeeId, String email, String employeeName,
                                      String date, int lateMinutes, Long attendanceId) {
        String subject = "Late Arrival Alert — " + date;
        String body = """
                <html><body style="font-family:sans-serif;padding:20px">
                <h2 style="color:#DC2626">Late Arrival Recorded</h2>
                <p>Hi %s,</p>
                <p>Your attendance has been marked as <strong>Late</strong> for <strong>%s</strong>.</p>
                <p>You were <strong>%d minutes</strong> late today.</p>
                <p>Please ensure timely attendance going forward.</p>
                <p style="color:#6b7280;font-size:12px;margin-top:24px">AttendAI — Attendance Management System</p>
                </body></html>
                """.formatted(employeeName, date, lateMinutes);
        sendAndLog(employeeId, email, NotificationType.LATE_ARRIVAL, subject, body, attendanceId);
    }

    public void sendAttendanceReminder(Long employeeId, String email, String employeeName, String date) {
        String subject = "Attendance Reminder — " + date;
        String body = """
                <html><body style="font-family:sans-serif;padding:20px">
                <h2 style="color:#2563EB">Attendance Reminder</h2>
                <p>Hi %s,</p>
                <p>This is a reminder to mark your attendance for today <strong>%s</strong>.</p>
                <p>Please check-in through the AttendAI portal.</p>
                <p style="color:#6b7280;font-size:12px;margin-top:24px">AttendAI — Attendance Management System</p>
                </body></html>
                """.formatted(employeeName, date);
        sendAndLog(employeeId, email, NotificationType.ATTENDANCE_REMINDER, subject, body, null);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getByEmployee(Long employeeId, Pageable pageable) {
        return PagedResponse.from(
                repo.findByRecipientEmployeeIdOrderByCreatedAtDesc(employeeId, pageable)
                        .map(NotificationResponse::from));
    }

    @Transactional(readOnly = true)
    public long countUnread(Long employeeId) {
        return repo.countByRecipientEmployeeIdAndReadAtIsNull(employeeId);
    }

    public NotificationResponse markRead(Long id, Long employeeId) {
        repo.markOneRead(id, employeeId);
        return repo.findById(id)
                .map(NotificationResponse::from)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
    }

    public void markAllRead(Long employeeId) {
        repo.markAllRead(employeeId);
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private void sendAndLog(Long employeeId, String email, NotificationType type,
                             String subject, String body, Long referenceId) {
        NotificationLog log = new NotificationLog();
        log.setRecipientEmployeeId(employeeId);
        log.setRecipientEmail(email);
        log.setType(type);
        log.setSubject(subject);
        log.setBody(body);
        log.setReferenceId(referenceId);
        repo.save(log);

        emailService.sendHtml(email, subject, body)
                .thenAccept(success -> updateStatus(log.getId(), success));
    }

    private NotificationLog buildLog(NotificationRequest req) {
        NotificationLog log = new NotificationLog();
        log.setRecipientEmployeeId(req.recipientEmployeeId());
        log.setRecipientEmail(req.recipientEmail());
        log.setType(req.type());
        log.setSubject(req.subject());
        log.setBody(req.body());
        log.setReferenceId(req.referenceId());
        return log;
    }

    @Transactional
    public void updateStatus(Long logId, boolean success) {
        repo.findById(logId).ifPresent(l -> {
            l.setStatus(success ? NotificationStatus.SENT : NotificationStatus.FAILED);
            if (success) l.setSentAt(Instant.now());
            repo.save(l);
        });
    }

    private String buildLeaveDecisionHtml(String name, String leaveType, String from, String to,
                                           int days, String decision, String reason) {
        String color = "APPROVED".equals(decision) ? "#16A34A" : "#DC2626";
        String reasonRow = reason != null
                ? "<tr><td style='padding:8px;border:1px solid #e5e7eb'><strong>Reason</strong></td><td style='padding:8px;border:1px solid #e5e7eb'>" + reason + "</td></tr>"
                : "";
        return """
               <html><body style="font-family:sans-serif;padding:20px">
               <h2 style="color:%s">Leave %s</h2>
               <p>Hi %s,</p>
               <p>Your leave request has been <strong>%s</strong>.</p>
               <table style="border-collapse:collapse;width:100%%">
                 <tr><td style="padding:8px;border:1px solid #e5e7eb"><strong>Type</strong></td><td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                 <tr><td style="padding:8px;border:1px solid #e5e7eb"><strong>From</strong></td><td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                 <tr><td style="padding:8px;border:1px solid #e5e7eb"><strong>To</strong></td><td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                 <tr><td style="padding:8px;border:1px solid #e5e7eb"><strong>Days</strong></td><td style="padding:8px;border:1px solid #e5e7eb">%d</td></tr>
                 %s
               </table>
               <p style="color:#6b7280;font-size:12px;margin-top:24px">AttendAI — Attendance Management System</p>
               </body></html>
               """.formatted(color, decision, name, decision.toLowerCase(), leaveType, from, to, days, reasonRow);
    }
}
