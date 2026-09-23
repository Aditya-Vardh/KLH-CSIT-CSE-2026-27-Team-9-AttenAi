package com.attendai.ai;

import com.attendai.ai.client.AttendanceClient;
import com.attendai.ai.client.EmployeeClient;
import com.attendai.ai.client.dto.EmployeeSummary;
import com.attendai.ai.client.dto.MonthlySummary;
import com.attendai.ai.service.WorkforceAnalyticsService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkforceAnalyticsServiceTest {
    private final AttendanceClient attendance = mock(AttendanceClient.class);
    private final EmployeeClient employees = mock(EmployeeClient.class);
    private final WorkforceAnalyticsService service = new WorkforceAnalyticsService(attendance, employees);

    @Test
    void calculatesPeriodChangeAnomaliesAndDepartments() {
        when(attendance.getMonthlySummary(eq(7L), eq(2026), eq(9), anyString(), anyString()))
                .thenReturn(new MonthlySummary(7L, 2026, 9, 20, 12, 8, 3, 60, 120, 6));
        when(attendance.getMonthlySummary(eq(7L), eq(2026), eq(8), anyString(), anyString()))
                .thenReturn(new MonthlySummary(7L, 2026, 8, 20, 18, 2, 1, 90, 150, 7.5));
        when(employees.getSummaryById(eq(7L), anyString(), anyString()))
                .thenReturn(new EmployeeSummary(7L, 70L, "E7", "Asha Rao", "asha@example.com", "HR", "Analyst", 20));

        var result = service.summary(List.of(7L), LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30), "hr@example.com", "HR");

        assertEquals(1, result.employeesCovered());
        assertEquals(-30.0, result.attendanceChangePctPoints());
        assertEquals(2, result.anomalies().size());
        assertEquals("HR", result.departments().get(0).department());
        assertTrue(result.departments().get(0).anomalyCount() > 0);
    }
}