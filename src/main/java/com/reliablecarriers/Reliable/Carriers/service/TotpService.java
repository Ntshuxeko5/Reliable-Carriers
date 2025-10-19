package com.reliablecarriers.Reliable.Carriers.service;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * Minimal TOTP service (RFC 6238) using HMAC-SHA1 and Base32 secrets.
 * Replaces the external Google Authenticator dependency with a self-contained implementation.
 */
@Service
public class TotpService {
    private static final Base32 BASE32 = new Base32();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TIME_STEP_SECONDS = 30L;
    private static final int TOTP_DIGITS = 6;

    public String createSecret() {
        byte[] bytes = new byte[20]; // 160-bit secret
        RANDOM.nextBytes(bytes);
        return BASE32.encodeToString(bytes).replace("=", "");
    }

    public boolean authorize(String base32Secret, int code) {
        if (base32Secret == null) return false;
        long now = Instant.now().getEpochSecond();
        long t = now / TIME_STEP_SECONDS;

        // Allow a window of +/-1 time step
        for (long i = -1; i <= 1; i++) {
            int candidate = generateTOTP(base32Secret, t + i);
            if (candidate == code) return true;
        }
        return false;
    }

    private int generateTOTP(String base32Secret, long counter) {
        byte[] key = BASE32.decode(base32Secret);
        byte[] data = ByteBuffer.allocate(8).putLong(counter).array();

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hmac = mac.doFinal(data);

            int offset = hmac[hmac.length - 1] & 0x0F;
            int binary = ((hmac[offset] & 0x7f) << 24) |
                    ((hmac[offset + 1] & 0xff) << 16) |
                    ((hmac[offset + 2] & 0xff) << 8) |
                    (hmac[offset + 3] & 0xff);

            int otp = binary % (int) Math.pow(10, TOTP_DIGITS);
            return otp;
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            // Invalid secret or algorithm
            return -1;
        }
    }
}
