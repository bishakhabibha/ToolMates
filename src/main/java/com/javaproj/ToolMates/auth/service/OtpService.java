package com.javaproj.ToolMates.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private JavaMailSender mailSender;

    public void generateAndSend(String email, Purpose purpose) {
        String key = storeKey(email, purpose);
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(OTP_EXPIRY_SECONDS);
        otpStore.put(key, new OtpEntry(otp, expiresAt));
        sendEmail(email, otp, purpose);
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
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);

        if (purpose == Purpose.SIGNUP) {
            msg.setSubject("ToolMates — Your verification code");
            msg.setText(
                    "Hello,\n\n" +
                            "Your ToolMates email verification code is:\n\n" +
                            "    " + otp + "\n\n" +
                            "This code expires in 10 minutes.\n" +
                            "If you did not request this, you can safely ignore this email.\n\n" +
                            "— The ToolMates Team"
            );
        } else {
            msg.setSubject("ToolMates — Password reset code");
            msg.setText(
                    "Hello,\n\n" +
                            "We received a request to reset your ToolMates password.\n\n" +
                            "Your reset code is:\n\n" +
                            "    " + otp + "\n\n" +
                            "This code expires in 10 minutes.\n" +
                            "If you did not request this, you can safely ignore this email.\n\n" +
                            "— The ToolMates Team"
            );
        }

        mailSender.send(msg);
    }

    private record OtpEntry(String otp, Instant expiresAt) {}
}