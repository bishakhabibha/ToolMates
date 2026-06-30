package com.javaproj.ToolMates.auth.model;

import java.time.LocalDateTime;

public class Notification {

    private Long notificationId;
    private Long userId;
    private Long rentalRequestId;
    private String messageText;
    private String notificationType;
    private boolean read;
    private LocalDateTime createdAt;

    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRentalRequestId() { return rentalRequestId; }
    public void setRentalRequestId(Long rentalRequestId) { this.rentalRequestId = rentalRequestId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
