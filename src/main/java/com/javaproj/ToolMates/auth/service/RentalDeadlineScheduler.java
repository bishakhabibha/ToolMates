package com.javaproj.ToolMates.auth.service;

import com.javaproj.ToolMates.auth.model.RentalRequest;
import com.javaproj.ToolMates.auth.repository.NotificationDao;
import com.javaproj.ToolMates.auth.repository.RentalRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RentalDeadlineScheduler {

    @Autowired
    private RentalRequestDao rentalRequestDao;

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private RentalRequestService rentalRequestService;

    @Value("${app.demo-mode:false}")
    private boolean demoMode;

    @Value("${app.demo-ending-reminder-seconds:30}")
    private long demoEndingReminderSeconds;

    @Value("${app.demo-owner-return-check-seconds:60}")
    private long demoOwnerReturnCheckSeconds;

    @Scheduled(fixedDelayString = "${app.scheduler.rental-deadline-delay-ms:30000}")
    public void checkRentalDeadlines() {
        createPickupReminders(24 * 60, "pickup_reminder_24h", "Pickup is scheduled in 24 hours.");
        createPickupReminders(2 * 60, "pickup_reminder_2h", "Pickup is scheduled in 2 hours.");

        for (RentalRequest rentalRequest : rentalRequestDao.findPickupsDueNow()) {
            if (!notificationDao.existsByRentalRequestAndType(rentalRequest.getRentalRequestId(), "pickup_confirmation_owner")) {
                rentalRequestService.beginPickupConfirmation(rentalRequest);
            }
        }

        for (RentalRequest rentalRequest : rentalRequestDao.findPaymentConfirmationsWaiting()) {
            if (rentalRequest.getOwnerPaymentConfirmedAt() == null
                    && !notificationDao.existsRecentByRentalRequestUserAndType(rentalRequest.getRentalRequestId(), rentalRequest.getOwnerId(), "payment_confirmation_reminder", 30)) {
                notificationDao.create(rentalRequest.getOwnerId(), rentalRequest.getRentalRequestId(), "Advance payment confirmation is still pending.", "payment_confirmation_reminder");
            }
            if (rentalRequest.getBorrowerPaymentConfirmedAt() == null
                    && !notificationDao.existsRecentByRentalRequestUserAndType(rentalRequest.getRentalRequestId(), rentalRequest.getBorrowerId(), "payment_confirmation_reminder", 30)) {
                notificationDao.create(rentalRequest.getBorrowerId(), rentalRequest.getRentalRequestId(), "Advance payment confirmation is still pending.", "payment_confirmation_reminder");
            }
        }

        for (RentalRequest rentalRequest : rentalRequestDao.findPickupConfirmationsWaiting()) {
            if (rentalRequest.getOwnerPickupConfirmation() == null
                    && !notificationDao.existsRecentByRentalRequestUserAndType(rentalRequest.getRentalRequestId(), rentalRequest.getOwnerId(), "pickup_confirmation_waiting", pickupReminderMinutes())) {
                notificationDao.create(rentalRequest.getOwnerId(), rentalRequest.getRentalRequestId(), "Pickup confirmation is still pending. Please answer YES or NO.", "pickup_confirmation_waiting");
            }
            if (rentalRequest.getBorrowerPickupConfirmation() == null
                    && !notificationDao.existsRecentByRentalRequestUserAndType(rentalRequest.getRentalRequestId(), rentalRequest.getBorrowerId(), "pickup_confirmation_waiting", pickupReminderMinutes())) {
                notificationDao.create(rentalRequest.getBorrowerId(), rentalRequest.getRentalRequestId(), "Pickup confirmation is still pending. Please answer YES or NO.", "pickup_confirmation_waiting");
            }
        }

        for (RentalRequest rentalRequest : rentalRequestDao.findActiveRentalsEndingWithinSeconds(endingReminderSeconds())) {
            if (!notificationDao.existsByRentalRequestAndType(rentalRequest.getRentalRequestId(), "rental_expiry_warning")) {
                notificationDao.create(
                        rentalRequest.getBorrowerId(),
                        rentalRequest.getRentalRequestId(),
                        "Your rental period ends in 6 hours. Please prepare to return the tool and settle any remaining balance.",
                        "rental_expiry_warning"
                );
            }
        }

        for (RentalRequest rentalRequest : rentalRequestDao.findRentalsEndingNow()) {
            if (!notificationDao.existsByRentalRequestAndType(rentalRequest.getRentalRequestId(), "return_due_owner")) {
                rentalRequestService.moveActiveToAwaitingReturn(rentalRequest);
            }
        }

        for (RentalRequest rentalRequest : rentalRequestDao.findRentalsReadyForReturnCheckAfterSeconds(returnCheckSeconds())) {
            if (!notificationDao.existsByRentalRequestAndType(rentalRequest.getRentalRequestId(), "return_confirmation")) {
                notificationDao.create(
                        rentalRequest.getOwnerId(),
                        rentalRequest.getRentalRequestId(),
                        "Was this tool returned to you?",
                        "return_confirmation"
                );
            }
        }
    }

    private void createPickupReminders(int minutesBefore, String notificationType, String message) {
        for (RentalRequest rentalRequest : rentalRequestDao.findPickupRemindersDue(minutesBefore, notificationType)) {
            notificationDao.create(rentalRequest.getOwnerId(), rentalRequest.getRentalRequestId(), message, notificationType);
            notificationDao.create(rentalRequest.getBorrowerId(), rentalRequest.getRentalRequestId(), message, notificationType);
        }
    }

    private long endingReminderSeconds() {
        return demoMode ? Math.max(1, demoEndingReminderSeconds) : 6 * 60 * 60;
    }

    private long returnCheckSeconds() {
        return demoMode ? Math.max(1, demoOwnerReturnCheckSeconds) : 2 * 24 * 60 * 60;
    }

    private int pickupReminderMinutes() {
        return demoMode ? 1 : 60;
    }
}
