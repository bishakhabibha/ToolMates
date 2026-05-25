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

    private static final int OTP_EXPIRY_SECONDS = 600;
    private static final int OTP_LENGTH = 6;

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private JavaMailSender mailSender;

    public void generateAndSend(String email) {
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(OTP_EXPIRY_SECONDS);
        otpStore.put(email.toLowerCase(), new OtpEntry(otp, expiresAt));
        sendEmail(email, otp);
    }

    public boolean verify(String email, String otp) {
        OtpEntry entry = otpStore.get(email.toLowerCase());
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStore.remove(email.toLowerCase());
            return false;
        }
        boolean match = entry.otp().equals(otp.trim());
        if (match) otpStore.remove(email.toLowerCase());
        return match;
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        return String.format("%0" + OTP_LENGTH + "d", random.nextInt(bound));
    }

    private void sendEmail(String to, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("ToolMates — Your verification code");
        msg.setText(
                "Hello,\n\n" +
                        "Your ToolMates email verification code is:\n\n" +
                        "    " + otp + "\n\n" +
                        "This code expires in 10 minutes.\n" +
                        "If you did not request this, you can safely ignore this email.\n\n" +
                        "— The ToolMates Team"
        );
        mailSender.send(msg);
    }

    private record OtpEntry(String otp, Instant expiresAt) {}
}