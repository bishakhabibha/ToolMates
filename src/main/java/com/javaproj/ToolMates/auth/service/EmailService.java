package com.javaproj.ToolMates.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // MATCHED METHOD NAME HERE
    public void sendOTPEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("toolmates.support@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Your ToolMates Signup OTP");
        message.setText("Your OTP code is: " + otp);

        mailSender.send(message);
    }
}