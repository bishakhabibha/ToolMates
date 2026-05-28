package com.javaproj.ToolMates.auth.service;

import com.javaproj.ToolMates.auth.dto.LoginRequest;
import com.javaproj.ToolMates.auth.dto.SignupRequest;
import com.javaproj.ToolMates.auth.exception.DuplicateFieldException;
import com.javaproj.ToolMates.auth.exception.EmailNotVerifiedException;
import com.javaproj.ToolMates.auth.exception.InvalidCredentialsException;
import com.javaproj.ToolMates.auth.exception.UserNotFoundException;
import com.javaproj.ToolMates.auth.model.User;
import com.javaproj.ToolMates.auth.repository.UserDao;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    public static final String SESSION_RESET_VERIFIED_EMAIL = "resetVerifiedEmail";

    @Autowired private UserDao        userDao;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private OtpService     otpService;

    // ── Existing: Signup ─────────────────────────────────────────────────

    public void sendVerificationOtp(String email) {
        otpService.generateAndSend(email, OtpService.Purpose.SIGNUP);
    }

    public boolean verifyOtp(String email, String otp) {
        return otpService.verify(email, otp, OtpService.Purpose.SIGNUP);
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

    // ── New: Login ───────────────────────────────────────────────────────

    public User login(LoginRequest req, HttpSession session) {
        String identifier = req.getUsername().trim();

        // Try student ID first, then email
        Optional<User> optUser = userDao.findByStudentId(identifier);
        if (optUser.isEmpty()) {
            optUser = userDao.findByEmail(identifier.toLowerCase());
        }

        User user = optUser.orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        int maxAge = req.isRememberMe() ? 60 * 60 * 24 * 30 : -1;
        session.setMaxInactiveInterval(maxAge);
        session.setAttribute("userId",    user.getUserId());
        session.setAttribute("studentId", user.getStudentId());
        session.setAttribute("email",     user.getStudentEmail());

        return user;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    // ── New: Forgot / Reset Password ─────────────────────────────────────

    public void sendPasswordResetOtp(String email) {
        String normalised = email.trim().toLowerCase();
        if (!userDao.existsByEmail(normalised))
            throw new UserNotFoundException(normalised);
        otpService.generateAndSend(normalised, OtpService.Purpose.PASSWORD_RESET);
    }

    public boolean verifyPasswordResetOtp(String email, String otp) {
        return otpService.verify(email.trim().toLowerCase(), otp.trim(), OtpService.Purpose.PASSWORD_RESET);
    }

    public void resetPassword(String email, String newPassword, HttpSession session) {
        String normalised = email.trim().toLowerCase();

        String sessionEmail = (String) session.getAttribute(SESSION_RESET_VERIFIED_EMAIL);
        if (sessionEmail == null || !sessionEmail.equals(normalised))
            throw new SecurityException("Password reset was not authorised for this email.");

        User user = userDao.findByEmail(normalised)
                .orElseThrow(() -> new UserNotFoundException(normalised));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userDao.updatePassword(user.getUserId(), user.getPasswordHash());

        session.removeAttribute(SESSION_RESET_VERIFIED_EMAIL);
    }
}