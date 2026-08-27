package com.attendai.leave.service;

import com.attendai.leave.client.EmployeeClient;
import com.attendai.leave.dto.PagedResponse;
import com.attendai.leave.dto.ReviewRequest;
import com.attendai.leave.dto.WFHRequestDto;
import com.attendai.leave.dto.WFHResponse;
import com.attendai.leave.entity.LeaveStatus;
import com.attendai.leave.entity.WFHRequest;
import com.attendai.leave.exception.BadRequestException;
import com.attendai.leave.exception.ResourceNotFoundException;
import com.attendai.leave.repository.WFHRequestRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WFHRequestService {

    private static final Logger log = LoggerFactory.getLogger(WFHRequestService.class);

    private final WFHRequestRepository wfhRepo;
    private final EmployeeClient employeeClient;

    public WFHRequestService(WFHRequestRepository wfhRepo, EmployeeClient employeeClient) {
        this.wfhRepo = wfhRepo;
        this.employeeClient = employeeClient;
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    public WFHResponse submit(WFHRequestDto dto, String callerEmail, String callerRole) {
        employeeClient.getSummaryById(dto.employeeId(), callerEmail, callerRole);

        if (dto.endDate().isBefore(dto.startDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        if (wfhRepo.hasOverlappingApproved(dto.employeeId(), dto.startDate(), dto.endDate())) {
            throw new BadRequestException("An approved WFH request already exists for this period");
        }

        WFHRequest req = new WFHRequest();
        req.setEmployeeId(dto.employeeId());
        req.setStartDate(dto.startDate());
        req.setEndDate(dto.endDate());
        req.setReason(dto.reason());
        req.setStatus(LeaveStatus.PENDING);
        WFHRequest saved = wfhRepo.save(req);
        log.info("WFH request submitted: employee={} dates={} to {}", dto.employeeId(), dto.startDate(), dto.endDate());
        return WFHResponse.from(saved);
    }

    // ── Get by ID ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public WFHResponse getById(Long id) {
        return WFHResponse.from(findOrThrow(id));
    }

    // ── Employee history ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<WFHResponse> getByEmployee(Long employeeId, Pageable pageable) {
        return PagedResponse.from(
                wfhRepo.findByEmployeeId(employeeId, pageable).map(WFHResponse::from));
    }

    // ── Pending (Admin) ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<WFHResponse> getPending() {
        return wfhRepo.findByStatus(LeaveStatus.PENDING).stream().map(WFHResponse::from).toList();
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    public WFHResponse approve(Long id, ReviewRequest review, Long reviewerId) {
        WFHRequest req = findOrThrow(id);
        assertPending(req);
        req.setStatus(LeaveStatus.APPROVED);
        req.setReviewedBy(reviewerId);
        req.setReviewedAt(Instant.now());
        if (review != null && review.comment() != null) {
            req.setReviewComment(review.comment());
        }
        log.info("WFH request approved: id={} by reviewer={}", id, reviewerId);
        return WFHResponse.from(wfhRepo.save(req));
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    public WFHResponse reject(Long id, ReviewRequest review, Long reviewerId) {
        WFHRequest req = findOrThrow(id);
        assertPending(req);
        req.setStatus(LeaveStatus.REJECTED);
        req.setReviewedBy(reviewerId);
        req.setReviewedAt(Instant.now());
        if (review != null && review.comment() != null) {
            req.setReviewComment(review.comment());
        }
        log.info("WFH request rejected: id={} by reviewer={}", id, reviewerId);
        return WFHResponse.from(wfhRepo.save(req));
    }

    // ── Cancel (employee) ─────────────────────────────────────────────────────

    public WFHResponse cancel(Long id, Long employeeId) {
        WFHRequest req = findOrThrow(id);
        if (!req.getEmployeeId().equals(employeeId)) {
            throw new BadRequestException("Cannot cancel another employee's WFH request");
        }
        if (req.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only PENDING WFH requests can be cancelled");
        }
        req.setStatus(LeaveStatus.CANCELLED);
        log.info("WFH request cancelled: id={} by employee={}", id, employeeId);
        return WFHResponse.from(wfhRepo.save(req));
    }

    // ── Check approved WFH for date ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public boolean hasApprovedWfh(Long employeeId, LocalDate date) {
        return wfhRepo.findActiveApprovedRequest(employeeId, date).isPresent();
    }

    @Transactional(readOnly = true)
    public Optional<WFHResponse> getApprovedWfhForDate(Long employeeId, LocalDate date) {
        return wfhRepo.findActiveApprovedRequest(employeeId, date).map(WFHResponse::from);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private WFHRequest findOrThrow(Long id) {
        return wfhRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WFH request not found: " + id));
    }

    private void assertPending(WFHRequest req) {
        if (req.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("WFH request is already " + req.getStatus());
        }
    }
}
