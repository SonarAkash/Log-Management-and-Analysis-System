package com.LogManagementSystem.LogManager.Security;

import java.time.Duration;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;

@Service
public class OtpService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private EmailService emailService;

    private static final long OTP_VALID_DURATION_SECONDS = 60; // 1 minute
    
    // 6-digit OTP
    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generates number between 100000 and 999999
        return String.valueOf(otp);
    }
    
    // Save OTP to Redis with expiry
    public void saveOTP(String email, String otp) {
        String key = getOtpKey(email);
        redisTemplate.opsForValue().set(key, otp, Duration.ofSeconds(OTP_VALID_DURATION_SECONDS));
    }
    
    // Get OTP from Redis
    public String getOTP(String email) {
        String key = getOtpKey(email);
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteOTP(String email) {
        String key = getOtpKey(email);
        redisTemplate.delete(key);
    }

    public void generateAndSendOTP(String email) throws MessagingException {
        try {
            String otp = generateOTP();
            emailService.sendOtpEmail(email, otp);
            // Only save OTP after successful email send
            saveOTP(email, otp);
        } catch (Exception e) {
            throw new MessagingException("Failed to send OTP email: " + e.getMessage());
        }
    }

    public boolean verifyOTP(String email, String otp) {
        String savedOtp = getOTP(email);
        if (savedOtp == null) {
            return false; // OTP expired or doesn't exist
        }
        boolean isValid = savedOtp.equals(otp);
        if (isValid) {
            deleteOTP(email); // Delete OTP after successful verification
        }
        return isValid;
    }

    private String getOtpKey(String email) {
        return "otp:" + email;
    }
}