package com.attendai.employee.controller;

import com.attendai.employee.dto.DesignationRequest;
import com.attendai.employee.dto.DesignationResponse;
import com.attendai.employee.dto.PagedResponse;
import com.attendai.employee.service.DesignationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/designations")
@Tag(name = "Designations", description = "Designation management endpoints")
public class DesignationController {

    private final DesignationService designationService;

    public DesignationController(DesignationService designationService) {
        this.designationService = designationService;
    }

    @GetMapping
    @Operation(summary = "List designations with optional search and pagination")
    public PagedResponse<DesignationResponse> list(
            @Parameter(description = "Search by title or grade") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sort) {
        return designationService.list(search, active,
                PageRequest.of(page, size, Sort.by(sort)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a designation by ID")
    public DesignationResponse getById(@PathVariable Long id) {
        return designationService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new designation")
    public ResponseEntity<DesignationResponse> create(@Valid @RequestBody DesignationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(designationService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a designation")
    public DesignationResponse update(@PathVariable Long id, @Valid @RequestBody DesignationRequest request) {
        return designationService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a designation (soft delete)")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        designationService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hard delete a designation (only if not assigned)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        designationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
