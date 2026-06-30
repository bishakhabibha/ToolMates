package com.javaproj.ToolMates.auth.service;

import com.javaproj.ToolMates.auth.model.RentalRequest;
import com.javaproj.ToolMates.auth.repository.NotificationDao;
import com.javaproj.ToolMates.auth.repository.RentalRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
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

        for (RentalRequest rentalRequest : rentalRequestDao.findActiveRentalsDueWithinHours(6)) {
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

        for (RentalRequest rentalRequest : rentalRequestDao.findActiveRentalsExpiredForDays(2)) {
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
}
