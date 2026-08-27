package com.attendai.ai.service;

import com.attendai.ai.client.AttendanceClient;
import com.attendai.ai.client.EmployeeClient;
import com.attendai.ai.client.dto.MonthlySummary;
import com.attendai.ai.dto.AnalyticsQueryRequest;
import com.attendai.ai.dto.AnalyticsQueryResponse;
import com.attendai.ai.dto.AnalyticsQueryResponse.EmployeeStat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsAiService {

    private final AttendanceClient attendanceClient;
    private final EmployeeClient employeeClient;

    public AnalyticsAiService(AttendanceClient attendanceClient, EmployeeClient employeeClient) {
        this.attendanceClient = attendanceClient;
        this.employeeClient = employeeClient;
    }

    public AnalyticsQueryResponse query(AnalyticsQueryRequest req, String callerEmail, String callerRole) {
        List<Long> ids = req.employeeIds() != null ? req.employeeIds() : List.of();
        LocalDate from = req.fromDate() != null ? LocalDate.parse(req.fromDate()) : LocalDate.now().withDayOfMonth(1);
        int year = from.getYear();
        int month = from.getMonthValue();

        List<EmployeeStat> stats = new ArrayList<>();
        for (Long id : ids) {
            try {
                MonthlySummary ms = attendanceClient.getMonthlySummary(id, year, month, callerEmail, callerRole);
                String name;
                try { name = employeeClient.getSummaryById(id, callerEmail, callerRole).fullName(); }
                catch (Exception e) { name = "Employee #" + id; }

                double pct = ms.attendancePercentage();
                String rating = pct >= 95 ? "EXCELLENT" : pct >= 85 ? "GOOD" : pct >= 70 ? "AVERAGE" : "POOR";
                stats.add(new EmployeeStat(id, name, pct, ms.lateDays(), ms.absentDays(), rating));
            } catch (Exception ignored) {}
        }

        // Build natural-language insight
        long poor = stats.stream().filter(s -> "POOR".equals(s.rating())).count();
        long excellent = stats.stream().filter(s -> "EXCELLENT".equals(s.rating())).count();
        String question = req.question();
        String insight;
        List<String> recs = new ArrayList<>();

        if (question.toLowerCase().contains("poor") || question.toLowerCase().contains("below")) {
            insight = poor == 0
                    ? "All employees have satisfactory attendance for " + year + "-" + String.format("%02d", month) + "."
                    : poor + " employee(s) have poor attendance (<70%) this month.";
            if (poor > 0) {
                recs.add("Schedule 1-on-1 meetings with poor-attendance employees.");
                recs.add("Review if workload or personal issues are affecting attendance.");
                recs.add("Consider issuing formal attendance warnings if pattern persists.");
            }
        } else {
            double avg = stats.stream().mapToDouble(EmployeeStat::attendancePct).average().orElse(0);
            insight = String.format("Average attendance for %d-%02d: %.1f%%. %d excellent, %d poor.",
                    year, month, avg, excellent, poor);
            recs.add("Maintain attendance incentives for top performers.");
            if (poor > 0) recs.add("Address " + poor + " employee(s) with poor attendance.");
        }

        return new AnalyticsQueryResponse(question, insight, recs, stats);
    }
}
