package com.attendai.ai.service;

import com.attendai.ai.client.AttendanceClient;
import com.attendai.ai.client.EmployeeClient;
import com.attendai.ai.client.dto.EmployeeSummary;
import com.attendai.ai.dto.NlAttendanceRequest;
import com.attendai.ai.dto.NlAttendanceResponse;
import com.attendai.ai.service.NlpService.Intent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class NlAttendanceService {

    private final NlpService nlp;
    private final EmployeeClient employeeClient;
    private final AttendanceClient attendanceClient;

    public NlAttendanceService(NlpService nlp, EmployeeClient employeeClient,
                                AttendanceClient attendanceClient) {
        this.nlp = nlp;
        this.employeeClient = employeeClient;
        this.attendanceClient = attendanceClient;
    }

    public NlAttendanceResponse process(NlAttendanceRequest req, String callerEmail, String callerRole) {
        String msg = req.message();
        Intent intent = nlp.extractIntent(msg);
        LocalDate date = nlp.extractDate(msg);
        LocalTime expectedArrival = nlp.extractExpectedArrival(msg);
        int lateMinutes = nlp.computeLateMinutes(expectedArrival);

        EmployeeSummary emp = employeeClient.getSummaryById(req.employeeId(), callerEmail, callerRole);

        if (intent == Intent.LATE_ARRIVAL) {
            // Build manual attendance mark request
            Map<String, Object> body = Map.of(
                    "employeeId", req.employeeId(),
                    "attendanceDate", date.toString(),
                    "checkInTime", expectedArrival.toString(),
                    "status", "LATE",
                    "note", "AI: " + msg
            );
            try {
                var record = attendanceClient.markAiAttendance(body, callerEmail, callerRole);
                return new NlAttendanceResponse(
                        "LATE_ARRIVAL", date, expectedArrival, lateMinutes,
                        msg, "ATTENDANCE_MARKED",
                        "Your expected late arrival of " + lateMinutes + " min has been recorded.",
                        record != null ? record.id() : null);
            } catch (Exception e) {
                return new NlAttendanceResponse(
                        "LATE_ARRIVAL", date, expectedArrival, lateMinutes,
                        msg, "MANUAL_REVIEW_REQUIRED",
                        "Could not auto-mark attendance. Please notify your manager. (" + e.getMessage() + ")",
                        null);
            }
        }

        if (intent == Intent.ABSENT_TODAY) {
            Map<String, Object> body = Map.of(
                    "employeeId", req.employeeId(),
                    "attendanceDate", date.toString(),
                    "status", "ABSENT",
                    "note", "AI: " + msg
            );
            try {
                attendanceClient.markAiAttendance(body, callerEmail, callerRole);
                return new NlAttendanceResponse("ABSENT_TODAY", date, null, 0, msg,
                        "ATTENDANCE_MARKED", "Absence recorded for " + date, null);
            } catch (Exception e) {
                return new NlAttendanceResponse("ABSENT_TODAY", date, null, 0, msg,
                        "MANUAL_REVIEW_REQUIRED", "Could not auto-mark absence: " + e.getMessage(), null);
            }
        }

        return new NlAttendanceResponse(intent.name(), date, null, 0, msg,
                "MANUAL_REVIEW_REQUIRED",
                "Intent '" + intent.name() + "' understood but requires manual processing.", null);
    }
}
