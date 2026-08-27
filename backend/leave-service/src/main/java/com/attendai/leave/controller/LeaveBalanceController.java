package com.attendai.leave.controller;

import com.attendai.leave.dto.LeaveBalanceResponse;
import com.attendai.leave.service.LeaveBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leave/balance")
@Tag(name = "Leave Balance", description = "Query and initialize leave balances")
public class LeaveBalanceController {

    private final LeaveBalanceService service;

    public LeaveBalanceController(LeaveBalanceService service) { this.service = service; }

    @GetMapping("/{employeeId}")
    @Operation(summary = "Get current-year leave balance for an employee (creates missing records)")
    public List<LeaveBalanceResponse> getBalance(@PathVariable Long employeeId) {
        return service.getOrCreateBalances(employeeId);
    }

    @GetMapping("/{employeeId}/year/{year}")
    @Operation(summary = "Get leave balance for a specific year")
    public List<LeaveBalanceResponse> getForYear(@PathVariable Long employeeId,
                                                  @PathVariable int year) {
        return service.getForYear(employeeId, year);
    }

    @PostMapping("/{employeeId}/initialize")
    @Operation(summary = "Initialize leave balances for a newly onboarded employee")
    public void initialize(@PathVariable Long employeeId) {
        service.initializeForEmployee(employeeId);
    }
}
