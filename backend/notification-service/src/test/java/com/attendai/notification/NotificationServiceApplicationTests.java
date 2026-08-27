package com.attendai.notification;

import com.attendai.notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class NotificationServiceApplicationTests {

    @MockBean
    EmailService emailService;

    @Test
    void contextLoads() {}
}
