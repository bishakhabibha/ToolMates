package com.javaproj.ToolMates.auth.controller;

import com.javaproj.ToolMates.auth.dto.ReviewRequest;
import com.javaproj.ToolMates.auth.repository.ReviewDao;
import com.javaproj.ToolMates.auth.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342", "http://localhost:8080"}, allowCredentials = "true")
public class ReviewController {

    @Autowired
    private ReviewDao reviewDao;

    @Autowired
    private UserDao userDao;

    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody ReviewRequest request, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute("userId");
        if (loggedInUserId != null) request.setReviewerId(loggedInUserId);
        if (request.getReviewedUserId() == null && request.getReviewedStudentId() != null && !request.getReviewedStudentId().isBlank()) {
            userDao.findByStudentId(request.getReviewedStudentId().trim())
                    .ifPresent(user -> request.setReviewedUserId(user.getUserId()));
        }
        if (request.getRatingStars() == null || request.getRatingStars() < 1 || request.getRatingStars() > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5."));
        }
        if (request.getReviewerId() == null || request.getReviewedUserId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reviewer and reviewed user are required."));
        }
        if (request.getReviewerId().equals(request.getReviewedUserId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "You cannot review yourself."));
        }
        try {
            reviewDao.save(request);
            return new ResponseEntity<>(Map.of("message", "Review submitted."), HttpStatus.CREATED);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not submit review."));
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        try {
            return ResponseEntity.ok(reviewDao.findLeaderboard());
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
