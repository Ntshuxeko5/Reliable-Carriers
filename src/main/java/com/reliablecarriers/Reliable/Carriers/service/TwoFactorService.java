package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.User;

public interface TwoFactorService {
    /**
     * Generate and send a one-time token via the chosen method (EMAIL or SMS)
     */
    void generateAndSendToken(User user, String method);

    /**
     * Verify a token for a user
     */
    boolean verifyToken(User user, String token);
}
