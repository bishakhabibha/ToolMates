package com.javaproj.ToolMates.auth.model;

import java.time.LocalDateTime;

public class Message {

    private Long messageId;
    private Long rentalRequestId;
    private Long senderId;
    private Long receiverId;
    private String messageText;
    private LocalDateTime sentAt;
    private boolean read;

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public Long getRentalRequestId() { return rentalRequestId; }
    public void setRentalRequestId(Long rentalRequestId) { this.rentalRequestId = rentalRequestId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
