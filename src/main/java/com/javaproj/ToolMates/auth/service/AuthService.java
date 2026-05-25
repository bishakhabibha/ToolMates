package com.javaproj.ToolMates.auth.service;

import com.javaproj.ToolMates.auth.dto.SignupRequest;
import com.javaproj.ToolMates.auth.exception.DuplicateFieldException;
import com.javaproj.ToolMates.auth.exception.EmailNotVerifiedException;
import com.javaproj.ToolMates.auth.model.User;
import com.javaproj.ToolMates.auth.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserDao userDao;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private OtpService otpService;

    public void sendVerificationOtp(String email) {
        otpService.generateAndSend(email);
    }

    public boolean verifyOtp(String email, String otp) {
        return otpService.verify(email, otp);
    }

    public User register(SignupRequest req, boolean emailVerified) {

        if (!emailVerified)
            throw new EmailNotVerifiedException("Please verify your email before signing up.");

        if (userDao.existsByStudentId(req.getStudentId()))
            throw new DuplicateFieldException("studentId", "This Student ID is already registered.");

        if (userDao.existsByStudentEmail(req.getStudentEmail().toLowerCase()))
            throw new DuplicateFieldException("studentEmail", "This email address is already registered.");

        User user = new User();
        user.setFirstName(req.getFirstName().trim());
        user.setLastName(req.getLastName().trim());
        user.setStudentId(req.getStudentId().trim());
        user.setDepartment(req.getDepartment());
        user.setStudentEmail(req.getStudentEmail().toLowerCase().trim());
        user.setPhone(req.getPhone().trim());
        user.setAddress(req.getAddress() != null ? req.getAddress().trim() : null);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setEmailVerified(true);

        return userDao.save(user);
    }
}