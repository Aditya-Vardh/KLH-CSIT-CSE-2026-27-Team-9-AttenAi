package com.attendai.attendance.service;

import com.attendai.attendance.client.EmployeeClient;
import com.attendai.attendance.client.dto.EmployeeSummary;
import com.attendai.attendance.dto.AnalyticsResponse;
import com.attendai.attendance.dto.AnalyticsResponse.DailyBreakdown;
import com.attendai.attendance.dto.AnalyticsResponse.EmployeeAttendanceStat;
import com.attendai.attendance.dto.AttendanceResponse;
import com.attendai.attendance.dto.CheckInRequest;
import com.attendai.attendance.dto.CheckOutRequest;
import com.attendai.attendance.dto.ManualAttendanceRequest;
import com.attendai.attendance.dto.MonthlySummary;
import com.attendai.attendance.dto.PagedResponse;
import com.attendai.attendance.entity.Attendance;
import com.attendai.attendance.entity.AttendanceStatus;
import com.attendai.attendance.exception.BadRequestException;
import com.attendai.attendance.exception.ResourceNotFoundException;
import com.attendai.attendance.repository.AttendanceRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeClient employeeClient;

    /** Configurable standard check-in time; default 09:00. */
    @Value("${attendai.attendance.expected-check-in:09:00}")
    private String expectedCheckInConfig;

    /** Standard workday hours for percentage calculation; default 8.0. */
    @Value("${attendai.attendance.standard-hours:8.0}")
    private double standardHours;

    public AttendanceService(AttendanceRepository attendanceRepository,
                              EmployeeClient employeeClient) {
        this.attendanceRepository = attendanceRepository;
        this.employeeClient = employeeClient;
    }

    // ── Check In ──────────────────────────────────────────────────────────────

    public AttendanceResponse checkIn(CheckInRequest request, String callerEmail, String callerRole) {
        LocalDate date = request.attendanceDate() != null ? request.attendanceDate() : LocalDate.now();
        LocalTime time = request.checkInTime() != null ? request.checkInTime() : LocalTime.now();

        // Verify employee exists via Feign
        employeeClient.getSummaryById(request.employeeId(), callerEmail, callerRole);

        if (attendanceRepository.existsByEmployeeIdAndAttendanceDate(request.employeeId(), date)) {
            throw new BadRequestException(
                    "Attendance record already exists for employee " + request.employeeId() + " on " + date);
        }

        LocalTime expectedCheckIn = parseExpectedCheckIn();
        boolean late = time.isAfter(expectedCheckIn);
        int lateMinutes = late ? (int) ChronoUnit.MINUTES.between(expectedCheckIn, time) : 0;

        Attendance a = new Attendance();
        a.setEmployeeId(request.employeeId());
        a.setAttendanceDate(date);
        a.setCheckInTime(time);
        a.setExpectedCheckIn(expectedCheckIn);
        a.setLateArrival(late);
        a.setLateMinutes(lateMinutes);
        a.setStatus(late ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);
        a.setNote(request.note());
        return AttendanceResponse.from(attendanceRepository.save(a));
    }

    // ── Check Out ─────────────────────────────────────────────────────────────

    public AttendanceResponse checkOut(CheckOutRequest request, String callerEmail, String callerRole) {
        LocalDate today = LocalDate.now();
        LocalTime time = request.checkOutTime() != null ? request.checkOutTime() : LocalTime.now();

        Attendance a = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(request.employeeId(), today)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No check-in record found for employee " + request.employeeId() + " today"));

        if (a.getCheckOutTime() != null) {
            throw new BadRequestException("Employee has already checked out today");
        }

        a.setCheckOutTime(time);

        // Calculate working hours
        if (a.getCheckInTime() != null) {
            double hours = ChronoUnit.MINUTES.between(a.getCheckInTime(), time) / 60.0;
            a.setWorkingHours(Math.max(0, hours));
            if (hours < standardHours / 2) {
                a.setStatus(AttendanceStatus.HALF_DAY);
            }
        }
        if (request.note() != null) {
            a.setNote(request.note());
        }
        return AttendanceResponse.from(attendanceRepository.save(a));
    }

    // ── Manual / HR Override ──────────────────────────────────────────────────

    public AttendanceResponse upsert(ManualAttendanceRequest request, String callerEmail, String callerRole) {
        employeeClient.getSummaryById(request.employeeId(), callerEmail, callerRole);

        Attendance a = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(request.employeeId(), request.attendanceDate())
                .orElse(new Attendance());

        a.setEmployeeId(request.employeeId());
        a.setAttendanceDate(request.attendanceDate());
        a.setCheckInTime(request.checkInTime());
        a.setCheckOutTime(request.checkOutTime());
        a.setStatus(request.status());
        a.setNote(request.note());

        if (request.checkInTime() != null && request.checkOutTime() != null) {
            double hours = ChronoUnit.MINUTES.between(request.checkInTime(), request.checkOutTime()) / 60.0;
            a.setWorkingHours(Math.max(0, hours));
        }

        LocalTime expectedCheckIn = parseExpectedCheckIn();
        a.setExpectedCheckIn(expectedCheckIn);
        if (request.checkInTime() != null && request.checkInTime().isAfter(expectedCheckIn)) {
            a.setLateArrival(true);
            a.setLateMinutes((int) ChronoUnit.MINUTES.between(expectedCheckIn, request.checkInTime()));
        } else {
            a.setLateArrival(false);
            a.setLateMinutes(0);
        }

        return AttendanceResponse.from(attendanceRepository.save(a));
    }

    // ── History ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<AttendanceResponse> getHistory(Long employeeId, LocalDate from, LocalDate to,
                                                         AttendanceStatus status, Pageable pageable) {
        return PagedResponse.from(
                attendanceRepository.findHistory(employeeId, from, to, status, pageable)
                        .map(AttendanceResponse::from));
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getByEmployeeAndDate(Long employeeId, LocalDate date) {
        return AttendanceResponse.from(
                attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, date)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No attendance record for employee " + employeeId + " on " + date)));
    }

    // ── Monthly Summary ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public MonthlySummary getMonthlySummary(Long employeeId, int year, int month) {
        long presentDays = attendanceRepository.countPresentDays(employeeId, year, month);
        long lateDays    = attendanceRepository.countLateDays(employeeId, year, month);
        Double totalHoursRaw = attendanceRepository.sumWorkingHours(employeeId, year, month);
        double totalHours = totalHoursRaw != null ? totalHoursRaw : 0.0;

        // Count working days in the month (Mon–Fri)
        YearMonth ym = YearMonth.of(year, month);
        long workingDays = ym.atDay(1).datesUntil(ym.atEndOfMonth().plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();

        long absentDays = Math.max(0, workingDays - presentDays);
        double percentage = workingDays == 0 ? 0 : Math.round((presentDays * 100.0 / workingDays) * 10) / 10.0;
        double avgHours = presentDays == 0 ? 0 : Math.round((totalHours / presentDays) * 10) / 10.0;

        return new MonthlySummary(employeeId, year, month, workingDays,
                presentDays, absentDays, lateDays, percentage, totalHours, avgHours);
    }

    // ── Late Arrivals ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getLateArrivals(LocalDate from, LocalDate to) {
        return attendanceRepository.findLateArrivals(from, to)
                .stream().map(AttendanceResponse::from).toList();
    }

    // ── Analytics ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(List<Long> employeeIds, LocalDate from, LocalDate to,
                                           Map<Long, String> employeeNames) {
        List<Attendance> records = attendanceRepository.findByEmployeeIdsAndDateRange(employeeIds, from, to);

        // Daily breakdown
        Map<LocalDate, long[]> byDay = records.stream()
                .collect(Collectors.groupingBy(Attendance::getAttendanceDate,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            long present = list.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT
                                    || a.getStatus() == AttendanceStatus.LATE
                                    || a.getStatus() == AttendanceStatus.HALF_DAY).count();
                            long absent  = list.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
                            long late    = list.stream().filter(Attendance::isLateArrival).count();
                            return new long[]{present, absent, late};
                        })));

        List<DailyBreakdown> dailyBreakdowns = new ArrayList<>();
        from.datesUntil(to.plusDays(1)).forEach(d -> {
            long[] counts = byDay.getOrDefault(d, new long[]{0, 0, 0});
            dailyBreakdowns.add(new DailyBreakdown(d.toString(), counts[0], counts[1], counts[2]));
        });

        // Per-employee stats
        Map<Long, List<Attendance>> byEmployee = records.stream()
                .collect(Collectors.groupingBy(Attendance::getEmployeeId));

        long totalWorkingDays = from.datesUntil(to.plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();

        List<EmployeeAttendanceStat> employeeStats = new ArrayList<>();
        for (Long empId : employeeIds) {
            List<Attendance> empRecords = byEmployee.getOrDefault(empId, List.of());
            long present = empRecords.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT
                    || a.getStatus() == AttendanceStatus.LATE
                    || a.getStatus() == AttendanceStatus.HALF_DAY).count();
            long late = empRecords.stream().filter(Attendance::isLateArrival).count();
            double pct = totalWorkingDays == 0 ? 0 : Math.round((present * 100.0 / totalWorkingDays) * 10) / 10.0;
            String rating = pct >= 95 ? "EXCELLENT" : pct >= 85 ? "GOOD" : pct >= 70 ? "AVERAGE" : "POOR";
            String name = employeeNames.getOrDefault(empId, "Employee #" + empId);
            employeeStats.add(new EmployeeAttendanceStat(empId, name, present, late, pct, rating));
        }

        long totalPresent = records.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT
                || a.getStatus() == AttendanceStatus.LATE
                || a.getStatus() == AttendanceStatus.HALF_DAY).count();
        long totalAbsent  = records.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long totalLate    = records.stream().filter(Attendance::isLateArrival).count();
        double totalHrs   = records.stream().filter(a -> a.getWorkingHours() != null)
                .mapToDouble(Attendance::getWorkingHours).sum();
        double avgPct = employeeStats.isEmpty() ? 0
                : employeeStats.stream().mapToDouble(EmployeeAttendanceStat::attendancePercentage).average().orElse(0);

        String period = from + " to " + to;
        return new AnalyticsResponse(period, employeeIds.size(),
                Math.round(avgPct * 10) / 10.0,
                totalPresent, totalAbsent, totalLate,
                Math.round(totalHrs * 10) / 10.0,
                dailyBreakdowns, employeeStats);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void delete(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendance record not found with id: " + id);
        }
        attendanceRepository.deleteById(id);
    }

    // ── AI-generated attendance helper ────────────────────────────────────────

    /**
     * Called by AI Agent Service to mark expected-late attendance via natural language input.
     */
    public AttendanceResponse markAiGenerated(Long employeeId, LocalDate date, LocalTime expectedArrival,
                                               String note, String callerEmail, String callerRole) {
        employeeClient.getSummaryById(employeeId, callerEmail, callerRole);
        if (attendanceRepository.existsByEmployeeIdAndAttendanceDate(employeeId, date)) {
            throw new BadRequestException("Attendance already recorded for " + employeeId + " on " + date);
        }
        LocalTime expectedCheckIn = parseExpectedCheckIn();
        boolean late = expectedArrival != null && expectedArrival.isAfter(expectedCheckIn);
        int lateMinutes = late ? (int) ChronoUnit.MINUTES.between(expectedCheckIn, expectedArrival) : 0;

        Attendance a = new Attendance();
        a.setEmployeeId(employeeId);
        a.setAttendanceDate(date);
        a.setCheckInTime(expectedArrival);
        a.setExpectedCheckIn(expectedCheckIn);
        a.setLateArrival(late);
        a.setLateMinutes(lateMinutes);
        a.setStatus(late ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);
        a.setNote(note);
        a.setAiGenerated(true);
        return AttendanceResponse.from(attendanceRepository.save(a));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private LocalTime parseExpectedCheckIn() {
        try {
            return LocalTime.parse(expectedCheckInConfig);
        } catch (Exception e) {
            return LocalTime.of(9, 0);
        }
    }
}
