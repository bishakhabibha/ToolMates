package com.javaproj.ToolMates.auth.controller;

import com.javaproj.ToolMates.auth.dto.LoginRequest;
import com.javaproj.ToolMates.auth.dto.SignupRequest;
import com.javaproj.ToolMates.auth.exception.DuplicateFieldException;
import com.javaproj.ToolMates.auth.exception.InvalidCredentialsException;
import com.javaproj.ToolMates.auth.model.User;
import com.javaproj.ToolMates.auth.repository.UserDao;
import com.javaproj.ToolMates.auth.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private UserDao userDao;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        try {
            User saved = authService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "firstName", saved.getFirstName(),
                    "lastName",  saved.getLastName(),
                    "message",   "Account created successfully!"
            ));
        } catch (DuplicateFieldException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("field", ex.getField(), "error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request.");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
