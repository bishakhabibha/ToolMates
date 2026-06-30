package com.javaproj.ToolMates.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    public enum Purpose { SIGNUP, PASSWORD_RESET }

    private static final int OTP_EXPIRY_SECONDS = 600;
    private static final int OTP_LENGTH = 6;

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private EmailService emailService;

    public void generateAndSend(String email, Purpose purpose) {
        String key = storeKey(email, purpose);
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(OTP_EXPIRY_SECONDS);
        sendEmail(email, otp, purpose);
        otpStore.put(key, new OtpEntry(otp, expiresAt));
    }

    public boolean verify(String email, String otp, Purpose purpose) {
        String key = storeKey(email, purpose);
        OtpEntry entry = otpStore.get(key);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStore.remove(key);
            return false;
        }
        boolean match = entry.otp().equals(otp.trim());
        if (match) otpStore.remove(key);
        return match;
    }

    private String storeKey(String email, Purpose purpose) {
        return purpose.name() + ":" + email.toLowerCase();
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        return String.format("%0" + OTP_LENGTH + "d", random.nextInt(bound));
    }

    private void sendEmail(String to, String otp, Purpose purpose) {
        emailService.sendOTPEmail(to, otp);
    }

    private record OtpEntry(String otp, Instant expiresAt) {}
}
