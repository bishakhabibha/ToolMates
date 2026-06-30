package com.javaproj.ToolMates.auth.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RentalRequest {

    private Long rentalRequestId;
    private Long toolId;
    private Long ownerId;
    private Long borrowerId;
    private String borrowerName;
    private String renterId;
    private LocalDate startDate;
    private Integer durationDays;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime activeStartDate;
    private LocalDate pickupDate;
    private LocalTime pickupTime;
    private String pickupLocation;
    private String pickupInstructions;
    private String ownerPickupConfirmation;
    private String borrowerPickupConfirmation;
    private String ownerPaymentConfirmation;
    private String borrowerPaymentConfirmation;
    private LocalDateTime ownerPickupConfirmedAt;
    private LocalDateTime borrowerPickupConfirmedAt;
    private LocalDateTime ownerPaymentConfirmedAt;
    private LocalDateTime borrowerPaymentConfirmedAt;
    private Double totalRent;
    private Double advancePaid;
    private Double remainingBalance;
    private LocalDateTime paymentConfirmationTime;
    private LocalDateTime rentalStartAt;
    private LocalDateTime rentalEndAt;
    private LocalDateTime ownerReturnConfirmedAt;
    private LocalDateTime borrowerReturnConfirmedAt;
    private Boolean pickupNotificationSent;
    private Boolean endingReminderSent;
    private Boolean returnConfirmationNotificationSent;

    public Long getRentalRequestId() { return rentalRequestId; }
    public void setRentalRequestId(Long rentalRequestId) { this.rentalRequestId = rentalRequestId; }

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(Long borrowerId) { this.borrowerId = borrowerId; }

    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

    public String getRenterId() { return renterId; }
    public void setRenterId(String renterId) { this.renterId = renterId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getActiveStartDate() { return activeStartDate; }
    public void setActiveStartDate(LocalDateTime activeStartDate) { this.activeStartDate = activeStartDate; }

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public LocalTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalTime pickupTime) { this.pickupTime = pickupTime; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getPickupInstructions() { return pickupInstructions; }
    public void setPickupInstructions(String pickupInstructions) { this.pickupInstructions = pickupInstructions; }

    public String getOwnerPickupConfirmation() { return ownerPickupConfirmation; }
    public void setOwnerPickupConfirmation(String ownerPickupConfirmation) { this.ownerPickupConfirmation = ownerPickupConfirmation; }

    public String getBorrowerPickupConfirmation() { return borrowerPickupConfirmation; }
    public void setBorrowerPickupConfirmation(String borrowerPickupConfirmation) { this.borrowerPickupConfirmation = borrowerPickupConfirmation; }

    public String getOwnerPaymentConfirmation() { return ownerPaymentConfirmation; }
    public void setOwnerPaymentConfirmation(String ownerPaymentConfirmation) { this.ownerPaymentConfirmation = ownerPaymentConfirmation; }

    public String getBorrowerPaymentConfirmation() { return borrowerPaymentConfirmation; }
    public void setBorrowerPaymentConfirmation(String borrowerPaymentConfirmation) { this.borrowerPaymentConfirmation = borrowerPaymentConfirmation; }

    public LocalDateTime getOwnerPickupConfirmedAt() { return ownerPickupConfirmedAt; }
    public void setOwnerPickupConfirmedAt(LocalDateTime ownerPickupConfirmedAt) { this.ownerPickupConfirmedAt = ownerPickupConfirmedAt; }

    public LocalDateTime getBorrowerPickupConfirmedAt() { return borrowerPickupConfirmedAt; }
    public void setBorrowerPickupConfirmedAt(LocalDateTime borrowerPickupConfirmedAt) { this.borrowerPickupConfirmedAt = borrowerPickupConfirmedAt; }

    public LocalDateTime getOwnerPaymentConfirmedAt() { return ownerPaymentConfirmedAt; }
    public void setOwnerPaymentConfirmedAt(LocalDateTime ownerPaymentConfirmedAt) { this.ownerPaymentConfirmedAt = ownerPaymentConfirmedAt; }

    public LocalDateTime getBorrowerPaymentConfirmedAt() { return borrowerPaymentConfirmedAt; }
    public void setBorrowerPaymentConfirmedAt(LocalDateTime borrowerPaymentConfirmedAt) { this.borrowerPaymentConfirmedAt = borrowerPaymentConfirmedAt; }

    public Double getTotalRent() { return totalRent; }
    public void setTotalRent(Double totalRent) { this.totalRent = totalRent; }

    public Double getAdvancePaid() { return advancePaid; }
    public void setAdvancePaid(Double advancePaid) { this.advancePaid = advancePaid; }

    public Double getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(Double remainingBalance) { this.remainingBalance = remainingBalance; }

    public LocalDateTime getPaymentConfirmationTime() { return paymentConfirmationTime; }
    public void setPaymentConfirmationTime(LocalDateTime paymentConfirmationTime) { this.paymentConfirmationTime = paymentConfirmationTime; }

    public LocalDateTime getRentalStartAt() { return rentalStartAt; }
    public void setRentalStartAt(LocalDateTime rentalStartAt) { this.rentalStartAt = rentalStartAt; }

    public LocalDateTime getRentalEndAt() { return rentalEndAt; }
    public void setRentalEndAt(LocalDateTime rentalEndAt) { this.rentalEndAt = rentalEndAt; }

    public LocalDateTime getOwnerReturnConfirmedAt() { return ownerReturnConfirmedAt; }
    public void setOwnerReturnConfirmedAt(LocalDateTime ownerReturnConfirmedAt) { this.ownerReturnConfirmedAt = ownerReturnConfirmedAt; }

    public LocalDateTime getBorrowerReturnConfirmedAt() { return borrowerReturnConfirmedAt; }
    public void setBorrowerReturnConfirmedAt(LocalDateTime borrowerReturnConfirmedAt) { this.borrowerReturnConfirmedAt = borrowerReturnConfirmedAt; }

    public Boolean getPickupNotificationSent() { return pickupNotificationSent; }
    public void setPickupNotificationSent(Boolean pickupNotificationSent) { this.pickupNotificationSent = pickupNotificationSent; }

    public Boolean getEndingReminderSent() { return endingReminderSent; }
    public void setEndingReminderSent(Boolean endingReminderSent) { this.endingReminderSent = endingReminderSent; }

    public Boolean getReturnConfirmationNotificationSent() { return returnConfirmationNotificationSent; }
    public void setReturnConfirmationNotificationSent(Boolean returnConfirmationNotificationSent) { this.returnConfirmationNotificationSent = returnConfirmationNotificationSent; }
}
