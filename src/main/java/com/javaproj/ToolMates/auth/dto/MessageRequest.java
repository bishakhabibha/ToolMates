package com.javaproj.ToolMates.auth.dto;

public class MessageRequest {

    private Long rentalRequestId;
    private Long senderId;
    private Long receiverId;
    private String senderStudentId;
    private String receiverStudentId;
    private Long otherUserId;
    private String messageText;

    public Long getRentalRequestId() { return rentalRequestId; }
    public void setRentalRequestId(Long rentalRequestId) { this.rentalRequestId = rentalRequestId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getSenderStudentId() { return senderStudentId; }
    public void setSenderStudentId(String senderStudentId) { this.senderStudentId = senderStudentId; }

    public String getReceiverStudentId() { return receiverStudentId; }
    public void setReceiverStudentId(String receiverStudentId) { this.receiverStudentId = receiverStudentId; }

    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
}
