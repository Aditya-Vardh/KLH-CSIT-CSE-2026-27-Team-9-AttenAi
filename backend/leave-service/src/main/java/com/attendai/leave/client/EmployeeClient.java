package com.attendai.leave.client;

import com.attendai.leave.client.dto.EmployeeSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "employee-service", path = "/api/employees")
public interface EmployeeClient {

    @GetMapping("/{id}/summary")
    EmployeeSummary getSummaryById(
            @PathVariable("id") Long id,
            @RequestHeader("X-Auth-User-Email") String userEmail,
            @RequestHeader("X-Auth-User-Role") String userRole);
}
