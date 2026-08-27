package com.attendai.ai.client;

import com.attendai.ai.client.dto.LeaveBalanceInfo;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "leave-service", path = "/api/leave")
public interface LeaveClient {

    @GetMapping("/balance/{employeeId}")
    List<LeaveBalanceInfo> getBalance(@PathVariable Long employeeId,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role);
}
