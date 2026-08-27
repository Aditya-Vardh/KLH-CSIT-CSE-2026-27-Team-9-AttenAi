package com.attendai.attendance.controller;

import com.attendai.attendance.client.EmployeeClient;
import com.attendai.attendance.client.dto.EmployeeSummary;
import com.attendai.attendance.dto.AttendanceResponse;
import com.attendai.attendance.dto.CheckInRequest;
import com.attendai.attendance.dto.CheckOutRequest;
import com.attendai.attendance.dto.ManualAttendanceRequest;
import com.attendai.attendance.dto.MonthlySummary;
import com.attendai.attendance.dto.PagedResponse;
import com.attendai.attendance.entity.AttendanceStatus;
import com.attendai.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance", description = "Attendance management — check-in/out, history, analytics")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeClient employeeClient;

    public AttendanceController(AttendanceService attendanceService, EmployeeClient employeeClient) {
        this.attendanceService = attendanceService;
        this.employeeClient = employeeClient;
    }

    // ── Check In / Out ────────────────────────────────────────────────────────

    @PostMapping("/check-in")
    @Operation(summary = "Record employee check-in")
    public ResponseEntity<AttendanceResponse> checkIn(
            @Valid @RequestBody CheckInRequest request,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.checkIn(request, email, role));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Record employee check-out and compute working hours")
    public AttendanceResponse checkOut(
            @Valid @RequestBody CheckOutRequest request,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role) {
        return attendanceService.checkOut(request, email, role);
    }

    // ── HR Manual Override ────────────────────────────────────────────────────

    @PutMapping("/manual")
    @Operation(summary = "HR/Admin: create or update an attendance record directly")
    public AttendanceResponse upsert(
            @Valid @RequestBody ManualAttendanceRequest request,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role) {
        return attendanceService.upsert(request, email, role);
    }

    // ── History ───────────────────────────────────────────────────────────────

    @GetMapping("/history/{employeeId}")
    @Operation(summary = "Paginated attendance history for an employee")
    public PagedResponse<AttendanceResponse> getHistory(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveTo   = to   != null ? to   : LocalDate.now();
        return attendanceService.getHistory(employeeId, effectiveFrom, effectiveTo, status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "attendanceDate")));
    }

    @GetMapping("/{employeeId}/{date}")
    @Operation(summary = "Get a single attendance record by employee and date")
    public AttendanceResponse getByEmployeeAndDate(
            @PathVariable Long employeeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.getByEmployeeAndDate(employeeId, date);
    }

    // ── Monthly Summary ───────────────────────────────────────────────────────

    @GetMapping("/monthly/{employeeId}")
    @Operation(summary = "Monthly attendance summary (percentage, hours, late days)")
    public MonthlySummary getMonthlySummary(
            @PathVariable Long employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        return attendanceService.getMonthlySummary(employeeId, year, month);
    }

    // ── Late Arrivals ─────────────────────────────────────────────────────────

    @GetMapping("/late-arrivals")
    @Operation(summary = "All late arrivals in a date range")
    public List<AttendanceResponse> getLateArrivals(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return attendanceService.getLateArrivals(from, to);
    }

    // ── Analytics ─────────────────────────────────────────────────────────────

    @GetMapping("/analytics")
    @Operation(summary = "Aggregated analytics for a set of employees and date range")
    public com.attendai.attendance.dto.AnalyticsResponse getAnalytics(
            @RequestParam List<Long> employeeIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role) {

        // Fetch employee names via Feign for analytics labels
        Map<Long, String> employeeNames = employeeIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            try {
                                EmployeeSummary s = employeeClient.getSummaryById(id, email, role);
                                return s.fullName();
                            } catch (Exception e) {
                                return "Employee #" + id;
                            }
                        }));

        return attendanceService.getAnalytics(employeeIds, from, to, employeeNames);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an attendance record (HR/Admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
