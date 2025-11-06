package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.DocumentVerificationStatus;
import com.reliablecarriers.Reliable.Carriers.model.DriverDocument;
import com.reliablecarriers.Reliable.Carriers.model.DriverDocumentType;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.BusinessDocumentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.DriverDocumentRepository;
import com.reliablecarriers.Reliable.Carriers.service.impl.DocumentExpiryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentExpiryServiceTest {

    @Mock
    private DriverDocumentRepository driverDocumentRepository;

    @Mock
    private BusinessDocumentRepository businessDocumentRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DocumentExpiryServiceImpl documentExpiryService;

    private User testDriver;
    private DriverDocument documentExpiring30Days;
    private DriverDocument documentExpiring14Days;
    private DriverDocument documentExpiring7Days;
    private DriverDocument expiredDocument;

    @BeforeEach
    void setUp() {
        // Create test driver
        testDriver = new User();
        testDriver.setId(1L);
        testDriver.setEmail("driver@test.com");
        testDriver.setFirstName("Test");
        testDriver.setLastName("Driver");

        // Document expiring in 30 days
        documentExpiring30Days = new DriverDocument();
        documentExpiring30Days.setId(1L);
        documentExpiring30Days.setDriver(testDriver);
        documentExpiring30Days.setDocumentType(DriverDocumentType.DRIVER_LICENSE);
        documentExpiring30Days.setVerificationStatus(DocumentVerificationStatus.VERIFIED);
        documentExpiring30Days.setExpiresAt(Date.from(
            LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()
        ));

        // Document expiring in 14 days
        documentExpiring14Days = new DriverDocument();
        documentExpiring14Days.setId(2L);
        documentExpiring14Days.setDriver(testDriver);
        documentExpiring14Days.setDocumentType(DriverDocumentType.ID_DOCUMENT);
        documentExpiring14Days.setVerificationStatus(DocumentVerificationStatus.VERIFIED);
        documentExpiring14Days.setExpiresAt(Date.from(
            LocalDate.now().plusDays(14).atStartOfDay(ZoneId.systemDefault()).toInstant()
        ));

        // Document expiring in 7 days
        documentExpiring7Days = new DriverDocument();
        documentExpiring7Days.setId(3L);
        documentExpiring7Days.setDriver(testDriver);
        documentExpiring7Days.setDocumentType(DriverDocumentType.PROOF_OF_ADDRESS);
        documentExpiring7Days.setVerificationStatus(DocumentVerificationStatus.VERIFIED);
        documentExpiring7Days.setExpiresAt(Date.from(
            LocalDate.now().plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant()
        ));

        // Expired document
        expiredDocument = new DriverDocument();
        expiredDocument.setId(4L);
        expiredDocument.setDriver(testDriver);
        expiredDocument.setDocumentType(DriverDocumentType.DRIVER_LICENSE);
        expiredDocument.setVerificationStatus(DocumentVerificationStatus.VERIFIED);
        expiredDocument.setExpiresAt(Date.from(
            LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant()
        ));
    }

    @Test
    void testCheckExpiringDocuments_SendsAlerts() {
        // Arrange
        List<DriverDocument> documents = new ArrayList<>();
        documents.add(documentExpiring30Days);
        documents.add(documentExpiring14Days);
        documents.add(documentExpiring7Days);

        when(driverDocumentRepository.findAll()).thenReturn(documents);
        when(businessDocumentRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        documentExpiryService.checkExpiringDocuments();

        // Assert
        verify(emailService, times(3)).sendDocumentExpiryWarning(
            anyString(), anyString(), anyString(), anyString()
        );
    }

    @Test
    void testMarkExpiredDocuments_MarksAsExpired() {
        // Arrange
        List<DriverDocument> documents = new ArrayList<>();
        documents.add(expiredDocument);

        when(driverDocumentRepository.findAll()).thenReturn(documents);
        when(businessDocumentRepository.findAll()).thenReturn(new ArrayList<>());
        when(driverDocumentRepository.save(any(DriverDocument.class))).thenReturn(expiredDocument);

        // Act
        documentExpiryService.markExpiredDocuments();

        // Assert
        verify(driverDocumentRepository, times(1)).save(any(DriverDocument.class));
        verify(driverDocumentRepository).save(argThat(doc -> 
            doc.getVerificationStatus() == DocumentVerificationStatus.EXPIRED
        ));
    }

    @Test
    void testCheckExpiringDocuments_IgnoresNonVerifiedDocuments() {
        // Arrange
        documentExpiring30Days.setVerificationStatus(DocumentVerificationStatus.PENDING);
        List<DriverDocument> documents = new ArrayList<>();
        documents.add(documentExpiring30Days);

        when(driverDocumentRepository.findAll()).thenReturn(documents);
        when(businessDocumentRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        documentExpiryService.checkExpiringDocuments();

        // Assert
        verify(emailService, never()).sendDocumentExpiryWarning(
            anyString(), anyString(), anyString(), anyString()
        );
    }
}

