package com.attendai.ai.service;

import com.attendai.ai.client.AttendanceClient;
import com.attendai.ai.client.LeaveClient;
import com.attendai.ai.client.dto.LeaveBalanceInfo;
import com.attendai.ai.client.dto.MonthlySummary;
import com.attendai.ai.dto.LeaveRecommendationRequest;
import com.attendai.ai.dto.LeaveRecommendationResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LeaveRecommendationService {

    private final AttendanceClient attendanceClient;
    private final LeaveClient leaveClient;

    public LeaveRecommendationService(AttendanceClient attendanceClient, LeaveClient leaveClient) {
        this.attendanceClient = attendanceClient;
        this.leaveClient = leaveClient;
    }

    public LeaveRecommendationResponse recommend(LeaveRecommendationRequest req,
                                                  String callerEmail, String callerRole) {
        int year = req.startDate().getYear();
        int month = req.startDate().getMonthValue();

        // 1. Attendance percentage
        double attendancePct = 100.0;
        try {
            MonthlySummary ms = attendanceClient.getMonthlySummary(
                    req.employeeId(), year, month, callerEmail, callerRole);
            attendancePct = ms.attendancePercentage();
        } catch (Exception ignored) {}

        // 2. Leave balance for requested type
        int remaining = 999;
        try {
            List<LeaveBalanceInfo> balances = leaveClient.getBalance(req.employeeId(), callerEmail, callerRole);
            Optional<LeaveBalanceInfo> bal = balances.stream()
                    .filter(b -> b.leaveTypeId().equals(req.leaveTypeId())).findFirst();
            if (bal.isPresent()) remaining = bal.get().remainingDays();
        } catch (Exception ignored) {}

        // 3. Requested days
        long requestedDays = countBusinessDays(req.startDate(), req.endDate());

        // ── Scoring ────────────────────────────────────────────────────────────
        List<String> factors = new ArrayList<>();
        double score = 1.0;

        if (attendancePct < 70) { score -= 0.35; factors.add("Low attendance (" + attendancePct + "%)"); }
        else if (attendancePct < 85) { score -= 0.15; factors.add("Below-average attendance"); }
        else factors.add("Good attendance record");

        if (remaining < requestedDays) { score -= 0.40; factors.add("Insufficient leave balance"); }
        else if (remaining < requestedDays * 2) { score -= 0.10; factors.add("Low remaining balance"); }
        else factors.add("Adequate leave balance");

        if (requestedDays > 10) { score -= 0.20; factors.add("Long leave duration (>10 days)"); }
        else if (requestedDays > 5) { score -= 0.05; factors.add("Extended leave duration"); }

        // Clamp 0..1
        score = Math.max(0.0, Math.min(1.0, Math.round(score * 100.0) / 100.0));

        String recommendation = score >= 0.60 ? "APPROVE" : "REJECT";
        String reason = score >= 0.60
                ? "Employee meets all eligibility criteria for the requested leave."
                : "One or more eligibility criteria not met: " + String.join(", ", factors) + ".";

        return new LeaveRecommendationResponse(
                recommendation, score, reason, factors,
                attendancePct, remaining, false);
    }

    private long countBusinessDays(LocalDate from, LocalDate to) {
        return from.datesUntil(to.plusDays(1))
                .filter(d -> d.getDayOfWeek().getValue() <= 5).count();
    }
}
