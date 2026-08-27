package com.attendai.ai.service;

import com.attendai.ai.client.AttendanceClient;
import com.attendai.ai.client.EmployeeClient;
import com.attendai.ai.client.LeaveClient;
import com.attendai.ai.client.dto.EmployeeSummary;
import com.attendai.ai.client.dto.MonthlySummary;
import com.attendai.ai.dto.ChatRequest;
import com.attendai.ai.dto.ChatResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HrChatbotService {

    private final EmployeeClient employeeClient;
    private final AttendanceClient attendanceClient;
    private final LeaveClient leaveClient;

    public HrChatbotService(EmployeeClient employeeClient,
                              AttendanceClient attendanceClient,
                              LeaveClient leaveClient) {
        this.employeeClient = employeeClient;
        this.attendanceClient = attendanceClient;
        this.leaveClient = leaveClient;
    }

    public ChatResponse chat(ChatRequest req, String callerEmail, String callerRole) {
        String msg = req.message().toLowerCase().trim();

        // ── Intent routing ────────────────────────────────────────────────────
        if (matches(msg, "leave balance", "remaining leave", "how many leave", "leave days")) {
            return handleLeaveBalance(req.employeeId(), callerEmail, callerRole);
        }
        if (matches(msg, "attendance", "attendance percentage", "how often", "present")) {
            return handleAttendance(req.employeeId(), callerEmail, callerRole);
        }
        if (matches(msg, "my profile", "my info", "my details", "who am i")) {
            return handleProfile(req.employeeId(), callerEmail, callerRole);
        }
        if (matches(msg, "policy", "policies", "rules", "working hours", "office hours")) {
            return handlePolicy();
        }
        if (matches(msg, "hello", "hi", "hey", "good morning", "good afternoon")) {
            return new ChatResponse("GREETING", "Hello! I'm the AttendAI assistant. I can help with:\n"
                    + "• Leave balance\n• Attendance percentage\n• Your profile\n• Company policies\n\nWhat would you like to know?", null);
        }

        return new ChatResponse("UNKNOWN",
                "I didn't understand that. Try asking about your leave balance, attendance percentage, profile, or company policies.", null);
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    private ChatResponse handleLeaveBalance(Long empId, String email, String role) {
        try {
            var balances = leaveClient.getBalance(empId, email, role);
            String answer = buildBalanceText(balances);
            return new ChatResponse("LEAVE_BALANCE", answer, balances);
        } catch (Exception e) {
            return new ChatResponse("LEAVE_BALANCE", "Unable to fetch leave balance right now. Please try again later.", null);
        }
    }

    private ChatResponse handleAttendance(Long empId, String email, String role) {
        try {
            LocalDate now = LocalDate.now();
            MonthlySummary ms = attendanceClient.getMonthlySummary(
                    empId, now.getYear(), now.getMonthValue(), email, role);
            String answer = String.format(
                    "Your attendance for %s %d:\n• Present: %d days\n• Absent: %d days\n• Late: %d days\n• Attendance %%: %.1f%%\n• Total hours: %.1f hrs",
                    now.getMonth().name(), now.getYear(),
                    ms.presentDays(), ms.absentDays(), ms.lateDays(),
                    ms.attendancePercentage(), ms.totalWorkingHours());
            return new ChatResponse("ATTENDANCE", answer, ms);
        } catch (Exception e) {
            return new ChatResponse("ATTENDANCE", "Unable to fetch attendance data right now.", null);
        }
    }

    private ChatResponse handleProfile(Long empId, String email, String role) {
        try {
            EmployeeSummary emp = employeeClient.getSummaryById(empId, email, role);
            String answer = String.format(
                    "Your profile:\n• Name: %s\n• Code: %s\n• Email: %s\n• Department: %s\n• Designation: %s\n• Annual Leave Quota: %d days",
                    emp.fullName(), emp.employeeCode(), emp.email(),
                    emp.department(), emp.designation(), emp.annualLeaveQuota());
            return new ChatResponse("PROFILE", answer, emp);
        } catch (Exception e) {
            return new ChatResponse("PROFILE", "Unable to fetch your profile right now.", null);
        }
    }

    private ChatResponse handlePolicy() {
        String policy = """
                Company Attendance Policies:
                • Working hours: 9:00 AM – 6:00 PM (Mon–Fri)
                • Check-in deadline: 9:30 AM (after which marked as LATE)
                • Annual leave: 24 days/year
                • Sick leave: 12 days/year
                • Casual leave: 8 days/year
                • Leave requests must be submitted at least 1 day in advance
                • Half-day: working less than 4 hours
                """;
        return new ChatResponse("POLICY", policy.trim(), null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean matches(String msg, String... keywords) {
        for (String k : keywords) if (msg.contains(k)) return true;
        return false;
    }

    private String buildBalanceText(List<?> balances) {
        if (balances == null || balances.isEmpty()) return "No leave balance records found.";
        StringBuilder sb = new StringBuilder("Your leave balance:\n");
        for (Object b : balances) {
            // Use toString-like reflection-free approach via record toString
            sb.append("• ").append(b.toString()).append("\n");
        }
        return sb.toString().trim();
    }
}
