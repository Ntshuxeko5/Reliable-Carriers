package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "two_factor_tokens")
public class TwoFactorToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String token;

    @Column(nullable = false, length = 20)
    private String method; // EMAIL or SMS

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public TwoFactorToken() {}

    public TwoFactorToken(String token, String method, Date expiresAt, User user) {
        this.token = token;
        this.method = method;
        this.expiresAt = expiresAt;
        this.user = user;
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
