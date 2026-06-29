package com.javaproj.ToolMates.auth.controller;

import com.javaproj.ToolMates.auth.dto.ForgotPasswordRequest;
import com.javaproj.ToolMates.auth.dto.LoginRequest;
import com.javaproj.ToolMates.auth.dto.ResetPasswordRequest;
import com.javaproj.ToolMates.auth.dto.SignupRequest;
import com.javaproj.ToolMates.auth.exception.DuplicateFieldException;
import com.javaproj.ToolMates.auth.exception.EmailNotVerifiedException;
import com.javaproj.ToolMates.auth.exception.InvalidCredentialsException;
import com.javaproj.ToolMates.auth.exception.UserNotFoundException;
import com.javaproj.ToolMates.auth.model.User;
import com.javaproj.ToolMates.auth.repository.UserDao;
import com.javaproj.ToolMates.auth.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:63342", allowCredentials = "true")
public class AuthController {

    private static final String SESSION_VERIFIED_EMAIL = "verifiedEmail";

    @Autowired private AuthService authService;
    @Autowired private UserDao userDao;

    // ── Existing: Signup ─────────────────────────────────────────────────

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));

        authService.sendVerificationOtp(email.trim().toLowerCase());
        return ResponseEntity.ok(Map.of("message", "OTP sent. Please check your inbox."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body, HttpSession session) {
        String email = body.get("email");
        String otp   = body.get("otp");

        if (email == null || otp == null)
            return ResponseEntity.badRequest().body(Map.of("error", "email and otp are required."));

        boolean valid = authService.verifyOtp(email.trim().toLowerCase(), otp.trim());
        if (!valid)
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("error", "Invalid or expired code. Please try again."));

        session.setAttribute(SESSION_VERIFIED_EMAIL, email.trim().toLowerCase());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req, HttpSession session) {
        boolean emailVerified = true;

        try {
            User saved = authService.register(req, emailVerified);
            session.invalidate();
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "firstName", saved.getFirstName(),
                    "lastName",  saved.getLastName(),
                    "message",   "Account created successfully!"
            ));
        } catch (EmailNotVerifiedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));
        } catch (DuplicateFieldException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("field", ex.getField(), "error", ex.getMessage()));
        }
    }

    // ── New: Login & Logout ───────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        try {
            User user = authService.login(req, session);
            return ResponseEntity.ok(Map.of(
                    "userId",    user.getUserId(),
                    "studentId", user.getStudentId(),
                    "firstName", user.getFirstName(),
                    "lastName",  user.getLastName(),
                    "message",   "Login successful."
            ));
        } catch (InvalidCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String studentId = (String) session.getAttribute("studentId");
        String email = (String) session.getAttribute("email");
        if (userId == null || studentId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not logged in."));
        }
        User user = userDao.findByUserId(userId).orElse(null);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "studentId", studentId,
                "email", email == null ? "" : email,
                "firstName", user == null ? "" : user.getFirstName(),
                "lastName", user == null ? "" : user.getLastName()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    // ── New: Forgot / Reset Password ─────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        try {
            authService.sendPasswordResetOtp(req.getEmail().trim().toLowerCase());
        } catch (UserNotFoundException ignored) {
            // Don't reveal whether the email exists
        }
        return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, a reset code has been sent."
        ));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyResetOtp(@RequestBody Map<String, String> body, HttpSession session) {
        String email = body.get("email");
        String otp   = body.get("otp");

        if (email == null || otp == null)
            return ResponseEntity.badRequest().body(Map.of("error", "email and otp are required."));

        boolean valid = authService.verifyPasswordResetOtp(email, otp);
        if (!valid)
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("error", "Invalid or expired reset code. Please try again."));

        session.setAttribute(AuthService.SESSION_RESET_VERIFIED_EMAIL, email.trim().toLowerCase());
        return ResponseEntity.ok(Map.of("message", "OTP verified. You may now set a new password."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest req, HttpSession session) {
        try {
            authService.resetPassword(req.getEmail(), req.getNewPassword(), session);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully. Please log in."));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
