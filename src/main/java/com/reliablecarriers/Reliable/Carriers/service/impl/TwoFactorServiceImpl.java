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
import java.util.Optional;

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
        // Always log token at INFO level so it's visible in production logs (for debugging email/SMS issues)
        logger.info("2FA TOKEN GENERATED - User: {}, Token: {}, Method: {}", user.getEmail(), token, method);

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
                    // Token already logged above, no need to log again
                } catch (Exception e) {
                    logger.warn("Failed to send 2FA SMS to {}: {}, falling back to email", 
                                user.getPhone(), e.getMessage());
                    // Log token when SMS fails
                    logger.info("2FA TOKEN (SMS FAILED) - User: {}, Token: {}, Error: {}", 
                               user.getEmail(), token, e.getMessage());
                    try {
                        // Fallback to email if SMS fails
                        emailService.sendSimpleEmail(user.getEmail(), "Your verification code", message);
                        logger.info("2FA email sent successfully as fallback for user: {}", user.getEmail());
                        // Token already logged above
                    } catch (Exception emailException) {
                        logger.error("Both SMS and email failed for user {}: {}", 
                                   user.getEmail(), emailException.getMessage(), emailException);
                        // Token already logged above, but log again for visibility when both fail
                        logger.info("2FA TOKEN (BOTH FAILED) - User: {}, Token: {}", user.getEmail(), token);
                    }
                }
            } else {
                logger.warn("User phone number is null for user: {}, falling back to email", user.getEmail());
                try {
                    emailService.sendSimpleEmail(user.getEmail(), "Your verification code", message);
                    logger.info("2FA email sent successfully to user: {}", user.getEmail());
                    // Token already logged above
                } catch (Exception emailException) {
                    logger.error("Email failed for user {}: {}", user.getEmail(), emailException.getMessage(), emailException);
                    // Token already logged above, but log again for visibility when email fails
                    logger.info("2FA TOKEN (EMAIL FAILED) - User: {}, Token: {}, Error: {}", 
                               user.getEmail(), token, emailException.getMessage());
                }
            }
        } else {
            // default to email
            logger.debug("Attempting email delivery for user: {}", user.getEmail());
            try {
                emailService.sendSimpleEmail(user.getEmail(), "Your verification code", message);
                logger.info("2FA email sent successfully to user: {}", user.getEmail());
                // Token already logged above
            } catch (Exception e) {
                logger.error("Failed to send 2FA email to {}: {}", user.getEmail(), e.getMessage(), e);
                // Token already logged above, but log again for visibility when email fails
                logger.info("2FA TOKEN (EMAIL FAILED) - User: {}, Token: {}, Error: {}", 
                           user.getEmail(), token, e.getMessage());
                // Note: Token is still saved in database, user can use resend functionality
            }
        }
    }

    @Override
    public boolean verifyToken(User user, String token) {
        if (user == null || token == null) {
            logger.warn("verifyToken called with null user or token");
            return false;
        }
        
        // Normalize the token: trim whitespace and ensure it's a 6-digit string
        String cleanedToken = token.trim();
        
        // Remove any non-numeric characters (spaces, dashes, etc.)
        cleanedToken = cleanedToken.replaceAll("[^0-9]", "");
        
        // If token is numeric, ensure it's exactly 6 digits with leading zeros
        String normalizedToken;
        try {
            if (cleanedToken.isEmpty()) {
                logger.warn("Token is empty after normalization");
                return false;
            }
            
            // Parse as integer to remove any leading zeros that might cause issues
            int tokenInt = Integer.parseInt(cleanedToken);
            
            // Ensure it's a valid 6-digit code (0-999999)
            if (tokenInt < 0 || tokenInt > 999999) {
                logger.warn("Token out of valid range: {}", tokenInt);
                return false;
            }
            
            // Format as 6-digit string with leading zeros
            normalizedToken = String.format("%06d", tokenInt);
        } catch (NumberFormatException e) {
            // Token is not numeric, use as-is (shouldn't happen but handle gracefully)
            logger.warn("Token is not numeric: {}", cleanedToken);
            // If it's already 6 characters, use it as-is
            if (cleanedToken.length() != 6) {
                return false;
            }
            normalizedToken = cleanedToken;
        }
        
        // Make final for use in lambda
        final String finalNormalizedToken = normalizedToken;
        
        logger.debug("Verifying token for user: {}, normalized token: {}", user.getEmail(), finalNormalizedToken);
        
        Date now = new Date();
        
        // Try to find a valid (unused and not expired) token in a single query
        var found = tokenRepository.findValidTokenForUser(user, finalNormalizedToken, now);
        
        if (found.isPresent()) {
            // Token is valid - mark as used
            TwoFactorToken t = found.get();
            t.setUsed(true);
            tokenRepository.save(t);
            logger.info("2FA TOKEN VERIFIED SUCCESSFULLY - User: {}, Token: {}", user.getEmail(), finalNormalizedToken);
            return true;
        }
        
        // Token not found as unused - check if it was already used but is still valid (not expired)
        // This handles duplicate requests/race conditions where token was consumed but request is retried
        var usedToken = tokenRepository.findUsedButValidToken(user, finalNormalizedToken, now);
        
        if (usedToken.isPresent()) {
            // Token was already used but is still valid (not expired) - allow re-verification
            // This handles cases where the frontend makes duplicate requests or user clicks submit multiple times
            logger.info("2FA TOKEN ALREADY USED BUT STILL VALID - User: {}, Token: {} (allowing re-verification for duplicate request)", 
                user.getEmail(), finalNormalizedToken);
            return true;
        }
        
        // Token not found or expired - check what happened for better error messages
        var allTokens = tokenRepository.findByUserAndUsedFalseOrderByExpiresAtDesc(user);
        
        if (!allTokens.isEmpty()) {
            // Check if token exists but is expired
            Optional<TwoFactorToken> expiredToken = allTokens.stream()
                .filter(t -> t.getToken().equals(finalNormalizedToken))
                .findFirst();
            
            if (expiredToken.isPresent()) {
                TwoFactorToken t = expiredToken.get();
                logger.warn("2FA TOKEN EXPIRED - User: {}, Token: {}, Expires: {}, Current: {}", 
                    user.getEmail(), finalNormalizedToken, t.getExpiresAt(), now);
                return false;
            }
            
            // Token doesn't match any existing token
            logger.warn("2FA TOKEN NOT FOUND - User: {}, Entered Token: {}, Available tokens:", user.getEmail(), finalNormalizedToken);
            for (TwoFactorToken t : allTokens) {
                logger.info("  Expected Token: {}, Expires: {}, Used: {}", 
                    t.getToken(), t.getExpiresAt(), t.isUsed());
            }
        } else {
            logger.warn("2FA VERIFICATION FAILED - User: {}, Entered: {}, No unused tokens found. User may need to request a new code.", 
                user.getEmail(), finalNormalizedToken);
        }
        
        return false;
    }
}
