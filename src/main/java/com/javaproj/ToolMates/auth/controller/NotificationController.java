package com.javaproj.ToolMates.auth.controller;

import com.javaproj.ToolMates.auth.model.User;
import com.javaproj.ToolMates.auth.repository.NotificationDao;
import com.javaproj.ToolMates.auth.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342", "http://localhost:8080"}, allowCredentials = "true")
public class NotificationController {

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private UserDao userDao;

    @GetMapping("/user/{studentId}")
    public ResponseEntity<?> getNotificationsByStudentId(@PathVariable String studentId, HttpSession session) {
        try {
            String loggedInStudentId = (String) session.getAttribute("studentId");
            if (loggedInStudentId == null || !loggedInStudentId.equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You can only view your own notifications."));
            }
            User user = userDao.findByStudentId(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
            return ResponseEntity.ok(notificationDao.findByUserId(user.getUserId()));
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyNotifications(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in."));
        try {
            return ResponseEntity.ok(notificationDao.findDetailedByUserId(userId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<?> getMyUnreadCount(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in."));
        return ResponseEntity.ok(Map.of("count", notificationDao.countUnread(userId)));
    }

    @PostMapping("/me/read")
    public ResponseEntity<?> markMineRead(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in."));
        notificationDao.markReadForUser(userId);
        return ResponseEntity.ok(Map.of("message", "Notifications marked read."));
    }
}
