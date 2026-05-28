package com.javaproj.ToolMates.auth.controller;

import com.javaproj.ToolMates.auth.dto.SignupRequest;
import com.javaproj.ToolMates.auth.exception.DuplicateFieldException;
import com.javaproj.ToolMates.auth.exception.EmailNotVerifiedException;
import com.javaproj.ToolMates.auth.model.User;
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
                    "userId",    saved.getUserId(),
                    "firstName", saved.getFirstName(),
                    "lastName",  saved.getLastName(),
                    "message",   "Account created! Your User ID is " + saved.getUserId() + "."
            ));
        } catch (EmailNotVerifiedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));
        } catch (DuplicateFieldException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("field", ex.getField(), "error", ex.getMessage()));
        }
    }
}