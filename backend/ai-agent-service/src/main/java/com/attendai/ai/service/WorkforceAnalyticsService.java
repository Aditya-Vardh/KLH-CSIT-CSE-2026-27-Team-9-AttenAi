package com.attendai.ai.service;

import com.attendai.ai.client.AttendanceClient;
import com.attendai.ai.client.EmployeeClient;
import com.attendai.ai.client.dto.EmployeeSummary;
import com.attendai.ai.client.dto.MonthlySummary;
import com.attendai.ai.dto.WorkforceSummaryResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WorkforceAnalyticsService {
    private static final double LOW_ATTENDANCE_THRESHOLD = 70.0;
    private static final long FREQUENT_LATENESS_THRESHOLD = 3;

    private final AttendanceClient attendanceClient;
    private final EmployeeClient employeeClient;

    public WorkforceAnalyticsService(AttendanceClient attendanceClient, EmployeeClient employeeClient) {
        this.attendanceClient = attendanceClient;
        this.employeeClient = employeeClient;
    }

    public WorkforceSummaryResponse summary(List<Long> employeeIds, LocalDate fromDate, LocalDate toDate,
                                            String email, String role) {
        LocalDate end = toDate == null ? LocalDate.now() : toDate;
        LocalDate start = fromDate == null ? end.withDayOfMonth(1) : fromDate;
        LocalDate previous = start.minusMonths(1);
        List<InsightData> data = load(employeeIds, start, previous, email, role);

        double average = average(data, InsightData::currentPct);
        double previousAverage = average(data, InsightData::previousPct);
        long present = data.stream().mapToLong(item -> item.current().presentDays()).sum();
        long absent = data.stream().mapToLong(item -> item.current().absentDays()).sum();
        long late = data.stream().mapToLong(item -> item.current().lateDays()).sum();
        double hours = data.stream().mapToDouble(item -> item.current().totalWorkingHours()).sum();
        String period = start + " to " + end;

        List<WorkforceSummaryResponse.Anomaly> anomalies = new ArrayList<>();
        List<WorkforceSummaryResponse.EmployeeInsight> employees = new ArrayList<>();
        Map<String, DepartmentAccumulator> departmentMap = new LinkedHashMap<>();
        for (InsightData item : data) {
            double change = item.currentPct() - item.previousPct();
            String status = item.currentPct() < LOW_ATTENDANCE_THRESHOLD ? "LOW_ATTENDANCE"
                    : item.current().lateDays() >= FREQUENT_LATENESS_THRESHOLD ? "FREQUENT_LATENESS" : "OK";
            employees.add(new WorkforceSummaryResponse.EmployeeInsight(item.employee().id(), item.employee().fullName(),
                    department(item.employee()), item.currentPct(), item.current().presentDays(),
                    item.current().absentDays(), item.current().lateDays(), item.current().totalWorkingHours(),
                    item.previousPct(), change, status));
            if (item.currentPct() < LOW_ATTENDANCE_THRESHOLD) {
                anomalies.add(new WorkforceSummaryResponse.Anomaly(item.employee().id(), item.employee().fullName(),
                        department(item.employee()), "LOW_ATTENDANCE", item.currentPct(), LOW_ATTENDANCE_THRESHOLD,
                        period, item.currentPct() < 50 ? "HIGH" : "MEDIUM"));
            }
            if (item.current().lateDays() >= FREQUENT_LATENESS_THRESHOLD) {
                anomalies.add(new WorkforceSummaryResponse.Anomaly(item.employee().id(), item.employee().fullName(),
                        department(item.employee()), "FREQUENT_LATENESS", item.current().lateDays(),
                        FREQUENT_LATENESS_THRESHOLD, period, "MEDIUM"));
            }
            departmentMap.computeIfAbsent(department(item.employee()), DepartmentAccumulator::new).add(item, change,
                    item.currentPct() < LOW_ATTENDANCE_THRESHOLD || item.current().lateDays() >= FREQUENT_LATENESS_THRESHOLD);
        }

        List<WorkforceSummaryResponse.DepartmentInsight> departments = departmentMap.values().stream()
                .map(DepartmentAccumulator::toInsight).toList();
        return new WorkforceSummaryResponse(start.toString(), end.toString(), employeeIds == null ? 0 : employeeIds.size(),
                data.size(), average, present, absent, late, hours, previousAverage, average - previousAverage,
                anomalies, employees, departments);
    }

    private List<InsightData> load(List<Long> ids, LocalDate current, LocalDate previous, String email, String role) {
        List<InsightData> result = new ArrayList<>();
        if (ids == null) return result;
        for (Long id : ids.stream().distinct().toList()) {
            try {
                MonthlySummary currentSummary = attendanceClient.getMonthlySummary(id, current.getYear(), current.getMonthValue(), email, role);
                MonthlySummary previousSummary = attendanceClient.getMonthlySummary(id, previous.getYear(), previous.getMonthValue(), email, role);
                EmployeeSummary employee = employeeClient.getSummaryById(id, email, role);
                result.add(new InsightData(employee, currentSummary, previousSummary));
            } catch (RuntimeException ignored) {
            }
        }
        return result;
    }

    private String department(EmployeeSummary employee) {
        return employee.department() == null || employee.department().isBlank() ? "Unassigned" : employee.department();
    }

    private double average(List<InsightData> data, java.util.function.ToDoubleFunction<InsightData> value) {
        return data.stream().mapToDouble(value).average().orElse(0);
    }

    private record InsightData(EmployeeSummary employee, MonthlySummary current, MonthlySummary previous) {
        double currentPct() { return current.attendancePercentage(); }
        double previousPct() { return previous.attendancePercentage(); }
    }

    private static final class DepartmentAccumulator {
        private final String name;
        private int count;
        private double attendance;
        private long late;
        private long absent;
        private double change;
        private int anomalies;

        private DepartmentAccumulator(String name) { this.name = name; }

        private void add(InsightData item, double employeeChange, boolean anomaly) {
            count++;
            attendance += item.currentPct();
            late += item.current().lateDays();
            absent += item.current().absentDays();
            change += employeeChange;
            if (anomaly) anomalies++;
        }

        private WorkforceSummaryResponse.DepartmentInsight toInsight() {
            return new WorkforceSummaryResponse.DepartmentInsight(name, count, attendance / count, late, absent,
                    change / count, anomalies);
        }
    }
}