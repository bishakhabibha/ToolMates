package com.javaproj.ToolMates.auth.controller;

import com.javaproj.ToolMates.auth.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342", "http://localhost:8080"}, allowCredentials = "true")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/{studentId}")
    public ResponseEntity<?> getProfile(@PathVariable String studentId) {
        try {
            return ResponseEntity.ok(profileService.getProfile(studentId));
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(HttpSession session) {
        String studentId = (String) session.getAttribute("studentId");
        if (studentId == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return getProfile(studentId);
    }
}
