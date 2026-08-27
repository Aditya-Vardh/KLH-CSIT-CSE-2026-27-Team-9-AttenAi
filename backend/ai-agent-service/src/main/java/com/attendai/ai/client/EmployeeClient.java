package com.attendai.ai.client;

import com.attendai.ai.client.dto.EmployeeSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "employee-service", path = "/api/employees")
public interface EmployeeClient {

    @GetMapping("/{id}/summary")
    EmployeeSummary getSummaryById(@PathVariable Long id,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role);

    @GetMapping("/by-user/{userId}/summary")
    EmployeeSummary getSummaryByUserId(@PathVariable Long userId,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role);
}
