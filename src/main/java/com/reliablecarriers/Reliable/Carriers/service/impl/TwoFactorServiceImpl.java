package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.TwoFactorToken;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.TwoFactorTokenRepository;
import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import com.reliablecarriers.Reliable.Carriers.service.SmsService;
import com.reliablecarriers.Reliable.Carriers.service.TwoFactorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;

@Service
public class TwoFactorServiceImpl implements TwoFactorService {

    private static final Logger logger = LoggerFactory.getLogger(TwoFactorServiceImpl.class);

    private final TwoFactorTokenRepository tokenRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Value("${app.2fa.token.ttl.minutes:10}")
    private int ttlMinutes;

    @Value("${app.2fa.enabled:true}")
    private boolean enabled;

    @Value("${app.debug.mode:false}")
    private boolean debugMode;

    public TwoFactorServiceImpl(TwoFactorTokenRepository tokenRepository, EmailService emailService, SmsService smsService) {
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @Override
    @Transactional
    public void generateAndSendToken(User user, String method) {
        logger.debug("Generating 2FA token for user: {}, method: {}, 2FA enabled: {}", 
                     user.getEmail(), method, enabled);
        
        // clear existing tokens
        tokenRepository.deleteByUser(user);

        // generate numeric 6-digit token
        String token = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        logger.debug("Generated 2FA token for user: {}", user.getEmail());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, ttlMinutes);
        Date expires = cal.getTime();

        TwoFactorToken t = new TwoFactorToken(token, method, expires, user);
        tokenRepository.save(t);
        logger.debug("2FA token saved to database for user: {}", user.getEmail());

        String message = "Your verification code is: " + token + ". It expires in " + ttlMinutes + " minutes.";

        if ("SMS".equalsIgnoreCase(method)) {
            logger.debug("Attempting SMS delivery for user: {}", user.getEmail());
            if (user.getPhone() != null) {
                try {
                    smsService.sendSms(user.getPhone(), message);
                    logger.info("2FA SMS sent successfully to phone: {}", user.getPhone());
                } catch (Exception e) {
                    logger.warn("Failed to send 2FA SMS to {}: {}, falling back to email", 
                                user.getPhone(), e.getMessage());
                    try {
                        // Fallback to email if SMS fails
                        emailService.sendSimpleEmail(user.getEmail(), "Your verification code", message);
                        logger.info("2FA email sent successfully as fallback for user: {}", user.getEmail());
                    } catch (Exception emailException) {
                        logger.error("Both SMS and email failed for user {}: {}", 
                                   user.getEmail(), emailException.getMessage(), emailException);
                        if (debugMode) {
                            logger.debug("2FA Token (DEBUG MODE) - User: {}, Token: {}", user.getEmail(), token);
                        }
                    }
                }
            } else {
                logger.warn("User phone number is null for user: {}, falling back to email", user.getEmail());
                try {
                    emailService.sendSimpleEmail(user.getEmail(), "Your verification code", message);
                    logger.info("2FA email sent successfully to user: {}", user.getEmail());
                } catch (Exception emailException) {
                    logger.error("Email failed for user {}: {}", user.getEmail(), emailException.getMessage(), emailException);
                    if (debugMode) {
                        logger.debug("2FA Token (DEBUG MODE) - User: {}, Token: {}", user.getEmail(), token);
                    }
                }
            }
        } else {
            // default to email
            logger.debug("Attempting email delivery for user: {}", user.getEmail());
            try {
                emailService.sendSimpleEmail(user.getEmail(), "Your verification code", message);
                logger.info("2FA email sent successfully to user: {}", user.getEmail());
            } catch (Exception e) {
                logger.error("Failed to send 2FA email to {}: {}", user.getEmail(), e.getMessage(), e);
                // Don't throw exception - token is saved, user can use resend or check logs
                if (debugMode) {
                    logger.debug("2FA Token (DEBUG MODE - EMAIL FAILED) - User: {}, Token: {}, Error: {}", 
                               user.getEmail(), token, e.getMessage());
                }
                // Note: Token is still saved in database, user can use resend functionality
            }
        }
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
