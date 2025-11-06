package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Business document model for storing business verification documents
 * All documents must be certified copies
 */
@Entity
@Table(name = "business_documents")
public class BusinessDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private User business; // Business user
    
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private BusinessDocumentType documentType;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Column(name = "is_certified", nullable = false)
    private Boolean isCertified = false; // Must be certified copy
    
    @Column(name = "certified_by", length = 255)
    private String certifiedBy; // Name of person who certified
    
    @Column(name = "certification_date")
    private Date certificationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private DocumentVerificationStatus verificationStatus = DocumentVerificationStatus.PENDING;
    
    @Column(name = "verified_by")
    private Long verifiedBy; // Admin user ID
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "verified_at")
    private Date verifiedAt;
    
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expires_at")
    private Date expiresAt; // For licenses, certificates with expiry
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getBusiness() { return business; }
    public void setBusiness(User business) { this.business = business; }
    
    public BusinessDocumentType getDocumentType() { return documentType; }
    public void setDocumentType(BusinessDocumentType documentType) { this.documentType = documentType; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    
    public Boolean getIsCertified() { return isCertified; }
    public void setIsCertified(Boolean isCertified) { this.isCertified = isCertified; }
    
    public String getCertifiedBy() { return certifiedBy; }
    public void setCertifiedBy(String certifiedBy) { this.certifiedBy = certifiedBy; }
    
    public Date getCertificationDate() { return certificationDate; }
    public void setCertificationDate(Date certificationDate) { this.certificationDate = certificationDate; }
    
    public DocumentVerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(DocumentVerificationStatus verificationStatus) { 
        this.verificationStatus = verificationStatus; 
    }
    
    public Long getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(Long verifiedBy) { this.verifiedBy = verifiedBy; }
    
    public Date getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Date verifiedAt) { this.verifiedAt = verifiedAt; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}


