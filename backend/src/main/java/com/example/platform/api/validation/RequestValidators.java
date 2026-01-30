package com.example.platform.api.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

/**
 * Common request validation utilities
 * Includes: limit/offset defaults, E.164 phone normalization, URL validation
 */
public class RequestValidators {
    
    // Pagination defaults and limits
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;
    public static final int DEFAULT_OFFSET = 0;
    
    // URL length limits
    public static final int MAX_URL_LENGTH = 2048;
    
    /**
     * Validate and normalize limit parameter
     * 
     * @param limit User-provided limit (null for default)
     * @return Validated limit between 1 and MAX_LIMIT
     */
    public static int validateLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException(
                String.format("Limit must be between 1 and %d", MAX_LIMIT)
            );
        }
        return limit;
    }
    
    /**
     * Validate and normalize offset parameter
     * 
     * @param offset User-provided offset (null for default)
     * @return Validated offset >= 0
     */
    public static int validateOffset(Integer offset) {
        if (offset == null) {
            return DEFAULT_OFFSET;
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be >= 0");
        }
        return offset;
    }
    
    /**
     * Normalize phone number to E.164 format
     * Accepts input with spaces, dashes, parentheses
     * 
     * @param phoneNumber User-provided phone number
     * @return E.164 formatted phone number (e.g., +886912345678)
     * @throws IllegalArgumentException if phone number is invalid
     */
    public static String normalizePhoneE164(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            
            // Try parsing with default region (null means international format required)
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, null);
            
            // Validate the number
            if (!phoneUtil.isValidNumber(number)) {
                throw new IllegalArgumentException("Invalid phone number");
            }
            
            // Format to E.164
            return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
            
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Invalid phone number format: " + e.getMessage());
        }
    }
    
    /**
     * Validate URL format and length
     * Only allows http:// and https://
     * 
     * @param url User-provided URL
     * @throws IllegalArgumentException if URL is invalid
     */
    public static void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return; // URL is optional in most cases
        }
        
        if (url.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException(
                String.format("URL length must not exceed %d characters", MAX_URL_LENGTH)
            );
        }
        
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with http:// or https://");
        }
    }
}
