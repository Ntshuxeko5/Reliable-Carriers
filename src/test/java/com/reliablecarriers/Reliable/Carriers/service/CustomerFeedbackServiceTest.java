package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.repository.CustomerFeedbackRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.service.impl.CustomerFeedbackServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerFeedbackServiceTest {

    @Mock
    private CustomerFeedbackRepository feedbackRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CustomerFeedbackServiceImpl feedbackService;

    private Shipment testShipment;

    @BeforeEach
    void setUp() {
        testShipment = new Shipment();
        testShipment.setId(1L);
        testShipment.setTrackingNumber("RC12345678");
        testShipment.setRecipientEmail("customer@test.com");
        testShipment.setRecipientName("Test Customer");
    }

    @Test
    void testGenerateFeedbackReport_GeneratesPDF() {
        // Arrange
        String startDate = "2024-01-01";
        String endDate = "2024-01-31";

        // Act
        byte[] report = feedbackService.generateFeedbackReport(startDate, endDate);

        // Assert
        assertNotNull(report);
        assertTrue(report.length > 0);
        // PDF files start with %PDF
        String pdfHeader = new String(report, 0, Math.min(4, report.length));
        assertEquals("%PDF", pdfHeader);
    }

    @Test
    void testSendFeedbackRequest_SendsEmail() {
        // Arrange
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        doNothing().when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());

        // Act
        feedbackService.sendFeedbackRequest(1L);

        // Assert
        verify(emailService, times(1)).sendSimpleEmail(
            eq("customer@test.com"),
            anyString(),
            anyString()
        );
    }

    @Test
    void testSendFeedbackRequest_ThrowsExceptionWhenShipmentNotFound() {
        // Arrange
        when(shipmentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            feedbackService.sendFeedbackRequest(1L);
        });
    }

    @Test
    void testExportFeedbackData_CSVFormat() {
        // Arrange
        String startDate = "2024-01-01";
        String endDate = "2024-01-31";

        // Act
        byte[] csvData = feedbackService.exportFeedbackData(startDate, endDate, "CSV");

        // Assert
        assertNotNull(csvData);
        assertTrue(csvData.length > 0);
        String csvContent = new String(csvData);
        assertTrue(csvContent.contains("Date,Customer Email"));
    }

    @Test
    void testExportFeedbackData_ExcelFormat() {
        // Arrange
        String startDate = "2024-01-01";
        String endDate = "2024-01-31";

        // Act
        byte[] excelData = feedbackService.exportFeedbackData(startDate, endDate, "EXCEL");

        // Assert
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
        // Excel files (XLSX) start with PK (ZIP signature)
        assertTrue(excelData[0] == 0x50 && excelData[1] == 0x4B);
    }

    @Test
    void testExportFeedbackData_UnsupportedFormat() {
        // Arrange
        String startDate = "2024-01-01";
        String endDate = "2024-01-31";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            feedbackService.exportFeedbackData(startDate, endDate, "UNSUPPORTED");
        });
    }
}

