package com.attendai.employee.controller;

import com.attendai.employee.dto.EmployeeRequest;
import com.attendai.employee.dto.EmployeeResponse;
import com.attendai.employee.dto.EmployeeSummary;
import com.attendai.employee.dto.PagedResponse;
import com.attendai.employee.entity.EmployeeStatus;
import com.attendai.employee.service.EmployeeService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "List employees with search, filter and pagination")
    public PagedResponse<EmployeeResponse> list(
            @Parameter(description = "Search by name, email or employee code")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by department ID")
            @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Filter by designation ID")
            @RequestParam(required = false) Long designationId,
            @Parameter(description = "Filter by employment status")
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sort) {
        return employeeService.list(search, departmentId, designationId, status,
                PageRequest.of(page, size, Sort.by(sort)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get employee by auth-service userId")
    public EmployeeResponse getByUserId(@PathVariable Long userId) {
        return employeeService.getByUserId(userId);
    }

    /**
     * Internal lightweight endpoint consumed by Attendance and Leave services via OpenFeign.
     */
    @GetMapping("/{id}/summary")
    @Operation(summary = "Get lightweight employee summary by ID (used internally by other services)")
    public EmployeeSummary getSummaryById(@PathVariable Long id) {
        return employeeService.getSummaryById(id);
    }

    @GetMapping("/by-user/{userId}/summary")
    @Operation(summary = "Get lightweight employee summary by userId (used internally)")
    public EmployeeSummary getSummaryByUserId(@PathVariable Long userId) {
        return employeeService.getSummaryByUserId(userId);
    }

    @PostMapping
    @Operation(summary = "Create a new employee")
    public ResponseEntity<EmployeeResponse> create(
            @Valid @RequestBody EmployeeRequest request,
            @RequestHeader("X-Auth-User-Email") String actorEmail,
            @RequestHeader("X-Auth-User-Role")  String actorRole) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.create(request, actorEmail, actorRole));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee (full update)")
    public EmployeeResponse update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request,
            @RequestHeader("X-Auth-User-Email") String actorEmail,
            @RequestHeader("X-Auth-User-Role")  String actorRole) {
        return employeeService.update(id, request, actorEmail, actorRole);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee record")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-Auth-User-Email") String actorEmail,
            @RequestHeader("X-Auth-User-Role")  String actorRole) {
        employeeService.delete(id, actorEmail, actorRole);
        return ResponseEntity.noContent().build();
    }
}
