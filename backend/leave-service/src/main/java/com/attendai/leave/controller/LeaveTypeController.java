package com.attendai.leave.controller;

import com.attendai.leave.dto.LeaveTypeRequest;
import com.attendai.leave.dto.LeaveTypeResponse;
import com.attendai.leave.service.LeaveTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leave/types")
@Tag(name = "Leave Types", description = "Manage leave type catalogue")
public class LeaveTypeController {

    private final LeaveTypeService service;

    public LeaveTypeController(LeaveTypeService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "List all active leave types")
    public List<LeaveTypeResponse> listActive(@RequestParam(defaultValue = "true") boolean activeOnly) {
        return activeOnly ? service.listActive() : service.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave type by ID")
    public LeaveTypeResponse getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    @Operation(summary = "Create a new leave type (HR/Admin)")
    public ResponseEntity<LeaveTypeResponse> create(@Valid @RequestBody LeaveTypeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a leave type")
    public LeaveTypeResponse update(@PathVariable Long id, @Valid @RequestBody LeaveTypeRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a leave type")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
