package com.attendai.leave.controller;

import com.attendai.leave.dto.LeaveRequestDto;
import com.attendai.leave.dto.LeaveResponse;
import com.attendai.leave.dto.PagedResponse;
import com.attendai.leave.dto.ReviewRequest;
import com.attendai.leave.entity.LeaveStatus;
import com.attendai.leave.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/leave/requests")
@Tag(name = "Leave Requests", description = "Submit, approve, reject and track leave requests")
public class LeaveRequestController {

    /** Roles permitted to approve or reject leave requests. */
    private static final Set<String> REVIEWER_ROLES = Set.of("ADMIN", "HR", "MANAGER");

    private final LeaveRequestService service;

    public LeaveRequestController(LeaveRequestService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Submit a new leave request")
    public ResponseEntity<LeaveResponse> submit(
            @Valid @RequestBody LeaveRequestDto dto,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(dto, email, role));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave request by ID")
    public LeaveResponse getById(@PathVariable Long id) { return service.getById(id); }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Paginated leave history for an employee")
    public PagedResponse<LeaveResponse> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.getByEmployee(employeeId, status, year,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/pending")
    @Operation(summary = "All pending requests — HR/Manager dashboard")
    public List<LeaveResponse> getPending() { return service.getPending(); }

    @GetMapping
    @Operation(summary = "All leave requests with filters (HR/Admin)")
    public PagedResponse<LeaveResponse> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.getAll(employeeId, status, year,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve a leave request (HR/Manager/Admin only)")
    public LeaveResponse approve(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewRequest review,
            @RequestHeader("X-Auth-User-Id") String reviewerId,
            @RequestHeader("X-Auth-User-Role") String reviewerRole) {
        assertReviewerRole(reviewerRole);
        return service.approve(id, review, parseLong(reviewerId));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject a leave request (HR/Manager/Admin only)")
    public LeaveResponse reject(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewRequest review,
            @RequestHeader("X-Auth-User-Id") String reviewerId,
            @RequestHeader("X-Auth-User-Role") String reviewerRole) {
        assertReviewerRole(reviewerRole);
        return service.reject(id, review, parseLong(reviewerId));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel own leave request (employee)")
    public LeaveResponse cancel(
            @PathVariable Long id,
            @RequestParam Long employeeId) {
        return service.cancel(id, employeeId);
    }

    /**
     * Used by attendance-service to check if an employee has approved leave on a date.
     * Returns 200 with leave info or 404 if no approved leave exists.
     */
    @GetMapping("/approved/check")
    @Operation(summary = "Check if employee has approved leave on a date (internal use)")
    public ResponseEntity<LeaveResponse> checkApprovedLeave(
            @RequestParam Long employeeId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return service.getApprovedLeaveForDate(employeeId, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Enforce that only ADMIN, HR, or MANAGER roles can approve/reject.
     * Returns 403 for any other role so that authorization is server-side,
     * not just hidden in the frontend.
     */
    private void assertReviewerRole(String role) {
        if (role == null || !REVIEWER_ROLES.contains(role.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only HR, Manager, or Admin can approve or reject leave requests");
        }
    }

    private Long parseLong(String value) {
        try { return Long.parseLong(value); } catch (Exception e) { return null; }
    }
}

