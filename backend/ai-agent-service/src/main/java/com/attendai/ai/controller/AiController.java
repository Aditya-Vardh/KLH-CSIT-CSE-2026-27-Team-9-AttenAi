package com.attendai.ai.controller;

import com.attendai.ai.dto.*;
import com.attendai.ai.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Agent", description = "NL attendance, leave recommendation, analytics, chatbot, PDF reports")
public class AiController {

    private final NlAttendanceService nlAttendance;
    private final LeaveRecommendationService leaveRec;
    private final AnalyticsAiService analytics;
    private final HrChatbotService chatbot;
    private final ReportGeneratorService reportGen;

    public AiController(NlAttendanceService nlAttendance,
                         LeaveRecommendationService leaveRec,
                         AnalyticsAiService analytics,
                         HrChatbotService chatbot,
                         ReportGeneratorService reportGen) {
        this.nlAttendance = nlAttendance;
        this.leaveRec = leaveRec;
        this.analytics = analytics;
        this.chatbot = chatbot;
        this.reportGen = reportGen;
    }

    @PostMapping("/attendance/nl")
    @Operation(summary = "Natural language attendance — e.g. 'I'm coming 30 min late today'")
    public NlAttendanceResponse processNlAttendance(
            @Valid @RequestBody NlAttendanceRequest req,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role) {
        return nlAttendance.process(req, email, role);
    }

    @PostMapping("/leave/recommend")
    @Operation(summary = "AI leave recommendation — APPROVE or REJECT with confidence score")
    public LeaveRecommendationResponse recommendLeave(
            @Valid @RequestBody LeaveRecommendationRequest req,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role) {
        return leaveRec.recommend(req, email, role);
    }

    @PostMapping("/analytics/query")
    @Operation(summary = "Ask a natural-language analytics question — e.g. 'Who has poor attendance?'")
    public AnalyticsQueryResponse queryAnalytics(
            @Valid @RequestBody AnalyticsQueryRequest req,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role) {
        return analytics.query(req, email, role);
    }

    @PostMapping("/chat")
    @Operation(summary = "HR assistant chatbot — ask about leave, attendance, profile, policies")
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest req,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role) {
        return chatbot.chat(req, email, role);
    }

    @GetMapping("/report/attendance")
    @Operation(summary = "Generate PDF attendance report for a list of employees")
    public ResponseEntity<byte[]> attendanceReport(
            @RequestParam List<Long> employeeIds,
            @RequestParam int year,
            @RequestParam int month,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role) {
        byte[] pdf = reportGen.generateAttendanceReport(employeeIds, year, month, email, role);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=attendance-report-" + year + "-" + String.format("%02d", month) + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
