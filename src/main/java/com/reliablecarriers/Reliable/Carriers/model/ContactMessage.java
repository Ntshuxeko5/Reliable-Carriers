package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String firstName;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String lastName;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;
    
    @Column(length = 20)
    @Size(max = 20)
    private String phone;
    
    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String subject;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String message;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.NEW;
    
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date readAt;
    
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date repliedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_by_user_id")
    private User repliedBy;
    
    @Column(columnDefinition = "TEXT")
    private String adminNotes;
    
    public enum MessageStatus {
        NEW,
        READ,
        REPLIED,
        ARCHIVED
    }
    
    // Constructors
    public ContactMessage() {
    }
    
    public ContactMessage(String firstName, String lastName, String email, String phone, 
                         String subject, String message) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.subject = subject;
        this.message = message;
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
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public MessageStatus getStatus() {
        return status;
    }
    
    public void setStatus(MessageStatus status) {
        this.status = status;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getReadAt() {
        return readAt;
    }
    
    public void setReadAt(Date readAt) {
        this.readAt = readAt;
    }
    
    public Date getRepliedAt() {
        return repliedAt;
    }
    
    public void setRepliedAt(Date repliedAt) {
        this.repliedAt = repliedAt;
    }
    
    public User getRepliedBy() {
        return repliedBy;
    }
    
    public void setRepliedBy(User repliedBy) {
        this.repliedBy = repliedBy;
    }
    
    public String getAdminNotes() {
        return adminNotes;
    }
    
    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

