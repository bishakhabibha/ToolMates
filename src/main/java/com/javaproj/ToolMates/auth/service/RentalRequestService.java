package com.javaproj.ToolMates.auth.service;

import com.javaproj.ToolMates.auth.dto.PickupScheduleRequest;
import com.javaproj.ToolMates.auth.dto.RentalRequestCreateRequest;
import com.javaproj.ToolMates.auth.dto.ReportRequest;
import com.javaproj.ToolMates.auth.model.RentalRequest;
import com.javaproj.ToolMates.auth.model.Tool;
import com.javaproj.ToolMates.auth.model.User;
import com.javaproj.ToolMates.auth.repository.NotificationDao;
import com.javaproj.ToolMates.auth.repository.RentalRequestDao;
import com.javaproj.ToolMates.auth.repository.ReportDao;
import com.javaproj.ToolMates.auth.repository.ToolDao;
import com.javaproj.ToolMates.auth.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RentalRequestService {

    public static final String PENDING = "PENDING";
    public static final String OWNER_ACCEPTED = "OWNER_ACCEPTED";
    public static final String PICKUP_DETAILS_SUBMITTED = "PICKUP_DETAILS_SUBMITTED";
    public static final String BORROWER_REVIEWING_PICKUP_DETAILS = "BORROWER_REVIEWING_PICKUP_DETAILS";
    public static final String PICKUP_SCHEDULED = "PICKUP_SCHEDULED";
    public static final String WAITING_FOR_PICKUP = "WAITING_FOR_PICKUP";
    public static final String WAITING_FOR_PICKUP_TIME = "WAITING_FOR_PICKUP_TIME";
    public static final String WAITING_FOR_PICKUP_CONFIRMATION = "WAITING_FOR_PICKUP_CONFIRMATION";
    public static final String PICKUP_CONFIRMATION = "PICKUP_CONFIRMATION";
    public static final String ADVANCE_PAYMENT_CONFIRMATION = "ADVANCE_PAYMENT_CONFIRMATION";
    public static final String PICKUP_DISPUTE = "PICKUP_DISPUTE";
    public static final String PICKUP_FAILED = "PICKUP_FAILED";
    public static final String PAYMENT_DISPUTE = "PAYMENT_DISPUTE";
    public static final String PAYMENT_PENDING = "PAYMENT_PENDING";
    public static final String ACTIVE = "ACTIVE";
    public static final String AWAITING_RETURN = "AWAITING_RETURN";
    public static final String RETURN_CONFIRMATION = "RETURN_CONFIRMATION";
    public static final String RETURN_DISPUTE = "RETURN_DISPUTE";
    public static final String COMPLETED = "COMPLETED";
    public static final String REPORTED = "REPORTED";
    public static final String CANCELLED = "CANCELLED";
    public static final String REJECTED = "REJECTED";

    private static final DateTimeFormatter PICKUP_TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    @Autowired
    private RentalRequestDao rentalRequestDao;

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private ToolDao toolDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ReportDao reportDao;

    @Value("${app.demo-mode:false}")
    private boolean demoMode;

    @Value("${app.demo-minutes-per-day:1}")
    private long demoMinutesPerDay;

    @Transactional
    public RentalRequest createRentalRequest(RentalRequestCreateRequest request, Long actorUserId) {
        if (actorUserId == null) throw new IllegalArgumentException("You must be logged in.");
        Tool tool = toolDao.findById(request.getToolId());
        if (tool == null) throw new IllegalArgumentException("Tool not found.");
        if (!Boolean.TRUE.equals(tool.getActive())) {
            throw new IllegalArgumentException("This tool is no longer available for rent.");
        }

        User borrower = userDao.findByUserId(actorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Borrower not found."));
        if (tool.getOwnerId() != null && tool.getOwnerId().equalsIgnoreCase(borrower.getStudentId())) {
            throw new IllegalArgumentException("You cannot rent your own tool.");
        }
        User owner = userDao.findByStudentId(tool.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found."));

        int durationDays = request.getDurationDays() == null ? 0 : request.getDurationDays();
        if (request.getStartDate() == null) throw new IllegalArgumentException("Pickup date is required.");
        if (durationDays < 1) throw new IllegalArgumentException("Duration must be at least one day.");
        if (tool.getMaxRentingPeriod() != null && durationDays > tool.getMaxRentingPeriod()) {
            throw new IllegalArgumentException("Duration exceeds max renting period.");
        }

        RentalRequest rentalRequest = new RentalRequest();
        rentalRequest.setToolId(tool.getId());
        rentalRequest.setOwnerId(owner.getUserId());
        rentalRequest.setBorrowerId(borrower.getUserId());
        rentalRequest.setBorrowerName((borrower.getFirstName() + " " + borrower.getLastName()).trim());
        rentalRequest.setRenterId(borrower.getStudentId());
        rentalRequest.setStartDate(request.getStartDate());
        rentalRequest.setDurationDays(durationDays);
        rentalRequest.setMessage(request.getMessage());
        rentalRequest.setStatus(PENDING);

        RentalRequest saved = rentalRequestDao.save(rentalRequest);
        rentalRequestDao.logStatusTransition(saved.getRentalRequestId(), null, PENDING, borrower.getUserId(), "Rental request created.");
        rentalRequestDao.logAction(saved.getRentalRequestId(), borrower.getUserId(), "CREATE_RENTAL_REQUEST", request.getMessage());
        notificationDao.createOnce(
                owner.getUserId(),
                saved.getRentalRequestId(),
                "User " + borrower.getFirstName() + " " + borrower.getLastName() + " requested to rent your tool.",
                "rental_request"
        );
        return saved;
    }

    @Transactional
    public RentalRequest acceptRentalRequest(Long rentalRequestId, Long actorUserId) {
        RentalRequest rentalRequest = rentalRequestDao.findByIdForUpdate(rentalRequestId);
        assertActor(rentalRequest, actorUserId, true);
        assertStatus(rentalRequest, PENDING);
        int updated = rentalRequestDao.markOwnerAccepted(rentalRequestId);
        if (updated != 1) throw new IllegalArgumentException("Could not accept this request in its current state.");
        rentalRequestDao.logAction(rentalRequestId, actorUserId, "ACCEPT_RENTAL_REQUEST", "Owner opened pickup details form.");
        rentalRequestDao.logStatusTransition(rentalRequestId, PENDING, OWNER_ACCEPTED, actorUserId, "Owner accepted request.");
        return rentalRequestDao.findById(rentalRequestId);
    }

    @Transactional
    public RentalRequest submitPickupDetails(Long rentalRequestId, PickupScheduleRequest scheduleRequest, Long actorUserId) {
        RentalRequest rentalRequest = rentalRequestDao.findByIdForUpdate(rentalRequestId);
        assertActor(rentalRequest, actorUserId, true);
        if (!OWNER_ACCEPTED.equals(rentalRequest.getStatus())
                && !PICKUP_DISPUTE.equals(rentalRequest.getStatus())
                && !PICKUP_FAILED.equals(rentalRequest.getStatus())) {
            throw new IllegalArgumentException("Pickup details can only be submitted after owner acceptance or pickup failure/dispute.");
        }
        String fromStatus = rentalRequest.getStatus();
        validatePickupDetails(scheduleRequest);

        Tool tool = toolDao.findById(rentalRequest.getToolId());
        if (tool == null) throw new IllegalArgumentException("Tool not found.");
        User owner = userDao.findByUserId(rentalRequest.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found."));

        double totalRent = roundMoney((tool.getPricePerDay() == null ? 0 : tool.getPricePerDay()) * rentalRequest.getDurationDays());
        double advancePaid = roundMoney(totalRent * 0.40);
        double remainingBalance = roundMoney(totalRent - advancePaid);
        String pickupLocation = scheduleRequest.getPickupLocation().trim();
        String instructions = blankToNull(scheduleRequest.getPickupInstructions());

        rentalRequestDao.schedulePickup(
                rentalRequestId,
                scheduleRequest.getPickupDate(),
                scheduleRequest.getPickupTime(),
                pickupLocation,
                instructions,
                totalRent,
                advancePaid,
                remainingBalance
        );
        rentalRequestDao.logAction(rentalRequestId, actorUserId, "SUBMIT_PICKUP_DETAILS",
                scheduleRequest.getPickupDate() + " " + scheduleRequest.getPickupTime().format(PICKUP_TIME_FORMAT) + " at " + pickupLocation);
        rentalRequestDao.logStatusTransition(rentalRequestId, fromStatus, PICKUP_DETAILS_SUBMITTED, actorUserId, "Owner submitted pickup details.");
        notificationDao.createOnce(
                rentalRequest.getBorrowerId(),
                rentalRequestId,
                "User " + owner.getFirstName() + " " + owner.getLastName() + " accepted your rental request.",
                "pickup_details_available"
        );
        return rentalRequestDao.findById(rentalRequestId);
    }

    @Transactional
    public Map<String, Object> getPickupDetails(Long rentalRequestId, Long actorUserId) {
        RentalRequest rentalRequest = rentalRequestDao.findByIdForUpdate(rentalRequestId);
        assertParticipant(rentalRequest, actorUserId);
        if (PICKUP_DETAILS_SUBMITTED.equals(rentalRequest.getStatus())) {
            transition(rentalRequest, BORROWER_REVIEWING_PICKUP_DETAILS, actorUserId, "Borrower opened pickup details.");
            transition(rentalRequest, PICKUP_SCHEDULED, actorUserId, "Pickup details displayed to borrower.");
        } else if (!BORROWER_REVIEWING_PICKUP_DETAILS.equals(rentalRequest.getStatus())
                && !PICKUP_SCHEDULED.equals(rentalRequest.getStatus())
                && !WAITING_FOR_PICKUP_TIME.equals(rentalRequest.getStatus())
                && !PICKUP_CONFIRMATION.equals(rentalRequest.getStatus())
                && !ADVANCE_PAYMENT_CONFIRMATION.equals(rentalRequest.getStatus())
                && !ACTIVE.equals(rentalRequest.getStatus())) {
            throw new IllegalArgumentException("Pickup details are not available in this rental state.");
        }

        RentalRequest updated = rentalRequestDao.findById(rentalRequestId);
        Tool tool = toolDao.findById(updated.getToolId());
        User owner = userDao.findByUserId(updated.getOwnerId()).orElse(null);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("rentalRequestId", updated.getRentalRequestId());
        details.put("status", updated.getStatus());
        details.put("ownerName", owner == null ? "" : (owner.getFirstName() + " " + owner.getLastName()).trim());
        details.put("ownerUserId", updated.getOwnerId());
        details.put("toolName", tool == null ? "" : tool.getName());
        details.put("toolId", updated.getToolId());
        details.put("pickupDate", updated.getPickupDate());
        details.put("pickupTime", updated.getPickupTime() == null ? null : updated.getPickupTime().format(PICKUP_TIME_FORMAT));
        details.put("pickupLocation", updated.getPickupLocation());
        details.put("instructions", updated.getPickupInstructions());
        details.put("durationDays", updated.getDurationDays());
        details.put("totalRent", updated.getTotalRent());
        details.put("advancePaid", updated.getAdvancePaid());
        details.put("remainingBalance", updated.getRemainingBalance());
        return details;
    }

    @Transactional
    public void rejectRentalRequest(Long rentalRequestId, Long actorUserId) {
        RentalRequest rentalRequest = rentalRequestDao.findByIdForUpdate(rentalRequestId);
        assertActor(rentalRequest, actorUserId, true);
        if (REJECTED.equalsIgnoreCase(rentalRequest.getStatus())) return;
        assertStatus(rentalRequest, PENDING);
        rentalRequestDao.markRejected(rentalRequestId);
        rentalRequestDao.logAction(rentalRequestId, actorUserId, "REJECT_RENTAL_REQUEST", null);
        rentalRequestDao.logStatusTransition(rentalRequestId, rentalRequest.getStatus(), REJECTED, actorUserId, "Owner rejected rental request.");
        notificationDao.create(rentalRequest.getBorrowerId(), rentalRequestId, "Your rental request was rejected.", "rental_rejected");
    }

    @Transactional
    public RentalRequest confirmPickup(Long rentalRequestId, Long actorUserId, boolean confirmed) {
        RentalRequest rentalRequest = rentalRequestDao.findByIdForUpdate(rentalRequestId);
        boolean owner = assertParticipant(rentalRequest, actorUserId);
        normalizePickupConfirmationState(rentalRequest, actorUserId);
        if ((owner && rentalRequest.getOwnerPickupConfirmation() != null) || (!owner && rentalRequest.getBorrowerPickupConfirmation() != null)) {
            throw new IllegalArgumentException("Pickup confirmation already submitted by this user.");
        }

        int updatedRows = rentalRequestDao.recordPickupConfirmation(rentalRequestId, owner, confirmed);
        if (updatedRows != 1) throw new IllegalArgumentException("Pickup confirmation already submitted by this user.");
        rentalRequestDao.logAction(rentalRequestId, actorUserId, owner ? "OWNER_PICKUP_CONFIRMATION" : "BORROWER_PICKUP_CONFIRMATION", confirmed ? "YES" : "NO");

        RentalRequest updated = rentalRequestDao.findByIdForUpdate(rentalRequestId);
        if (updated.getOwnerPickupConfirmation() == null || updated.getBorrowerPickupConfirmation() == null) {
            Long otherUserId = owner ? updated.getBorrowerId() : updated.getOwnerId();
            notificationDao.createOnce(otherUserId, rentalRequestId, "The other user answered the pickup confirmation. Please answer YES or NO.", "pickup_confirmation_waiting");
            notificationDao.markReadByRentalRequestUserAndTypes(rentalRequestId, actorUserId, "pickup_confirmation_owner", "pickup_confirmation_borrower", "pickup_confirmation_waiting");
            return updated;
        }
        notificationDao.markReadByRentalRequestAndTypes(rentalRequestId, "pickup_confirmation_owner", "pickup_confirmation_borrower", "pickup_confirmation_waiting");

        boolean ownerYes = "YES".equals(updated.getOwnerPickupConfirmation());
        boolean borrowerYes = "YES".equals(updated.getBorrowerPickupConfirmation());
        if (ownerYes && borrowerYes) {
            transition(updated, ADVANCE_PAYMENT_CONFIRMATION, actorUserId, "Both users confirmed successful pickup.");
            notificationDao.createOnce(updated.getBorrowerId(), rentalRequestId, "Did you pay the required 40% advance payment?", "payment_confirmation_borrower");
            notificationDao.createOnce(updated.getOwnerId(), rentalRequestId, "Did you receive the required 40% advance payment?", "payment_confirmation_owner");
        } else if (!ownerYes && !borrowerYes) {
            transition(updated, PICKUP_FAILED, actorUserId, "Both users said pickup failed.");
            notifyBoth(updated, "Pickup failed. You may reschedule pickup, cancel rental, or message the other user.", "pickup_failed");
        } else {
            transition(updated, PICKUP_DISPUTE, actorUserId, "Pickup confirmations did not match.");
            notifyBoth(updated, "Pickup confirmations do not match. Please continue chat, report the user, reschedule pickup, or cancel rental.", "pickup_dispute");
        }
        return rentalRequestDao.findById(rentalRequestId);
    }

    private void normalizePickupConfirmationState(RentalRequest rentalRequest, Long actorUserId) {
        if (PICKUP_CONFIRMATION.equals(rentalRequest.getStatus())) return;
        if (WAITING_FOR_PICKUP.equals(rentalRequest.getStatus())
                || WAITING_FOR_PICKUP_TIME.equals(rentalRequest.getStatus())
                || WAITING_FOR_PICKUP_CONFIRMATION.equals(rentalRequest.getStatus())) {
            transition(rentalRequest, PICKUP_CONFIRMATION, actorUserId, "Pickup confirmation state normalized before user response.");
            return;
        }
        assertStatus(rentalRequest, PICKUP_CONFIRMATION);
    }

    @Transactional
    public RentalRequest confirmPayment(Long rentalRequestId, Long actorUserId, boolean confirmed) {
        RentalRequest rentalRequest = rentalRequestDao.findByIdForUpdate(rentalRequestId);
        boolean owner = assertParticipant(rentalRequest, actorUserId);
        if (!ADVANCE_PAYMENT_CONFIRMATION.equals(rentalRequest.getStatus()) && !PAYMENT_PENDING.equals(rentalRequest.getStatus())) {
            throw new IllegalArgumentException("Advance payment can only be confirmed after successful pickup confirmation.");
        }
        if ((owner && rentalRequest.getOwnerPaymentConfirmation() != null) || (!owner && rentalRequest.getBorrowerPaymentConfirmation() != null)) {
            throw new IllegalArgumentException("Payment confirmation already submitted by this user.");
        }

        int updatedRows = rentalRequestDao.recordPaymentConfirmation(rentalRequestId, owner, confirmed);
        if (updatedRows != 1) throw new IllegalArgumentException("Payment confirmation already submitted by this user.");
        rentalRequestDao.logAction(rentalRequestId, actorUserId, owner ? "OWNER_PAYMENT_CONFIRMATION" : "BORROWER_PAYMENT_CONFIRMATION", confirmed ? "YES" : "NO");

        RentalRequest updated = rentalRequestDao.findByIdForUpdate(rentalRequestId);
        if (updated.getOwnerPaymentConfirmation() == null || updated.getBorrowerPaymentConfirmation() == null) {
            Long otherUserId = owner ? updated.getBorrowerId() : updated.getOwnerId();
            notificationDao.createOnce(otherUserId, rentalRequestId, "The other user answered the advance payment confirmation. Please answer YES or NO.", "payment_confirmation_waiting");
            notificationDao.markReadByRentalRequestUserAndTypes(rentalRequestId, actorUserId, "payment_confirmation_owner", "payment_confirmation_borrower", "payment_confirmation_waiting", "payment_confirmation_reminder");
            return updated;
        }
        notificationDao.markReadByRentalRequestAndTypes(rentalRequestId, "payment_confirmation_owner", "payment_confirmation_borrower", "payment_confirmation_waiting", "payment_confirmation_reminder");

        boolean ownerYes = "YES".equals(updated.getOwnerPaymentConfirmation());
        boolean borrowerYes = "YES".equals(updated.getBorrowerPaymentConfirmation());
        if (ownerYes && borrowerYes) {
            LocalDateTime startAt = LocalDateTime.now();
            LocalDateTime endAt = calculateRentalEnd(startAt, updated.getDurationDays());
            rentalRequestDao.startRental(rentalRequestId, safeMoney(updated.getTotalRent()), safeMoney(updated.getAdvancePaid()), safeMoney(updated.getRemainingBalance()), startAt, endAt);
            rentalRequestDao.logStatusTransition(rentalRequestId, ADVANCE_PAYMENT_CONFIRMATION, ACTIVE, actorUserId, "Both users confirmed advance payment.");
            notifyBoth(updated, "Pickup and payment have been successfully confirmed. Your rental period has officially started.", "rental_started");
        } else if (!ownerYes && !borrowerYes) {
            transition(updated, PAYMENT_PENDING, actorUserId, "Both users said advance payment is not complete.");
            notifyBoth(updated, "Advance payment is still pending. The rental has not started.", "payment_pending");
        } else {
            rentalRequestDao.resetPaymentConfirmations(rentalRequestId);
            rentalRequestDao.logStatusTransition(rentalRequestId, updated.getStatus(), updated.getStatus(), actorUserId, "Advance payment confirmations did not match. Confirmation reset for retry.");
            notifyBoth(updated, "Payment confirmation does not match. Please resolve the issue and confirm payment again.", "payment_confirmation_waiting");
        }
        return rentalRequestDao.findById(rentalRequestId);
    }

    @Transactional
    public RentalRequest confirmReturn(Long rentalRequestId, Long actorUserId, boolean confirmed) {
        RentalRequest rentalRequest = rentalRequestDao.findByIdForUpdate(rentalRequestId);
        boolean owner = assertParticipant(rentalRequest, actorUserId);
        if (!AWAITING_RETURN.equals(rentalRequest.getStatus()) && !RETURN_CONFIRMATION.equals(rentalRequest.getStatus())) {
            throw new IllegalArgumentException("Return can only be confirmed after the rental is awaiting return.");
        }
        if ((owner && rentalRequest.getOwnerReturnConfirmedAt() != null) || (!owner && rentalRequest.getBorrowerReturnConfirmedAt() != null)) {
            throw new IllegalArgumentException("Return already confirmed.");
        }
        if (!confirmed) {
            transition(rentalRequest, RETURN_DISPUTE, actorUserId, "Return disputed by participant.");
            notificationDao.markReadByRentalRequestUserAndTypes(rentalRequestId, actorUserId, "return_confirmation", "return_confirmation_waiting");
            notificationDao.createOnce(actorUserId, rentalRequestId, "Please submit the report form for this return issue.", "report_required");
            notifyBoth(rentalRequest, "Return confirmation does not match. Please resolve the issue or report the user.", "return_dispute");
            return rentalRequestDao.findById(rentalRequestId);
        }
        rentalRequestDao.confirmReturn(rentalRequestId, owner);
        rentalRequestDao.logAction(rentalRequestId, actorUserId, owner ? "OWNER_CONFIRMED_RETURN" : "BORROWER_CONFIRMED_RETURN", null);
        RentalRequest updated = rentalRequestDao.findByIdForUpdate(rentalRequestId);
        notificationDao.markReadByRentalRequestUserAndTypes(rentalRequestId, actorUserId, "return_confirmation", "return_confirmation_waiting");
        if (updated.getOwnerReturnConfirmedAt() != null && updated.getBorrowerReturnConfirmedAt() != null) {
            transition(updated, COMPLETED, actorUserId, "Both parties confirmed return.");
            notificationDao.markReadByRentalRequestAndTypes(rentalRequestId, "return_confirmation", "return_confirmation_waiting");
            notifyBoth(updated, "Rental completed. Please review each other.", "rental_completed");
        } else {
            transition(updated, RETURN_CONFIRMATION, actorUserId, "Waiting for the other party to confirm return.");
            Long otherUserId = owner ? updated.getBorrowerId() : updated.getOwnerId();
            notificationDao.createOnce(otherUserId, rentalRequestId, owner ? "The owner confirmed return. Did you return the tool?" : "The borrower confirmed return. Please confirm receipt.", "return_confirmation_waiting");
        }
        return rentalRequestDao.findById(rentalRequestId);
    }

    @Transactional
    public void cancelRental(Long rentalRequestId, Long actorUserId) {
        RentalRequest rentalRequest = rentalRequestDao.findByIdForUpdate(rentalRequestId);
        assertParticipant(rentalRequest, actorUserId);
        if (!OWNER_ACCEPTED.equals(rentalRequest.getStatus())
                && !PICKUP_DETAILS_SUBMITTED.equals(rentalRequest.getStatus())
                && !PICKUP_SCHEDULED.equals(rentalRequest.getStatus())
                && !PICKUP_DISPUTE.equals(rentalRequest.getStatus())
                && !PICKUP_FAILED.equals(rentalRequest.getStatus())
                && !PAYMENT_DISPUTE.equals(rentalRequest.getStatus())
                && !PAYMENT_PENDING.equals(rentalRequest.getStatus())) {
            throw new IllegalArgumentException("Rental cannot be cancelled in its current state.");
        }
        transition(rentalRequest, CANCELLED, actorUserId, "Rental cancelled.");
        notifyBoth(rentalRequest, "Rental has been cancelled.", "rental_cancelled");
    }

    @Transactional
    public Map<String, Object> getReportStatus(Long rentalRequestId, Long actorUserId) {
        RentalRequest rentalRequest = rentalRequestDao.findById(rentalRequestId);
        boolean owner = assertParticipant(rentalRequest, actorUserId);
        Long reportedId = owner ? rentalRequest.getBorrowerId() : rentalRequest.getOwnerId();
        return Map.of(
                "alreadyReported", reportDao.existsByRentalAndReporter(rentalRequestId, actorUserId),
                "reportedUserId", reportedId,
                "rentalId", rentalRequestId,
                "toolId", rentalRequest.getToolId()
        );
    }

    @Transactional
    public void reportRentalIssue(ReportRequest reportRequest, Long actorUserId) {
        RentalRequest rentalRequest = rentalRequestDao.findByIdForUpdate(reportRequest.getRentalId());
        boolean owner = assertParticipant(rentalRequest, actorUserId);
        if (reportDao.existsByRentalAndReporter(rentalRequest.getRentalRequestId(), actorUserId)) {
            throw new IllegalArgumentException("You have already submitted a report for this rental.");
        }
        if (reportRequest.getReasonCategory() == null || reportRequest.getReasonCategory().isBlank()) {
            throw new IllegalArgumentException("Report reason is required.");
        }
        if (reportRequest.getReasonCategory().contains("Other")
                && (reportRequest.getAdditionalDetails() == null || reportRequest.getAdditionalDetails().isBlank())) {
            throw new IllegalArgumentException("Additional details are required when Other is selected.");
        }
        reportRequest.setReporterId(actorUserId);
        reportRequest.setReportedId(owner ? rentalRequest.getBorrowerId() : rentalRequest.getOwnerId());
        reportRequest.setToolId(rentalRequest.getToolId());
        reportDao.save(reportRequest);
        rentalRequestDao.logAction(rentalRequest.getRentalRequestId(), actorUserId, "SUBMIT_USER_REPORT", reportRequest.getReasonCategory());
    }

    @Transactional
    public void beginPickupConfirmation(RentalRequest rentalRequest) {
        RentalRequest locked = rentalRequestDao.findByIdForUpdate(rentalRequest.getRentalRequestId());
        if (!PICKUP_SCHEDULED.equals(locked.getStatus()) || Boolean.TRUE.equals(locked.getPickupNotificationSent())) return;
        Tool tool = toolDao.findById(locked.getToolId());
        User owner = userDao.findByUserId(locked.getOwnerId()).orElse(null);
        User borrower = userDao.findByUserId(locked.getBorrowerId()).orElse(null);
        String toolName = tool == null ? "the tool" : tool.getName();
        String ownerName = owner == null ? "the owner" : (owner.getFirstName() + " " + owner.getLastName()).trim();
        String borrowerName = borrower == null ? "the borrower" : (borrower.getFirstName() + " " + borrower.getLastName()).trim();
        String ownerStudentId = owner == null ? "" : owner.getStudentId();
        String borrowerStudentId = borrower == null ? "" : borrower.getStudentId();
        transition(locked, WAITING_FOR_PICKUP_TIME, null, "Scheduled pickup time passed.");
        transition(locked, PICKUP_CONFIRMATION, null, "Pickup confirmation workflow started one minute after pickup time.");
        notificationDao.replaceUserRentalNotifications(
                locked.getOwnerId(),
                locked.getRentalRequestId(),
                "Did you hand over " + toolName + " to " + borrowerName + " (" + borrowerStudentId + ")?",
                "pickup_confirmation_owner",
                "pickup_reminder_24h",
                "pickup_reminder_2h",
                "pickup_confirmation_owner"
        );
        notificationDao.replaceUserRentalNotifications(
                locked.getBorrowerId(),
                locked.getRentalRequestId(),
                "Did you receive " + toolName + " from " + ownerName + " (" + ownerStudentId + ")?",
                "pickup_confirmation_borrower",
                "pickup_reminder_24h",
                "pickup_reminder_2h",
                "pickup_confirmation_borrower"
        );
        rentalRequestDao.markPickupNotificationSent(locked.getRentalRequestId());
    }

    @Transactional
    public void moveActiveToAwaitingReturn(RentalRequest rentalRequest) {
        RentalRequest locked = rentalRequestDao.findByIdForUpdate(rentalRequest.getRentalRequestId());
        if (!ACTIVE.equals(locked.getStatus())) return;
        transition(locked, AWAITING_RETURN, null, "Rental end date reached.");
        notificationDao.create(locked.getBorrowerId(), locked.getRentalRequestId(), "Please return the tool. Remaining balance may still be due.", "return_due_borrower");
        notificationDao.create(locked.getOwnerId(), locked.getRentalRequestId(), "Your tool should be returned today.", "return_due_owner");
    }

    private void validatePickupDetails(PickupScheduleRequest scheduleRequest) {
        if (scheduleRequest == null) throw new IllegalArgumentException("Pickup details are required.");
        if (scheduleRequest.getPickupDate() == null) throw new IllegalArgumentException("Pickup date is required.");
        if (scheduleRequest.getPickupTime() == null) throw new IllegalArgumentException("Pickup time is required.");
        if (scheduleRequest.getPickupLocation() == null || scheduleRequest.getPickupLocation().isBlank()) {
            throw new IllegalArgumentException("Pickup location is required.");
        }
        if (LocalDateTime.of(scheduleRequest.getPickupDate(), scheduleRequest.getPickupTime()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Pickup date and time cannot be in the past.");
        }
    }

    private void transition(RentalRequest rentalRequest, String toStatus, Long changedBy, String reason) {
        if (toStatus.equals(rentalRequest.getStatus())) return;
        String fromStatus = rentalRequest.getStatus();
        rentalRequestDao.updateStatus(rentalRequest.getRentalRequestId(), toStatus);
        rentalRequestDao.logStatusTransition(rentalRequest.getRentalRequestId(), fromStatus, toStatus, changedBy, reason);
        rentalRequest.setStatus(toStatus);
    }

    private void notifyBoth(RentalRequest rentalRequest, String message, String type) {
        notificationDao.create(rentalRequest.getOwnerId(), rentalRequest.getRentalRequestId(), message, type);
        notificationDao.create(rentalRequest.getBorrowerId(), rentalRequest.getRentalRequestId(), message, type);
    }

    private void assertStatus(RentalRequest rentalRequest, String expectedStatus) {
        if (!expectedStatus.equals(rentalRequest.getStatus())) {
            throw new IllegalArgumentException("Invalid rental state. Expected " + expectedStatus + " but found " + rentalRequest.getStatus() + ".");
        }
    }

    private void assertActor(RentalRequest rentalRequest, Long actorUserId, boolean mustBeOwner) {
        if (actorUserId == null) throw new IllegalArgumentException("You must be logged in.");
        if (mustBeOwner && !actorUserId.equals(rentalRequest.getOwnerId())) {
            throw new IllegalArgumentException("Only the owner can perform this action.");
        }
    }

    private boolean assertParticipant(RentalRequest rentalRequest, Long actorUserId) {
        if (actorUserId == null) throw new IllegalArgumentException("You must be logged in.");
        if (actorUserId.equals(rentalRequest.getOwnerId())) return true;
        if (actorUserId.equals(rentalRequest.getBorrowerId())) return false;
        throw new IllegalArgumentException("Only rental participants can perform this action.");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double safeMoney(Double value) {
        return value == null ? 0 : value;
    }

    private LocalDateTime calculateRentalEnd(LocalDateTime startAt, Integer durationDays) {
        int days = durationDays == null ? 0 : durationDays;
        if (!demoMode) return startAt.plusDays(days);
        return startAt.plusMinutes(Math.max(1, days) * Math.max(1, demoMinutesPerDay));
    }
}
