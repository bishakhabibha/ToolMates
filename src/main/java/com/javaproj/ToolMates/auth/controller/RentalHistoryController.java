package com.javaproj.ToolMates.auth.controller;

import com.javaproj.ToolMates.auth.service.RentalHistoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rental-history")
public class RentalHistoryController {

    @Autowired
    private RentalHistoryService rentalHistoryService;

    @GetMapping("/borrowed")
    public ResponseEntity<?> borrowed(
            HttpSession session,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return unauthorized();
        return ResponseEntity.ok(rentalHistoryService.findBorrowed(userId, search, filter, sort, page, size));
    }

    @GetMapping("/lent")
    public ResponseEntity<?> lent(
            HttpSession session,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return unauthorized();
        return ResponseEntity.ok(rentalHistoryService.findLent(userId, search, filter, sort, page, size));
    }

    @GetMapping("/{rentalId}")
    public ResponseEntity<?> details(@PathVariable Long rentalId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return unauthorized();
        try {
            return ResponseEntity.ok(rentalHistoryService.findDetails(userId, rentalId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{rentalId}/timeline")
    public ResponseEntity<?> timeline(@PathVariable Long rentalId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return unauthorized();
        try {
            return ResponseEntity.ok(rentalHistoryService.findTimeline(userId, rentalId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{rentalId}/progress")
    public ResponseEntity<?> progress(@PathVariable Long rentalId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return unauthorized();
        try {
            return ResponseEntity.ok(rentalHistoryService.calculateProgress(userId, rentalId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in."));
    }
}
