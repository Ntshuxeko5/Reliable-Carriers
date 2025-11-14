package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_role", columnList = "role"),
    @Index(name = "idx_users_active", columnList = "is_active"),
    @Index(name = "idx_users_role_active", columnList = "role,is_active"),
    @Index(name = "idx_users_phone", columnList = "phone"),
    @Index(name = "idx_users_is_business", columnList = "is_business"),
    @Index(name = "idx_users_customer_tier", columnList = "customer_tier")
})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String firstName;
    
    @Column(nullable = false, length = 50)
    private String lastName;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(length = 15)
    private String phone;
    
    @Column(length = 200)
    private String address;
    
    @Column(length = 50)
    private String city;
    
    @Column(length = 50)
    private String state;
    
    @Column(length = 10)
    private String zipCode;
    
    @Column(length = 50)
    private String country;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    // Compliance and preferences
    @Column(name = "waiver_accepted")
    private Boolean waiverAccepted;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "waiver_accepted_at")
    private Date waiverAcceptedAt;

    @Column(length = 20)
    private String insurancePreference; // BUDGET, BASIC, STANDARD, PREMIUM

    @Column(name = "profile_picture", length = 255)
    private String profilePicture; // URL to profile picture

    // 2FA TOTP fields
    @Column(name = "totp_secret", length = 128)
    private String totpSecret;

    @Column(name = "totp_enabled")
    private Boolean totpEnabled = false;

    // Email verification field - tracks if user has verified their email during registration
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    // Account active status - used for account management
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Customer tier for business logic
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier")
    private CustomerTier customerTier = CustomerTier.INDIVIDUAL;
    
    // Business registration fields
    @Column(name = "business_name", length = 200)
    private String businessName;
    
    @Column(name = "tax_id", length = 50)
    private String taxId;
    
    @Column(name = "registration_number", length = 50)
    private String registrationNumber;
    
    @Column(name = "is_business")
    private Boolean isBusiness = false;
    
    // Business verification fields
    @Enumerated(EnumType.STRING)
    @Column(name = "business_verification_status", length = 20)
    private BusinessVerificationStatus businessVerificationStatus = BusinessVerificationStatus.PENDING;
    
    @Column(name = "verification_notes", length = 1000)
    private String verificationNotes;
    
    @Column(name = "verified_by")
    private Long verifiedBy; // Admin user ID who verified
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "verified_at")
    private Date verifiedAt;
    
    // Business credit terms
    @Column(name = "credit_limit", precision = 12, scale = 2)
    private java.math.BigDecimal creditLimit = java.math.BigDecimal.ZERO;
    
    @Column(name = "payment_terms")
    private Integer paymentTerms = 0; // Days (e.g., 30 for Net 30)
    
    @Column(name = "current_balance", precision = 12, scale = 2)
    private java.math.BigDecimal currentBalance = java.math.BigDecimal.ZERO;
    
    // Driver-specific fields
    @Column(name = "driver_license_number", length = 50)
    private String driverLicenseNumber;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "license_expiry_date")
    private Date licenseExpiryDate;
    
    @Column(name = "vehicle_make", length = 50)
    private String vehicleMake;
    
    @Column(name = "vehicle_model", length = 50)
    private String vehicleModel;
    
    @Column(name = "vehicle_year")
    private Integer vehicleYear;
    
    @Column(name = "vehicle_registration", length = 20)
    private String vehicleRegistration;
    
    @Column(name = "vehicle_color", length = 30)
    private String vehicleColor;
    
    @Column(name = "vehicle_capacity_kg", precision = 10, scale = 2)
    private java.math.BigDecimal vehicleCapacityKg;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "driver_verification_status", length = 20)
    private DriverVerificationStatus driverVerificationStatus;
    
    @Column(name = "driver_verification_notes", length = 1000)
    private String driverVerificationNotes;
    
    @Column(name = "driver_verified_by")
    private Long driverVerifiedBy;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "driver_verified_at")
    private Date driverVerifiedAt;
    
    @Column(name = "is_online")
    private Boolean isOnline = false;
    
    @Column(name = "current_latitude", precision = 10, scale = 8)
    private java.math.BigDecimal currentLatitude;
    
    @Column(name = "current_longitude", precision = 11, scale = 8)
    private java.math.BigDecimal currentLongitude;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_location_update")
    private Date lastLocationUpdate;
    
    @Column(name = "driver_rating", precision = 3, scale = 2)
    private java.math.BigDecimal driverRating;
    
    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;
    
    @Column(name = "total_earnings", precision = 12, scale = 2)
    private java.math.BigDecimal totalEarnings = java.math.BigDecimal.ZERO;
    
    // Account lockout fields for security
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "account_locked")
    private Boolean accountLocked = false;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "account_locked_until")
    private Date accountLockedUntil;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (isBusiness != null && isBusiness && businessVerificationStatus == null) {
            businessVerificationStatus = BusinessVerificationStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getWaiverAccepted() {
        return waiverAccepted;
    }

    public void setWaiverAccepted(Boolean waiverAccepted) {
        this.waiverAccepted = waiverAccepted;
    }

    public Date getWaiverAcceptedAt() {
        return waiverAcceptedAt;
    }

    public void setWaiverAcceptedAt(Date waiverAcceptedAt) {
        this.waiverAcceptedAt = waiverAcceptedAt;
    }

    public String getInsurancePreference() {
        return insurancePreference;
    }

    public void setInsurancePreference(String insurancePreference) {
        this.insurancePreference = insurancePreference;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    // OAuth2 provider linking
    @Column(name = "oauth_provider", length = 50)
    private String oauthProvider;

    @Column(name = "oauth_provider_id", length = 128)
    private String oauthProviderId;

    public String getOauthProvider() {
        return oauthProvider;
    }

    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getOauthProviderId() {
        return oauthProviderId;
    }

    public void setOauthProviderId(String oauthProviderId) {
        this.oauthProviderId = oauthProviderId;
    }

    // 2FA accessors
    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public Boolean getTotpEnabled() {
        return totpEnabled;
    }

    public void setTotpEnabled(Boolean totpEnabled) {
        this.totpEnabled = totpEnabled;
    }

    // Customer tier accessors
    public CustomerTier getCustomerTier() {
        return customerTier;
    }

    public void setCustomerTier(CustomerTier customerTier) {
        this.customerTier = customerTier;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Boolean getIsBusiness() {
        return isBusiness;
    }

    public void setIsBusiness(Boolean isBusiness) {
        this.isBusiness = isBusiness;
    }

    public BusinessVerificationStatus getBusinessVerificationStatus() {
        return businessVerificationStatus;
    }

    public void setBusinessVerificationStatus(BusinessVerificationStatus businessVerificationStatus) {
        this.businessVerificationStatus = businessVerificationStatus;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }

    public Long getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(Long verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public Date getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Date verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public java.math.BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(java.math.BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Integer getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(Integer paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public java.math.BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(java.math.BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    // Driver-specific getters and setters
    public String getDriverLicenseNumber() { return driverLicenseNumber; }
    public void setDriverLicenseNumber(String driverLicenseNumber) { this.driverLicenseNumber = driverLicenseNumber; }
    
    public Date getLicenseExpiryDate() { return licenseExpiryDate; }
    public void setLicenseExpiryDate(Date licenseExpiryDate) { this.licenseExpiryDate = licenseExpiryDate; }
    
    public String getVehicleMake() { return vehicleMake; }
    public void setVehicleMake(String vehicleMake) { this.vehicleMake = vehicleMake; }
    
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    
    public Integer getVehicleYear() { return vehicleYear; }
    public void setVehicleYear(Integer vehicleYear) { this.vehicleYear = vehicleYear; }
    
    public String getVehicleRegistration() { return vehicleRegistration; }
    public void setVehicleRegistration(String vehicleRegistration) { this.vehicleRegistration = vehicleRegistration; }
    
    public String getVehicleColor() { return vehicleColor; }
    public void setVehicleColor(String vehicleColor) { this.vehicleColor = vehicleColor; }
    
    public java.math.BigDecimal getVehicleCapacityKg() { return vehicleCapacityKg; }
    public void setVehicleCapacityKg(java.math.BigDecimal vehicleCapacityKg) { this.vehicleCapacityKg = vehicleCapacityKg; }
    
    public DriverVerificationStatus getDriverVerificationStatus() { return driverVerificationStatus; }
    public void setDriverVerificationStatus(DriverVerificationStatus driverVerificationStatus) { this.driverVerificationStatus = driverVerificationStatus; }
    
    public String getDriverVerificationNotes() { return driverVerificationNotes; }
    public void setDriverVerificationNotes(String driverVerificationNotes) { this.driverVerificationNotes = driverVerificationNotes; }
    
    public Long getDriverVerifiedBy() { return driverVerifiedBy; }
    public void setDriverVerifiedBy(Long driverVerifiedBy) { this.driverVerifiedBy = driverVerifiedBy; }
    
    public Date getDriverVerifiedAt() { return driverVerifiedAt; }
    public void setDriverVerifiedAt(Date driverVerifiedAt) { this.driverVerifiedAt = driverVerifiedAt; }
    
    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }
    
    public java.math.BigDecimal getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(java.math.BigDecimal currentLatitude) { this.currentLatitude = currentLatitude; }
    
    public java.math.BigDecimal getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(java.math.BigDecimal currentLongitude) { this.currentLongitude = currentLongitude; }
    
    public Date getLastLocationUpdate() { return lastLocationUpdate; }
    public void setLastLocationUpdate(Date lastLocationUpdate) { this.lastLocationUpdate = lastLocationUpdate; }
    
    public java.math.BigDecimal getDriverRating() { return driverRating; }
    public void setDriverRating(java.math.BigDecimal driverRating) { this.driverRating = driverRating; }
    
    public Integer getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(Integer totalDeliveries) { this.totalDeliveries = totalDeliveries; }
    
    public java.math.BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(java.math.BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
    
    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    
    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }
    
    public Date getAccountLockedUntil() { return accountLockedUntil; }
    public void setAccountLockedUntil(Date accountLockedUntil) { this.accountLockedUntil = accountLockedUntil; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}