package com.javaproj.ToolMates.auth.controller;

import com.javaproj.ToolMates.auth.dto.ConfirmationRequest;
import com.javaproj.ToolMates.auth.dto.PickupScheduleRequest;
import com.javaproj.ToolMates.auth.dto.RentalRequestCreateRequest;
import com.javaproj.ToolMates.auth.dto.ReportRequest;
import com.javaproj.ToolMates.auth.model.RentalRequest;
import com.javaproj.ToolMates.auth.service.RentalRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rent-requests")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342", "http://localhost:8080"}, allowCredentials = "true")
public class RentalRequestController {

    @Autowired
    private RentalRequestService rentalRequestService;

    @PostMapping("/add")
    public ResponseEntity<?> addRentalRequest(@RequestBody RentalRequestCreateRequest request) {
        try {
            RentalRequest saved = rentalRequestService.createRentalRequest(request);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not send rental request. Please make sure the notifications table exists in MySQL."));
        }
    }

    @PostMapping("/{rentalRequestId}/accept")
    public ResponseEntity<?> acceptRentalRequest(@PathVariable Long rentalRequestId, HttpSession session) {
        try {
            Long actorUserId = (Long) session.getAttribute("userId");
            return ResponseEntity.ok(rentalRequestService.acceptRentalRequest(rentalRequestId, actorUserId));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{rentalRequestId}/pickup-details")
    public ResponseEntity<?> submitPickupDetails(@PathVariable Long rentalRequestId, @RequestBody PickupScheduleRequest request, HttpSession session) {
        try {
            Long actorUserId = (Long) session.getAttribute("userId");
            return ResponseEntity.ok(rentalRequestService.submitPickupDetails(rentalRequestId, request, actorUserId));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{rentalRequestId}/pickup-details")
    public ResponseEntity<?> getPickupDetails(@PathVariable Long rentalRequestId, HttpSession session) {
        try {
            Long actorUserId = (Long) session.getAttribute("userId");
            return ResponseEntity.ok(rentalRequestService.getPickupDetails(rentalRequestId, actorUserId));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{rentalRequestId}/reject")
    public ResponseEntity<?> rejectRentalRequest(@PathVariable Long rentalRequestId, HttpSession session) {
        try {
            Long actorUserId = (Long) session.getAttribute("userId");
            rentalRequestService.rejectRentalRequest(rentalRequestId, actorUserId);
            return ResponseEntity.ok(Map.of("message", "Rental request rejected."));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{rentalRequestId}/close")
    public ResponseEntity<?> closeRentalRequest(@PathVariable Long rentalRequestId, HttpSession session) {
        try {
            Long actorUserId = (Long) session.getAttribute("userId");
            return ResponseEntity.ok(rentalRequestService.confirmReturn(rentalRequestId, actorUserId, true));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{rentalRequestId}/pickup-confirmation")
    public ResponseEntity<?> confirmPickup(@PathVariable Long rentalRequestId, @RequestBody ConfirmationRequest request, HttpSession session) {
        try {
            Long actorUserId = (Long) session.getAttribute("userId");
            return ResponseEntity.ok(rentalRequestService.confirmPickup(rentalRequestId, actorUserId, Boolean.TRUE.equals(request.getConfirmed())));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{rentalRequestId}/payment-confirmation")
    public ResponseEntity<?> confirmPayment(@PathVariable Long rentalRequestId, @RequestBody ConfirmationRequest request, HttpSession session) {
        try {
            Long actorUserId = (Long) session.getAttribute("userId");
            return ResponseEntity.ok(rentalRequestService.confirmPayment(rentalRequestId, actorUserId, Boolean.TRUE.equals(request.getConfirmed())));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{rentalRequestId}/return-confirmation")
    public ResponseEntity<?> confirmReturn(@PathVariable Long rentalRequestId, @RequestBody ConfirmationRequest request, HttpSession session) {
        try {
            Long actorUserId = (Long) session.getAttribute("userId");
            return ResponseEntity.ok(rentalRequestService.confirmReturn(rentalRequestId, actorUserId, Boolean.TRUE.equals(request.getConfirmed())));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{rentalRequestId}/cancel")
    public ResponseEntity<?> cancelRental(@PathVariable Long rentalRequestId, HttpSession session) {
        try {
            Long actorUserId = (Long) session.getAttribute("userId");
            rentalRequestService.cancelRental(rentalRequestId, actorUserId);
            return ResponseEntity.ok(Map.of("message", "Rental cancelled."));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{rentalRequestId}/report")
    public ResponseEntity<?> reportRentalIssue(@PathVariable Long rentalRequestId, @RequestBody ReportRequest request) {
        try {
            request.setRentalId(rentalRequestId);
            rentalRequestService.reportRentalIssue(request);
            return new ResponseEntity<>(Map.of("message", "Report submitted."), HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
