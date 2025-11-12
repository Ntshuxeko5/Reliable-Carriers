package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.*;
import com.reliablecarriers.Reliable.Carriers.service.PaymentService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminReportController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;


    /**
     * Generate payment report
     */
    @GetMapping("/payments")
    public ResponseEntity<byte[]> generatePaymentReport(
            @RequestParam(defaultValue = "pdf") String format,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            List<Payment> payments;
            if (startDate != null && endDate != null) {
                payments = paymentService.getPaymentsByDateRange(startDate, endDate);
            } else {
                payments = paymentService.getAllPayments();
            }

            byte[] reportData;
            String filename;
            MediaType contentType;

            if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
                reportData = generatePaymentExcelReport(payments);
                filename = "payment-report.xlsx";
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            } else {
                reportData = generatePaymentPdfReport(payments, startDate, endDate);
                filename = "payment-report.pdf";
                contentType = MediaType.APPLICATION_PDF;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                .headers(headers)
                .body(reportData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate package report
     */
    @GetMapping("/packages")
    public ResponseEntity<byte[]> generatePackageReport(
            @RequestParam(defaultValue = "pdf") String format,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            List<Shipment> shipments = shipmentRepository.findAll();
            List<Booking> bookings = bookingRepository.findAll();

            byte[] reportData;
            String filename;
            MediaType contentType;

            if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
                reportData = generatePackageExcelReport(shipments, bookings);
                filename = "package-report.xlsx";
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            } else {
                reportData = generatePackagePdfReport(shipments, bookings, startDate, endDate);
                filename = "package-report.pdf";
                contentType = MediaType.APPLICATION_PDF;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                .headers(headers)
                .body(reportData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate user report
     */
    @GetMapping("/users")
    public ResponseEntity<byte[]> generateUserReport(
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            List<User> users = userRepository.findAll();

            byte[] reportData;
            String filename;
            MediaType contentType;

            if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
                reportData = generateUserExcelReport(users);
                filename = "user-report.xlsx";
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            } else {
                reportData = generateUserPdfReport(users);
                filename = "user-report.pdf";
                contentType = MediaType.APPLICATION_PDF;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                .headers(headers)
                .body(reportData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PDF Report Generators
    private byte[] generatePaymentPdfReport(List<Payment> payments, Date startDate, Date endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Payment Report")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String period = "All Time";
        if (startDate != null && endDate != null) {
            period = sdf.format(startDate) + " to " + sdf.format(endDate);
        }
        document.add(new Paragraph("Period: " + period)
            .setFontSize(12)
            .setMarginBottom(20));

        // Summary
        BigDecimal totalRevenue = payments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        document.add(new Paragraph("Total Revenue: R " + totalRevenue.toString())
            .setFontSize(14)
            .setBold()
            .setMarginBottom(20));

        // Table
        Table table = new Table(5);
        table.addHeaderCell("Transaction ID");
        table.addHeaderCell("Customer");
        table.addHeaderCell("Amount");
        table.addHeaderCell("Status");
        table.addHeaderCell("Date");

        for (Payment payment : payments) {
            table.addCell(payment.getTransactionId() != null ? payment.getTransactionId() : "N/A");
            String customer = payment.getUser() != null ? 
                (payment.getUser().getEmail() != null ? payment.getUser().getEmail() : "N/A") : "N/A";
            table.addCell(customer);
            table.addCell("R " + payment.getAmount().toString());
            table.addCell(payment.getStatus() != null ? payment.getStatus().toString() : "N/A");
            table.addCell(payment.getPaymentDate() != null ? sdf.format(payment.getPaymentDate()) : 
                (payment.getCreatedAt() != null ? sdf.format(payment.getCreatedAt()) : "N/A"));
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    private byte[] generatePackagePdfReport(List<Shipment> shipments, List<Booking> bookings, Date startDate, Date endDate) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Package Report")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20));

        document.add(new Paragraph("Total Shipments: " + shipments.size())
            .setFontSize(14)
            .setMarginBottom(10));
        document.add(new Paragraph("Total Bookings: " + bookings.size())
            .setFontSize(14)
            .setMarginBottom(20));

        // Shipments table
        Table table = new Table(4);
        table.addHeaderCell("Tracking Number");
        table.addHeaderCell("Status");
        table.addHeaderCell("Customer");
        table.addHeaderCell("Created Date");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Shipment shipment : shipments) {
            table.addCell(shipment.getTrackingNumber() != null ? shipment.getTrackingNumber() : "N/A");
            table.addCell(shipment.getStatus() != null ? shipment.getStatus().toString() : "N/A");
            table.addCell(shipment.getSender() != null && shipment.getSender().getEmail() != null ? 
                shipment.getSender().getEmail() : "N/A");
            table.addCell(shipment.getCreatedAt() != null ? sdf.format(shipment.getCreatedAt()) : "N/A");
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    private byte[] generateUserPdfReport(List<User> users) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("User Report")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20));

        document.add(new Paragraph("Total Users: " + users.size())
            .setFontSize(14)
            .setMarginBottom(20));

        Table table = new Table(4);
        table.addHeaderCell("Name");
        table.addHeaderCell("Email");
        table.addHeaderCell("Role");
        table.addHeaderCell("Created Date");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (User user : users) {
            String name = (user.getFirstName() != null ? user.getFirstName() : "") + 
                         " " + (user.getLastName() != null ? user.getLastName() : "");
            table.addCell(name.trim().isEmpty() ? "N/A" : name.trim());
            table.addCell(user.getEmail() != null ? user.getEmail() : "N/A");
            table.addCell(user.getRole() != null ? user.getRole().toString() : "N/A");
            table.addCell(user.getCreatedAt() != null ? sdf.format(user.getCreatedAt()) : "N/A");
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    // Excel Report Generators
    private byte[] generatePaymentExcelReport(List<Payment> payments) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Payments");

        // Header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Transaction ID", "Customer Email", "Amount", "Status", "Payment Method", "Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }

        // Data
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        for (Payment payment : payments) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(payment.getTransactionId() != null ? payment.getTransactionId() : "");
            row.createCell(1).setCellValue(payment.getUser() != null && payment.getUser().getEmail() != null ? 
                payment.getUser().getEmail() : "");
            row.createCell(2).setCellValue(payment.getAmount() != null ? payment.getAmount().doubleValue() : 0);
            row.createCell(3).setCellValue(payment.getStatus() != null ? payment.getStatus().toString() : "");
            row.createCell(4).setCellValue(payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : "");
            row.createCell(5).setCellValue(payment.getPaymentDate() != null ? sdf.format(payment.getPaymentDate()) : 
                (payment.getCreatedAt() != null ? sdf.format(payment.getCreatedAt()) : ""));
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    private byte[] generatePackageExcelReport(List<Shipment> shipments, List<Booking> bookings) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        // Shipments sheet
        Sheet shipmentsSheet = workbook.createSheet("Shipments");
        Row headerRow = shipmentsSheet.createRow(0);
        String[] headers = {"Tracking Number", "Status", "Customer Email", "Created Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        for (Shipment shipment : shipments) {
            Row row = shipmentsSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(shipment.getTrackingNumber() != null ? shipment.getTrackingNumber() : "");
            row.createCell(1).setCellValue(shipment.getStatus() != null ? shipment.getStatus().toString() : "");
            row.createCell(2).setCellValue(shipment.getSender() != null && shipment.getSender().getEmail() != null ? 
                shipment.getSender().getEmail() : "");
            row.createCell(3).setCellValue(shipment.getCreatedAt() != null ? sdf.format(shipment.getCreatedAt()) : "");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            shipmentsSheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    private byte[] generateUserExcelReport(List<User> users) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"First Name", "Last Name", "Email", "Role", "Phone", "Created Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getFirstName() != null ? user.getFirstName() : "");
            row.createCell(1).setCellValue(user.getLastName() != null ? user.getLastName() : "");
            row.createCell(2).setCellValue(user.getEmail() != null ? user.getEmail() : "");
            row.createCell(3).setCellValue(user.getRole() != null ? user.getRole().toString() : "");
            row.createCell(4).setCellValue(user.getPhone() != null ? user.getPhone() : "");
            row.createCell(5).setCellValue(user.getCreatedAt() != null ? sdf.format(user.getCreatedAt()) : "");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}

