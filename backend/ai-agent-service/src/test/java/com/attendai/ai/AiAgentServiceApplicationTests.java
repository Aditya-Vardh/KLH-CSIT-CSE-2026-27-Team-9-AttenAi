package com.attendai.ai;

import com.attendai.ai.client.AttendanceClient;
import com.attendai.ai.client.EmployeeClient;
import com.attendai.ai.client.LeaveClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class AiAgentServiceApplicationTests {
    @MockBean EmployeeClient employeeClient;
    @MockBean AttendanceClient attendanceClient;
    @MockBean LeaveClient leaveClient;

    @Test void contextLoads() {}
}
