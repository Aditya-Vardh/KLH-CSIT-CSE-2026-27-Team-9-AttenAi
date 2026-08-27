package com.attendai.attendance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import com.attendai.attendance.client.EmployeeClient;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class AttendanceServiceApplicationTests {

    /** Mock the Feign client so no actual HTTP call is made during context load test. */
    @MockBean
    private EmployeeClient employeeClient;

    @Test
    void contextLoads() {
    }
}
