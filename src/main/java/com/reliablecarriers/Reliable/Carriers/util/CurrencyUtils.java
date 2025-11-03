package com.reliablecarriers.Reliable.Carriers.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Currency utility class for South African Rand (ZAR) formatting
 */
public class CurrencyUtils {
    
    @SuppressWarnings("deprecation")
    private static final Locale SOUTH_AFRICA = new Locale("en", "ZA");
    private static final String CURRENCY_CODE = "ZAR";
    private static final String CURRENCY_SYMBOL = "R";
    
    /**
     * Format amount as South African Rand
     * @param amount The amount to format
     * @return Formatted string (e.g., "R 1,234.56")
     */
    public static String formatZAR(BigDecimal amount) {
        if (amount == null) {
            return "R 0.00";
        }
        
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(SOUTH_AFRICA);
        return currencyFormatter.format(amount);
    }
    
    /**
     * Format amount as South African Rand without currency symbol
     * @param amount The amount to format
     * @return Formatted string (e.g., "1,234.56")
     */
    public static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        
        NumberFormat numberFormatter = NumberFormat.getNumberInstance(SOUTH_AFRICA);
        numberFormatter.setMinimumFractionDigits(2);
        numberFormatter.setMaximumFractionDigits(2);
        return numberFormatter.format(amount);
    }
    
    /**
     * Format amount with R symbol prefix
     * @param amount The amount to format
     * @return Formatted string (e.g., "R 1,234.56")
     */
    public static String formatWithSymbol(BigDecimal amount) {
        if (amount == null) {
            return "R 0.00";
        }
        return CURRENCY_SYMBOL + " " + formatAmount(amount);
    }
    
    /**
     * Get currency symbol
     * @return "R"
     */
    public static String getCurrencySymbol() {
        return CURRENCY_SYMBOL;
    }
    
    /**
     * Get currency code
     * @return "ZAR"
     */
    public static String getCurrencyCode() {
        return CURRENCY_CODE;
    }
    
    /**
     * Parse string amount to BigDecimal
     * @param amountString The amount string to parse
     * @return BigDecimal amount
     */
    public static BigDecimal parseAmount(String amountString) {
        if (amountString == null || amountString.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Remove currency symbols and spaces
        String cleaned = amountString.replaceAll("[R\\s,]", "");
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Format for display in JavaScript/JSON
     * @param amount The amount to format
     * @return Formatted string for frontend use
     */
    public static String formatForJS(BigDecimal amount) {
        if (amount == null) {
            return "R0.00";
        }
        return "R" + formatAmount(amount);
    }
}

