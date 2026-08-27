package com.attendai.ai.service;

import com.attendai.ai.client.AttendanceClient;
import com.attendai.ai.client.EmployeeClient;
import com.attendai.ai.client.dto.EmployeeSummary;
import com.attendai.ai.client.dto.MonthlySummary;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportGeneratorService {

    private final AttendanceClient attendanceClient;
    private final EmployeeClient employeeClient;

    public ReportGeneratorService(AttendanceClient attendanceClient, EmployeeClient employeeClient) {
        this.attendanceClient = attendanceClient;
        this.employeeClient = employeeClient;
    }

    /**
     * Generate a PDF attendance report for a list of employees for a given month.
     */
    public byte[] generateAttendanceReport(List<Long> employeeIds, int year, int month,
                                            String callerEmail, String callerRole) {
        try {
            Document doc = new Document(PageSize.A4);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            Font cellFont  = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);

            Paragraph title = new Paragraph("AttendAI — Attendance Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph(LocalDate.of(year, month, 1).getMonth() + " " + year + "  |  Generated: " + LocalDate.now(),
                    new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY));
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(20);
            doc.add(sub);

            // Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setWidths(new float[]{3f, 2f, 2f, 2f, 2f, 2f, 2f});

            String[] headers = {"Employee", "Present", "Absent", "Late", "Attendance %", "Total Hrs", "Rating"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new BaseColor(37, 99, 235));
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (Long id : employeeIds) {
                try {
                    MonthlySummary ms = attendanceClient.getMonthlySummary(id, year, month, callerEmail, callerRole);
                    String name;
                    try { name = employeeClient.getSummaryById(id, callerEmail, callerRole).fullName(); }
                    catch (Exception e) { name = "Employee #" + id; }

                    double pct = ms.attendancePercentage();
                    String rating = pct >= 95 ? "EXCELLENT" : pct >= 85 ? "GOOD" : pct >= 70 ? "AVERAGE" : "POOR";
                    BaseColor rowColor = pct < 70 ? new BaseColor(254, 226, 226) : BaseColor.WHITE;

                    addRow(table, cellFont, rowColor, name,
                            String.valueOf(ms.presentDays()),
                            String.valueOf(ms.absentDays()),
                            String.valueOf(ms.lateDays()),
                            String.format("%.1f%%", pct),
                            String.format("%.1f", ms.totalWorkingHours()),
                            rating);
                } catch (Exception ignored) {}
            }
            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report: " + e.getMessage(), e);
        }
    }

    private void addRow(PdfPTable table, Font f, BaseColor bg, String... values) {
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v, f));
            cell.setBackgroundColor(bg);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }
}
