package com.attendai.leave.controller;

import com.attendai.leave.dto.PagedResponse;
import com.attendai.leave.dto.ReviewRequest;
import com.attendai.leave.dto.WFHRequestDto;
import com.attendai.leave.dto.WFHResponse;
import com.attendai.leave.service.WFHRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/leave/wfh")
@Tag(name = "WFH Requests", description = "Submit, approve, reject and track Work From Home requests")
public class WFHRequestController {

    private static final Set<String> REVIEWER_ROLES = Set.of("ADMIN", "HR", "MANAGER");

    private final WFHRequestService service;

    public WFHRequestController(WFHRequestService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Submit a new WFH request")
    public ResponseEntity<WFHResponse> submit(
            @Valid @RequestBody WFHRequestDto dto,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(dto, email, role));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get WFH request by ID")
    public WFHResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Paginated WFH history for an employee")
    public PagedResponse<WFHResponse> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.getByEmployee(employeeId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/pending")
    @Operation(summary = "All pending WFH requests — HR/Manager dashboard")
    public List<WFHResponse> getPending(
            @RequestHeader("X-Auth-User-Role") String role) {
        assertReviewerRole(role);
        return service.getPending();
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve a WFH request (HR/Manager/Admin only)")
    public WFHResponse approve(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewRequest review,
            @RequestHeader("X-Auth-User-Id") String reviewerId,
            @RequestHeader("X-Auth-User-Role") String reviewerRole) {
        assertReviewerRole(reviewerRole);
        return service.approve(id, review, parseLong(reviewerId));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject a WFH request (HR/Manager/Admin only)")
    public WFHResponse reject(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewRequest review,
            @RequestHeader("X-Auth-User-Id") String reviewerId,
            @RequestHeader("X-Auth-User-Role") String reviewerRole) {
        assertReviewerRole(reviewerRole);
        return service.reject(id, review, parseLong(reviewerId));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel own WFH request (employee)")
    public WFHResponse cancel(
            @PathVariable Long id,
            @RequestParam Long employeeId) {
        return service.cancel(id, employeeId);
    }

    /**
     * Used by attendance-service Feign client to check if employee has approved WFH for a date.
     */
    @GetMapping("/approved/check")
    @Operation(summary = "Check if employee has approved WFH for a date (internal use)")
    public ResponseEntity<WFHResponse> checkApprovedWfh(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getApprovedWfhForDate(employeeId, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void assertReviewerRole(String role) {
        if (role == null || !REVIEWER_ROLES.contains(role.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only HR, Manager, or Admin can approve or reject WFH requests");
        }
    }

    private Long parseLong(String value) {
        try { return Long.parseLong(value); } catch (Exception e) { return null; }
    }
}
