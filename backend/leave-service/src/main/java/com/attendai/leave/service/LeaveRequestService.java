package com.attendai.leave.service;

import com.attendai.leave.client.EmployeeClient;
import com.attendai.leave.client.NotificationClient;
import com.attendai.leave.client.dto.EmployeeSummary;
import com.attendai.leave.dto.LeaveRequestDto;
import com.attendai.leave.dto.LeaveResponse;
import com.attendai.leave.dto.PagedResponse;
import com.attendai.leave.dto.ReviewRequest;
import com.attendai.leave.entity.LeaveRequest;
import com.attendai.leave.entity.LeaveStatus;
import com.attendai.leave.entity.LeaveType;
import com.attendai.leave.exception.BadRequestException;
import com.attendai.leave.exception.ResourceNotFoundException;
import com.attendai.leave.repository.LeaveRequestRepository;
import com.attendai.leave.repository.LeaveTypeRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LeaveRequestService {

    private static final Logger log = LoggerFactory.getLogger(LeaveRequestService.class);

    // System-level identity forwarded to other services via Feign on background operations
    private static final String SYSTEM_EMAIL = "system@attendai.internal";
    private static final String SYSTEM_ROLE  = "ADMIN";

    private final LeaveRequestRepository requestRepo;
    private final LeaveTypeRepository typeRepo;
    private final LeaveBalanceService balanceService;
    private final EmployeeClient employeeClient;
    private final NotificationClient notificationClient;

    public LeaveRequestService(LeaveRequestRepository requestRepo,
                                LeaveTypeRepository typeRepo,
                                LeaveBalanceService balanceService,
                                EmployeeClient employeeClient,
                                NotificationClient notificationClient) {
        this.requestRepo        = requestRepo;
        this.typeRepo           = typeRepo;
        this.balanceService     = balanceService;
        this.employeeClient     = employeeClient;
        this.notificationClient = notificationClient;
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    public LeaveResponse submit(LeaveRequestDto dto, String callerEmail, String callerRole) {
        // Verify employee exists
        employeeClient.getSummaryById(dto.employeeId(), callerEmail, callerRole);

        if (dto.endDate().isBefore(dto.startDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        LeaveType lt = typeRepo.findById(dto.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found: " + dto.leaveTypeId()));

        if (!lt.isActive()) {
            throw new BadRequestException("Leave type '" + lt.getName() + "' is not active");
        }

        if (requestRepo.hasOverlappingLeave(dto.employeeId(), dto.startDate(), dto.endDate())) {
            throw new BadRequestException("An overlapping leave request already exists for this period");
        }

        int days = countBusinessDays(dto.startDate(), dto.endDate());
        if (days == 0) {
            throw new BadRequestException("Leave period contains no working days");
        }

        // Check balance (unless allowNegative)
        if (!lt.isAllowNegative()) {
            int year = dto.startDate().getYear();
            int approved = requestRepo.sumApprovedDays(dto.employeeId(), lt.getId(), year);
            int pending  = requestRepo.sumPendingDays(dto.employeeId(), lt.getId(), year);
            int remaining = lt.getDefaultDays() - approved - pending;
            if (days > remaining) {
                throw new BadRequestException(
                        "Insufficient leave balance. Requested: " + days + ", Available: " + remaining);
            }
        }

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId(dto.employeeId());
        lr.setLeaveType(lt);
        lr.setStartDate(dto.startDate());
        lr.setEndDate(dto.endDate());
        lr.setTotalDays(days);
        lr.setReason(dto.reason());
        lr.setDocumentUrl(dto.documentUrl());

        LeaveResponse response = LeaveResponse.from(requestRepo.save(lr));
        // Refresh balance to reflect new pending
        balanceService.refresh(dto.employeeId(), lt.getId(), dto.startDate().getYear());

        // Fire-and-forget: notify the employee that their request is submitted
        notifyAsync(() -> {
            EmployeeSummary emp = employeeClient.getSummaryById(dto.employeeId(), callerEmail, callerRole);
            notificationClient.leaveSubmitted(
                    dto.employeeId(), emp.email(), emp.fullName(),
                    lt.getName(),
                    dto.startDate().toString(), dto.endDate().toString(),
                    days, response.id());
        });

        return response;
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    public LeaveResponse approve(Long id, ReviewRequest review, Long reviewerId) {
        LeaveRequest lr = findOrThrow(id);
        assertPending(lr);
        lr.setStatus(LeaveStatus.APPROVED);
        lr.setReviewComment(review != null ? review.comment() : null);
        lr.setReviewedBy(reviewerId);
        lr.setReviewedAt(Instant.now());
        LeaveResponse resp = LeaveResponse.from(requestRepo.save(lr));
        balanceService.refresh(lr.getEmployeeId(), lr.getLeaveType().getId(), lr.getStartDate().getYear());

        // Notify employee of approval
        final LeaveRequest snapshot = lr;
        notifyAsync(() -> {
            EmployeeSummary emp = employeeClient.getSummaryById(
                    snapshot.getEmployeeId(), SYSTEM_EMAIL, SYSTEM_ROLE);
            notificationClient.leaveApproved(
                    snapshot.getEmployeeId(), emp.email(), emp.fullName(),
                    snapshot.getLeaveType().getName(),
                    snapshot.getStartDate().toString(), snapshot.getEndDate().toString(),
                    snapshot.getTotalDays(), snapshot.getId());
        });

        return resp;
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    public LeaveResponse reject(Long id, ReviewRequest review, Long reviewerId) {
        LeaveRequest lr = findOrThrow(id);
        assertPending(lr);
        lr.setStatus(LeaveStatus.REJECTED);
        lr.setReviewComment(review != null ? review.comment() : null);
        lr.setReviewedBy(reviewerId);
        lr.setReviewedAt(Instant.now());
        LeaveResponse resp = LeaveResponse.from(requestRepo.save(lr));
        balanceService.refresh(lr.getEmployeeId(), lr.getLeaveType().getId(), lr.getStartDate().getYear());

        // Notify employee of rejection
        final LeaveRequest snapshot = lr;
        final String rejectReason = review != null ? review.comment() : null;
        notifyAsync(() -> {
            EmployeeSummary emp = employeeClient.getSummaryById(
                    snapshot.getEmployeeId(), SYSTEM_EMAIL, SYSTEM_ROLE);
            notificationClient.leaveRejected(
                    snapshot.getEmployeeId(), emp.email(), emp.fullName(),
                    snapshot.getLeaveType().getName(),
                    snapshot.getStartDate().toString(), snapshot.getEndDate().toString(),
                    snapshot.getTotalDays(), rejectReason, snapshot.getId());
        });

        return resp;
    }

    // ── Cancel / Withdraw ─────────────────────────────────────────────────────

    public LeaveResponse cancel(Long id, Long requestingEmployeeId) {
        LeaveRequest lr = findOrThrow(id);
        if (!lr.getEmployeeId().equals(requestingEmployeeId)) {
            throw new BadRequestException("You can only cancel your own leave requests");
        }
        if (lr.getStatus() == LeaveStatus.APPROVED && lr.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot cancel an approved leave that has already started");
        }
        if (lr.getStatus() == LeaveStatus.REJECTED || lr.getStatus() == LeaveStatus.CANCELLED) {
            throw new BadRequestException("Leave request is already " + lr.getStatus().name().toLowerCase());
        }
        lr.setStatus(LeaveStatus.CANCELLED);
        LeaveResponse resp = LeaveResponse.from(requestRepo.save(lr));
        balanceService.refresh(lr.getEmployeeId(), lr.getLeaveType().getId(), lr.getStartDate().getYear());
        return resp;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LeaveResponse getById(Long id) {
        return LeaveResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<LeaveResponse> getByEmployee(Long employeeId, LeaveStatus status,
                                                       Integer year, Pageable pageable) {
        return PagedResponse.from(
                requestRepo.findByEmployee(employeeId, status, year, pageable)
                        .map(LeaveResponse::from));
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getPending() {
        return requestRepo.findAllPending().stream().map(LeaveResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<LeaveResponse> getAll(Long employeeId, LeaveStatus status,
                                                Integer year, Pageable pageable) {
        return PagedResponse.from(
                requestRepo.findAll(employeeId, status, year, pageable)
                        .map(LeaveResponse::from));
    }

    // ── AI integration helper ─────────────────────────────────────────────────

    /**
     * AI Agent calls this to store its recommendation on a pending request.
     */
    public LeaveResponse attachAiRecommendation(Long id, String recommendation,
                                                 double confidenceScore) {
        LeaveRequest lr = findOrThrow(id);
        lr.setAiRecommended(true);
        lr.setAiRecommendation(recommendation);
        lr.setAiConfidenceScore(confidenceScore);
        return LeaveResponse.from(requestRepo.save(lr));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private LeaveRequest findOrThrow(Long id) {
        return requestRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));
    }

    private void assertPending(LeaveRequest lr) {
        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is not in PENDING state: " + lr.getStatus());
        }
    }

    /**
     * Count Mon–Fri days between startDate and endDate inclusive.
     */
    public static int countBusinessDays(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) return 0;
        return (int) start.datesUntil(end.plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
    }

    /**
     * Run a notification task on a background daemon thread (fire-and-forget).
     * Failures are logged but never propagate — notifications are non-blocking.
     * Uses a plain daemon Thread so this works on Java 17+.
     */
    private void notifyAsync(Runnable task) {
        Thread t = new Thread(() -> {
            try {
                task.run();
            } catch (Exception ex) {
                log.warn("Notification dispatch failed (non-critical): {}", ex.getMessage());
            }
        });
        t.setDaemon(true);
        t.setName("notify-async-" + t.getId());
        t.start();
    }

    // ── Leave date check (used by attendance-service) ─────────────────────────

    @Transactional(readOnly = true)
    public boolean hasApprovedLeaveOnDate(Long employeeId, LocalDate date) {
        return requestRepo.hasApprovedLeaveOnDate(employeeId, date);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<LeaveResponse> getApprovedLeaveForDate(Long employeeId, LocalDate date) {
        return requestRepo.findApprovedLeaveForDate(employeeId, date)
                .map(LeaveResponse::from);
    }
}

