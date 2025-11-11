package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.TwoFactorToken;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.TwoFactorTokenRepository;
import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import com.reliablecarriers.Reliable.Carriers.service.SmsService;
import com.reliablecarriers.Reliable.Carriers.service.TwoFactorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;

@Service
public class TwoFactorServiceImpl implements TwoFactorService {

    private final TwoFactorTokenRepository tokenRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Value("${app.2fa.token.ttl.minutes:10}")
    private int ttlMinutes;

    @Value("${app.2fa.enabled:true}")
    private boolean enabled;

    public TwoFactorServiceImpl(TwoFactorTokenRepository tokenRepository, EmailService emailService, SmsService smsService) {
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @Override
    @Transactional
    public void generateAndSendToken(User user, String method) {
        System.out.println("=== 2FA DEBUG START ===");
        System.out.println("User: " + user.getEmail() + ", Phone: " + user.getPhone());
        System.out.println("Method: " + method);
        System.out.println("2FA Enabled: " + this.enabled);
        
        // clear existing tokens
        tokenRepository.deleteByUser(user);

        // generate numeric 6-digit token
        String token = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        System.out.println("Generated token: " + token);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, ttlMinutes);
        Date expires = cal.getTime();

        TwoFactorToken t = new TwoFactorToken(token, method, expires, user);
        tokenRepository.save(t);
        System.out.println("Token saved to database");

        String message = "Your verification code is: " + token + ". It expires in " + ttlMinutes + " minutes.";

        if ("SMS".equalsIgnoreCase(method)) {
            System.out.println("Attempting SMS delivery...");
            if (user.getPhone() != null) {
                try {
                    smsService.sendSms(user.getPhone(), message);
                    System.out.println("2FA SMS sent to " + user.getPhone());
                } catch (Exception e) {
                    System.err.println("Failed to send 2FA SMS to " + user.getPhone() + ": " + e.getMessage());
                    System.out.println("Falling back to email...");
                    try {
                        // Fallback to email if SMS fails
                        emailService.sendSimpleEmail(user.getEmail(), "Your verification code", message);
                    } catch (Exception emailException) {
                        System.err.println("Both SMS and email failed: " + emailException.getMessage());
                        // Don't throw exception - just log the token for development
                        System.out.println("=== 2FA TOKEN (DEVELOPMENT MODE) ===");
                        System.out.println("Token: " + token);
                        System.out.println("User: " + user.getEmail());
                        System.out.println("=== END TOKEN ===");
                    }
                }
            } else {
                System.err.println("User phone number is null, falling back to email");
                try {
                    emailService.sendSimpleEmail(user.getEmail(), "Your verification code", message);
                } catch (Exception emailException) {
                    System.err.println("Email failed: " + emailException.getMessage());
                    System.out.println("=== 2FA TOKEN (DEVELOPMENT MODE) ===");
                    System.out.println("Token: " + token);
                    System.out.println("User: " + user.getEmail());
                    System.out.println("=== END TOKEN ===");
                }
            }
        } else {
            // default to email
            System.out.println("Attempting email delivery...");
            try {
                emailService.sendSimpleEmail(user.getEmail(), "Your verification code", message);
                System.out.println("2FA Email sent to " + user.getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send 2FA email to " + user.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
                // Don't throw exception - token is saved, user can use resend or check logs
                // Log the token for development/debugging purposes
                System.out.println("=== 2FA TOKEN (EMAIL FAILED - CHECK LOGS) ===");
                System.out.println("Token: " + token);
                System.out.println("User: " + user.getEmail());
                System.out.println("Error: " + e.getMessage());
                System.out.println("=== END TOKEN ===");
                // Note: Token is still saved in database, user can use resend functionality
            }
        }
        System.out.println("=== 2FA DEBUG END ===");
    }

    @Override
    public boolean verifyToken(User user, String token) {
        var found = tokenRepository.findByUserAndTokenAndUsedFalse(user, token);
        if (found.isEmpty()) return false;
        TwoFactorToken t = found.get();
        if (t.getExpiresAt() != null && t.getExpiresAt().before(new Date())) return false;
        t.setUsed(true);
        tokenRepository.save(t);
        return true;
    }
}
