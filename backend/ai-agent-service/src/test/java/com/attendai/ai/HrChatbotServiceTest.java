package com.attendai.ai;

import com.attendai.ai.client.AttendanceClient;
import com.attendai.ai.client.EmployeeClient;
import com.attendai.ai.client.LeaveClient;
import com.attendai.ai.client.dto.EmployeeSummary;
import com.attendai.ai.dto.ChatRequest;
import com.attendai.ai.service.HrChatbotService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HrChatbotServiceTest {
    private final EmployeeClient employees = mock(EmployeeClient.class);
    private final AttendanceClient attendance = mock(AttendanceClient.class);
    private final HrChatbotService service = new HrChatbotService(employees, attendance, mock(LeaveClient.class));

    @Test
    void employeeCannotReadAnotherEmployeesChatData() {
        when(employees.getSummaryByUserId(eq(70L), anyString(), anyString()))
                .thenReturn(new EmployeeSummary(8L, 70L, "E8", "Other Employee", "other@example.com", "HR", "Analyst", 20));

        assertThrows(ResponseStatusException.class,
                () -> service.chat(new ChatRequest(7L, "What is my attendance?"),
                        "employee@example.com", "EMPLOYEE", "70"));
        verifyNoInteractions(attendance);
    }
}