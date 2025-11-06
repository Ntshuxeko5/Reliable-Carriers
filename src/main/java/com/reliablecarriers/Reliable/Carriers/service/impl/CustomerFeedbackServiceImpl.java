package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.CustomerFeedback;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.repository.CustomerFeedbackRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.service.CustomerFeedbackService;
import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerFeedbackServiceImpl implements CustomerFeedbackService {

    @Autowired
    private CustomerFeedbackRepository feedbackRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private EmailService emailService;

    @Override
    public CustomerFeedback createFeedback(Long shipmentId, String customerEmail, String customerPhone,
                                          Integer overallRating, Integer deliverySpeedRating,
                                          Integer driverCourtesyRating, Integer packageConditionRating,
                                          Integer communicationRating, String comments, String feedbackType) {
        // Find shipment
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));

        // Create feedback
        CustomerFeedback feedback = new CustomerFeedback();
        feedback.setShipment(shipment);
        feedback.setCustomerEmail(customerEmail);
        feedback.setCustomerPhone(customerPhone);
        feedback.setOverallRating(overallRating);
        feedback.setDeliverySpeedRating(deliverySpeedRating);
        feedback.setDriverCourtesyRating(driverCourtesyRating);
        feedback.setPackageConditionRating(packageConditionRating);
        feedback.setCommunicationRating(communicationRating);
        feedback.setComments(comments);
        feedback.setFeedbackType(feedbackType);
        
        // Set rating (same as overallRating)
        feedback.setRating(overallRating);
        
        // Analyze sentiment
        feedback.setSentiment(analyzeSentiment(comments));
        
        // Set would recommend (4 or 5 stars = yes)
        feedback.setWouldRecommend(overallRating >= 4);
        
        // Extract driver ID if shipment has driver
        if (shipment.getAssignedDriver() != null) {
            feedback.setDriverId(shipment.getAssignedDriver().getId());
        }
        
        // Extract customer ID if sender is a user
        if (shipment.getSender() != null) {
            feedback.setCustomerId(shipment.getSender().getId());
        }

        return feedbackRepository.save(feedback);
    }

    @Override
    public CustomerFeedback getFeedbackById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new RuntimeException("Feedback not found: " + feedbackId));
    }

    @Override
    public CustomerFeedback getFeedbackByShipment(Long shipmentId) {
        List<CustomerFeedback> feedbacks = feedbackRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId);
        return feedbacks.isEmpty() ? null : feedbacks.get(0);
    }

    @Override
    public List<CustomerFeedback> getFeedbackByCustomer(String email) {
        return feedbackRepository.findAll().stream()
            .filter(f -> email != null && email.equalsIgnoreCase(f.getCustomerEmail()))
            .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()))
            .collect(Collectors.toList());
    }

    @Override
    public List<CustomerFeedback> getFeedbackByRatingRange(Integer minRating, Integer maxRating) {
        return feedbackRepository.findByOverallRatingBetweenOrderByCreatedAtDesc(minRating, maxRating);
    }

    @Override
    public List<CustomerFeedback> getFeedbackBySentiment(String sentiment) {
        return feedbackRepository.findAll().stream()
            .filter(f -> sentiment != null && sentiment.equalsIgnoreCase(f.getSentiment()))
            .collect(Collectors.toList());
    }

    @Override
    public List<CustomerFeedback> getFeedbackByDateRange(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(end);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            final Date endDatePlusOne = cal.getTime();
            
            return feedbackRepository.findAll().stream()
                .filter(f -> f.getCreatedAt().after(start) && f.getCreatedAt().before(endDatePlusOne))
                .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format", e);
        }
    }

    @Override
    public List<CustomerFeedback> getFeedbackByType(String feedbackType) {
        return feedbackRepository.findByFeedbackTypeOrderByCreatedAtDesc(feedbackType);
    }

    @Override
    public Map<String, Object> getFeedbackStatistics() {
        long totalFeedbacks = feedbackRepository.count();
        Double avgRating = feedbackRepository.getOverallAverageRating();
        long unresolvedCount = feedbackRepository.findByIsResolvedFalseOrderByCreatedAtDesc().size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFeedbacks", totalFeedbacks);
        stats.put("averageRating", avgRating != null ? avgRating : 0.0);
        stats.put("unresolvedCount", unresolvedCount);
        stats.put("resolvedCount", totalFeedbacks - unresolvedCount);
        
        return stats;
    }

    @Override
    public Map<String, Double> getAverageRatingsByCategory() {
        List<CustomerFeedback> allFeedbacks = feedbackRepository.findAll();
        
        Map<String, Double> averages = new HashMap<>();
        
        double avgOverall = allFeedbacks.stream()
            .mapToInt(CustomerFeedback::getOverallRating)
            .average().orElse(0.0);
        averages.put("overall", avgOverall);
        
        double avgSpeed = allFeedbacks.stream()
            .mapToInt(CustomerFeedback::getDeliverySpeedRating)
            .average().orElse(0.0);
        averages.put("deliverySpeed", avgSpeed);
        
        double avgCourtesy = allFeedbacks.stream()
            .mapToInt(CustomerFeedback::getDriverCourtesyRating)
            .average().orElse(0.0);
        averages.put("driverCourtesy", avgCourtesy);
        
        double avgCondition = allFeedbacks.stream()
            .mapToInt(CustomerFeedback::getPackageConditionRating)
            .average().orElse(0.0);
        averages.put("packageCondition", avgCondition);
        
        double avgCommunication = allFeedbacks.stream()
            .mapToInt(CustomerFeedback::getCommunicationRating)
            .average().orElse(0.0);
        averages.put("communication", avgCommunication);
        
        return averages;
    }

    @Override
    public Double getCustomerSatisfactionScore() {
        return feedbackRepository.getOverallAverageRating();
    }

    @Override
    public Integer getNetPromoterScore() {
        List<CustomerFeedback> allFeedbacks = feedbackRepository.findAll();
        if (allFeedbacks.isEmpty()) return 0;
        
        long promoters = allFeedbacks.stream()
            .filter(f -> f.getOverallRating() != null && f.getOverallRating() >= 4)
            .count();
        
        long detractors = allFeedbacks.stream()
            .filter(f -> f.getOverallRating() != null && f.getOverallRating() <= 2)
            .count();
        
        double total = allFeedbacks.size();
        double nps = ((promoters - detractors) / total) * 100;
        
        return (int) Math.round(nps);
    }

    @Override
    public String analyzeSentiment(String comments) {
        if (comments == null || comments.trim().isEmpty()) {
            return "NEUTRAL";
        }
        
        String lowerComments = comments.toLowerCase();
        
        // Positive keywords
        List<String> positiveKeywords = Arrays.asList("good", "great", "excellent", "amazing", "wonderful", 
            "perfect", "fantastic", "love", "happy", "satisfied", "pleased", "thank", "fast", "quick");
        
        // Negative keywords
        List<String> negativeKeywords = Arrays.asList("bad", "terrible", "awful", "horrible", "slow", 
            "late", "damaged", "broken", "disappointed", "worst", "hate", "poor", "unsatisfied");
        
        int positiveCount = (int) positiveKeywords.stream()
            .filter(lowerComments::contains)
            .count();
        
        int negativeCount = (int) negativeKeywords.stream()
            .filter(lowerComments::contains)
            .count();
        
        if (positiveCount > negativeCount) {
            return "POSITIVE";
        } else if (negativeCount > positiveCount) {
            return "NEGATIVE";
        } else {
            return "NEUTRAL";
        }
    }

    @Override
    public CustomerFeedback respondToFeedback(Long feedbackId, String adminResponse, String respondedBy) {
        CustomerFeedback feedback = getFeedbackById(feedbackId);
        feedback.setAdminResponse(adminResponse);
        feedback.setRespondedBy(respondedBy);
        feedback.setResponseDate(new Date());
        feedback.setResponseStatus("RESPONDED");
        return feedbackRepository.save(feedback);
    }

    @Override
    public CustomerFeedback markFeedbackResolved(Long feedbackId) {
        CustomerFeedback feedback = getFeedbackById(feedbackId);
        feedback.setResolved(true);
        feedback.setResponseStatus("RESOLVED");
        return feedbackRepository.save(feedback);
    }

    @Override
    public Map<String, Object> getFeedbackTrends(String startDate, String endDate) {
        List<CustomerFeedback> feedbacks = getFeedbackByDateRange(startDate, endDate);
        
        Map<String, Object> trends = new HashMap<>();
        trends.put("totalFeedbacks", feedbacks.size());
        trends.put("averageRating", feedbacks.stream()
            .mapToInt(CustomerFeedback::getOverallRating)
            .average().orElse(0.0));
        
        // Group by date
        Map<String, Long> byDate = feedbacks.stream()
            .collect(Collectors.groupingBy(
                f -> new SimpleDateFormat("yyyy-MM-dd").format(f.getCreatedAt()),
                Collectors.counting()
            ));
        trends.put("feedbacksByDate", byDate);
        
        return trends;
    }

    @Override
    public List<Map<String, Object>> getTopFeedbackIssues() {
        List<CustomerFeedback> negativeFeedbacks = getFeedbackBySentiment("NEGATIVE");
        
        return negativeFeedbacks.stream()
            .limit(10)
            .map(f -> {
                Map<String, Object> issue = new HashMap<>();
                issue.put("feedbackId", f.getId());
                issue.put("comments", f.getComments());
                issue.put("rating", f.getOverallRating());
                issue.put("createdAt", f.getCreatedAt());
                return issue;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDriverFeedbackPerformance(Long driverId) {
        List<CustomerFeedback> driverFeedbacks = feedbackRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
        
        Map<String, Object> performance = new HashMap<>();
        performance.put("totalFeedbacks", driverFeedbacks.size());
        
        if (!driverFeedbacks.isEmpty()) {
            Double avgRating = feedbackRepository.getAverageRatingByDriverId(driverId);
            performance.put("averageRating", avgRating != null ? avgRating : 0.0);
            
            long positiveCount = driverFeedbacks.stream()
                .filter(f -> "POSITIVE".equals(f.getSentiment()))
                .count();
            performance.put("positiveFeedbackCount", positiveCount);
        } else {
            performance.put("averageRating", 0.0);
            performance.put("positiveFeedbackCount", 0L);
        }
        
        return performance;
    }

    @Override
    public Map<String, Object> getServiceTypeFeedbackPerformance(String serviceType) {
        // This would require a join with Shipment table - simplified for now
        List<CustomerFeedback> allFeedbacks = feedbackRepository.findAll();
        
        Map<String, Object> performance = new HashMap<>();
        performance.put("totalFeedbacks", allFeedbacks.size());
        performance.put("averageRating", allFeedbacks.stream()
            .mapToInt(CustomerFeedback::getOverallRating)
            .average().orElse(0.0));
        
        return performance;
    }

    @Override
    public byte[] generateFeedbackReport(String startDate, String endDate) {
        try {
            List<CustomerFeedback> feedbacks = getFeedbackByDateRange(startDate, endDate);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Header
            document.add(new Paragraph("Customer Feedback Report")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
            
            document.add(new Paragraph("Period: " + startDate + " to " + endDate)
                .setFontSize(12)
                .setMarginBottom(20));
            
            // Summary Statistics
            double avgRating = feedbacks.stream()
                .filter(f -> f.getOverallRating() != null)
                .mapToInt(CustomerFeedback::getOverallRating)
                .average()
                .orElse(0.0);
            
            long totalFeedbacks = feedbacks.size();
            long resolvedCount = feedbacks.stream().filter(f -> f.getResolved() != null && f.getResolved()).count();
            
            document.add(new Paragraph("Summary Statistics")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10));
            document.add(new Paragraph("Total Feedbacks: " + totalFeedbacks));
            document.add(new Paragraph("Average Rating: " + String.format("%.2f", avgRating) + " / 5"));
            document.add(new Paragraph("Resolved: " + resolvedCount));
            document.add(new Paragraph("Unresolved: " + (totalFeedbacks - resolvedCount))
                .setMarginBottom(20));
            
            // Feedback Table
            if (!feedbacks.isEmpty()) {
                document.add(new Paragraph("Feedback Details")
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(10));
                
                Table table = new Table(5);
                table.addHeaderCell("Date");
                table.addHeaderCell("Customer Email");
                table.addHeaderCell("Overall Rating");
                table.addHeaderCell("Comments");
                table.addHeaderCell("Status");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                for (CustomerFeedback feedback : feedbacks) {
                    table.addCell(feedback.getCreatedAt() != null ? sdf.format(feedback.getCreatedAt()) : "N/A");
                    table.addCell(feedback.getCustomerEmail() != null ? feedback.getCustomerEmail() : "N/A");
                    table.addCell(feedback.getOverallRating() != null ? feedback.getOverallRating().toString() : "N/A");
                    table.addCell(feedback.getComments() != null && feedback.getComments().length() > 50 
                        ? feedback.getComments().substring(0, 50) + "..." 
                        : (feedback.getComments() != null ? feedback.getComments() : "N/A"));
                    table.addCell(feedback.getResolved() != null && feedback.getResolved() ? "Resolved" : "Pending");
                }
                
                document.add(table);
            }
            
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    @Override
    public void sendFeedbackRequest(Long shipmentId) {
        try {
            Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
            
            String customerEmail = shipment.getRecipientEmail();
            if (customerEmail == null || customerEmail.isEmpty()) {
                throw new RuntimeException("Recipient email not found for shipment: " + shipmentId);
            }
            
            String customerName = shipment.getRecipientName() != null ? shipment.getRecipientName() : "Valued Customer";
            String trackingNumber = shipment.getTrackingNumber() != null ? shipment.getTrackingNumber() : shipmentId.toString();
            
            String subject = "We'd Love Your Feedback - Reliable Carriers";
            String message = String.format(
                "Dear %s,\n\n" +
                "Thank you for choosing Reliable Carriers for your shipping needs!\n\n" +
                "We hope your shipment (Tracking #: %s) was delivered to your satisfaction.\n\n" +
                "Your feedback is important to us and helps us improve our services. " +
                "Please take a moment to share your experience by visiting our feedback page.\n\n" +
                "Thank you for your time!\n\n" +
                "Best regards,\n" +
                "Reliable Carriers Team",
                customerName, trackingNumber
            );
            
            emailService.sendSimpleEmail(customerEmail, subject, message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send feedback request email", e);
        }
    }

    @Override
    public Double getFeedbackResponseRate() {
        long totalShipments = shipmentRepository.count();
        long totalFeedbacks = feedbackRepository.count();
        
        if (totalShipments == 0) return 0.0;
        
        return (totalFeedbacks / (double) totalShipments) * 100;
    }

    @Override
    public List<CustomerFeedback> getFeedbackByDriver(Long driverId) {
        return feedbackRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
    }

    @Override
    public Map<String, Object> getFeedbackSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalFeedbacks", feedbackRepository.count());
        summary.put("averageRating", feedbackRepository.getOverallAverageRating());
        summary.put("unresolvedCount", feedbackRepository.findByIsResolvedFalseOrderByCreatedAtDesc().size());
        summary.put("responseRate", getFeedbackResponseRate());
        
        return summary;
    }

    @Override
    public byte[] exportFeedbackData(String startDate, String endDate, String format) {
        try {
            List<CustomerFeedback> feedbacks = getFeedbackByDateRange(startDate, endDate);
            
            if ("CSV".equalsIgnoreCase(format)) {
                return exportToCSV(feedbacks);
            } else if ("EXCEL".equalsIgnoreCase(format) || "XLSX".equalsIgnoreCase(format)) {
                return exportToExcel(feedbacks);
            } else {
                throw new IllegalArgumentException("Unsupported export format: " + format + ". Supported formats: CSV, EXCEL");
            }
        } catch (IllegalArgumentException e) {
            // Re-throw IllegalArgumentException directly without wrapping
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to export feedback data", e);
        }
    }
    
    private byte[] exportToCSV(List<CustomerFeedback> feedbacks) {
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Customer Email,Customer Phone,Overall Rating,Delivery Speed,Driver Courtesy,Package Condition,Communication,Comments,Feedback Type,Resolved\n");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (CustomerFeedback feedback : feedbacks) {
            csv.append("\"");
            csv.append(feedback.getCreatedAt() != null ? sdf.format(feedback.getCreatedAt()) : "");
            csv.append("\",\"");
            csv.append(feedback.getCustomerEmail() != null ? feedback.getCustomerEmail().replace("\"", "\"\"") : "");
            csv.append("\",\"");
            csv.append(feedback.getCustomerPhone() != null ? feedback.getCustomerPhone().replace("\"", "\"\"") : "");
            csv.append("\",");
            csv.append(feedback.getOverallRating() != null ? feedback.getOverallRating() : "");
            csv.append(",");
            csv.append(feedback.getDeliverySpeedRating() != null ? feedback.getDeliverySpeedRating() : "");
            csv.append(",");
            csv.append(feedback.getDriverCourtesyRating() != null ? feedback.getDriverCourtesyRating() : "");
            csv.append(",");
            csv.append(feedback.getPackageConditionRating() != null ? feedback.getPackageConditionRating() : "");
            csv.append(",");
            csv.append(feedback.getCommunicationRating() != null ? feedback.getCommunicationRating() : "");
            csv.append(",\"");
            csv.append(feedback.getComments() != null ? feedback.getComments().replace("\"", "\"\"") : "");
            csv.append("\",\"");
            csv.append(feedback.getFeedbackType() != null ? feedback.getFeedbackType() : "");
            csv.append("\",");
            csv.append(feedback.getResolved() != null && feedback.getResolved() ? "Yes" : "No");
            csv.append("\n");
        }
        
        return csv.toString().getBytes();
    }
    
    private byte[] exportToExcel(List<CustomerFeedback> feedbacks) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Customer Feedback");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Customer Email", "Customer Phone", "Overall Rating", 
                           "Delivery Speed", "Driver Courtesy", "Package Condition", 
                           "Communication", "Comments", "Feedback Type", "Resolved"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Create data rows
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        for (CustomerFeedback feedback : feedbacks) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(feedback.getCreatedAt() != null ? sdf.format(feedback.getCreatedAt()) : "");
            row.createCell(1).setCellValue(feedback.getCustomerEmail() != null ? feedback.getCustomerEmail() : "");
            row.createCell(2).setCellValue(feedback.getCustomerPhone() != null ? feedback.getCustomerPhone() : "");
            row.createCell(3).setCellValue(feedback.getOverallRating() != null ? feedback.getOverallRating() : 0);
            row.createCell(4).setCellValue(feedback.getDeliverySpeedRating() != null ? feedback.getDeliverySpeedRating() : 0);
            row.createCell(5).setCellValue(feedback.getDriverCourtesyRating() != null ? feedback.getDriverCourtesyRating() : 0);
            row.createCell(6).setCellValue(feedback.getPackageConditionRating() != null ? feedback.getPackageConditionRating() : 0);
            row.createCell(7).setCellValue(feedback.getCommunicationRating() != null ? feedback.getCommunicationRating() : 0);
            row.createCell(8).setCellValue(feedback.getComments() != null ? feedback.getComments() : "");
            row.createCell(9).setCellValue(feedback.getFeedbackType() != null ? feedback.getFeedbackType() : "");
            row.createCell(10).setCellValue(feedback.getResolved() != null && feedback.getResolved() ? "Yes" : "No");
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

    @Override
    public List<String> getFeedbackImprovementSuggestions() {
        List<String> suggestions = new ArrayList<>();
        
        Double avgRating = feedbackRepository.getOverallAverageRating();
        if (avgRating != null && avgRating < 3.5) {
            suggestions.add("Overall customer satisfaction is below target. Consider improving service quality.");
        }
        
        long unresolvedCount = feedbackRepository.findByIsResolvedFalseOrderByCreatedAtDesc().size();
        if (unresolvedCount > 0) {
            suggestions.add("You have " + unresolvedCount + " unresolved feedback items. Please address them.");
        }
        
        return suggestions;
    }

    @Override
    public List<CustomerFeedback> getCustomerFeedbackHistory(String email) {
        return getFeedbackByCustomer(email);
    }

    @Override
    public CustomerFeedback updateFeedback(Long feedbackId, CustomerFeedback feedback) {
        CustomerFeedback existing = getFeedbackById(feedbackId);
        
        if (feedback.getComments() != null) {
            existing.setComments(feedback.getComments());
            existing.setSentiment(analyzeSentiment(feedback.getComments()));
        }
        if (feedback.getOverallRating() != null) {
            existing.setOverallRating(feedback.getOverallRating());
            existing.setRating(feedback.getOverallRating());
        }
        
        return feedbackRepository.save(existing);
    }

    @Override
    public void deleteFeedback(Long feedbackId) {
        feedbackRepository.deleteById(feedbackId);
    }

    @Override
    public List<CustomerFeedback> getFeedbackByStatus(String status) {
        if ("RESOLVED".equals(status)) {
            return feedbackRepository.findAll().stream()
                .filter(f -> Boolean.TRUE.equals(f.getResolved()))
                .collect(Collectors.toList());
        } else if ("PENDING".equals(status)) {
            return feedbackRepository.findAll().stream()
                .filter(f -> "PENDING".equals(f.getResponseStatus()))
                .collect(Collectors.toList());
        } else if ("RESPONDED".equals(status)) {
            return feedbackRepository.findAll().stream()
                .filter(f -> "RESPONDED".equals(f.getResponseStatus()))
                .collect(Collectors.toList());
        }
        
        return feedbackRepository.findAll();
    }

    @Override
    public Map<String, Object> getFeedbackAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Overall statistics
        analytics.putAll(getFeedbackStatistics());
        
        // Average ratings by category
        analytics.put("averageRatingsByCategory", getAverageRatingsByCategory());
        
        // Net Promoter Score
        analytics.put("netPromoterScore", getNetPromoterScore());
        
        // Feedback distribution
        List<Object[]> distribution = feedbackRepository.getFeedbackCountByRating();
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (Object[] item : distribution) {
            ratingDistribution.put((Integer) item[0], (Long) item[1]);
        }
        analytics.put("ratingDistribution", ratingDistribution);
        
        return analytics;
    }
}

