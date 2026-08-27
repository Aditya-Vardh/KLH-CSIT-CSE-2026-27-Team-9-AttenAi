package com.attendai.leave;

import com.attendai.leave.client.EmployeeClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class LeaveServiceApplicationTests {

    @MockBean
    private EmployeeClient employeeClient;

    @Test
    void contextLoads() {}
}
