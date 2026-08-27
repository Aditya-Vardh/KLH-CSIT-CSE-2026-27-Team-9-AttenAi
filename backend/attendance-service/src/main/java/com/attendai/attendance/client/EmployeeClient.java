package com.attendai.attendance.client;

import com.attendai.attendance.client.dto.EmployeeSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client that calls the employee-service via Eureka service discovery.
 * Uses the /summary endpoint to avoid over-fetching.
 */
@FeignClient(name = "employee-service", path = "/api/employees")
public interface EmployeeClient {

    @GetMapping("/{id}/summary")
    EmployeeSummary getSummaryById(
            @PathVariable("id") Long id,
            @RequestHeader("X-Auth-User-Email") String userEmail,
            @RequestHeader("X-Auth-User-Role") String userRole);

    @GetMapping("/by-user/{userId}/summary")
    EmployeeSummary getSummaryByUserId(
            @PathVariable("userId") Long userId,
            @RequestHeader("X-Auth-User-Email") String userEmail,
            @RequestHeader("X-Auth-User-Role") String userRole);
}
