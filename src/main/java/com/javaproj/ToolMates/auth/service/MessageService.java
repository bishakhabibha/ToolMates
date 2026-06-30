package com.javaproj.ToolMates.auth.service;

import com.javaproj.ToolMates.auth.dto.MessageRequest;
import com.javaproj.ToolMates.auth.model.Message;
import com.javaproj.ToolMates.auth.model.RentalRequest;
import com.javaproj.ToolMates.auth.model.User;
import com.javaproj.ToolMates.auth.repository.MessageDao;
import com.javaproj.ToolMates.auth.repository.NotificationDao;
import com.javaproj.ToolMates.auth.repository.RentalRequestDao;
import com.javaproj.ToolMates.auth.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private RentalRequestDao rentalRequestDao;

    public Message saveMessage(MessageRequest request) {
        Long senderId = resolveUserId(request.getSenderId(), request.getSenderStudentId());
        Long receiverId = request.getOtherUserId() != null
                ? request.getOtherUserId()
                : resolveUserId(request.getReceiverId(), request.getReceiverStudentId());

        if (request.getMessageText() == null || request.getMessageText().trim().isEmpty()) {
            throw new IllegalArgumentException("Message text is required.");
        }
        if (senderId.equals(receiverId)) throw new IllegalArgumentException("You cannot message yourself.");
        assertConversationAllowed(request.getRentalRequestId(), senderId, receiverId);

        Message message = new Message();
        message.setRentalRequestId(request.getRentalRequestId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessageText(request.getMessageText().trim());
        Message saved = messageDao.save(message);
        User sender = userDao.findByUserId(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found."));
        String senderName = (sender.getFirstName() + " " + sender.getLastName()).trim();
        notificationDao.create(receiverId, request.getRentalRequestId(), "You have unread messages from " + senderName + ".", "message");
        return saved;
    }

    public List<Message> getChatHistory(Long rentalRequestId, Long actorUserId) {
        RentalRequest rentalRequest = rentalRequestDao.findById(rentalRequestId);
        assertParticipant(rentalRequest, actorUserId);
        return messageDao.findByRentalRequestId(rentalRequestId);
    }

    public List<Map<String, Object>> getConversations(Long userId) {
        return messageDao.findConversationsForUser(userId);
    }

    public List<Message> getConversationWithUser(Long userId, Long otherUserId) {
        List<Message> messages = messageDao.findConversationBetweenUsers(userId, otherUserId);
        messageDao.markConversationRead(userId, otherUserId);
        return messages;
    }

    public List<Message> getConversationWithStudentId(Long userId, String otherStudentId) {
        Long otherUserId = resolveUserId(null, otherStudentId);
        return getConversationWithUser(userId, otherUserId);
    }

    private Long resolveUserId(Long userId, String studentId) {
        if (userId != null) return userId;
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("User is required.");
        }
        User user = userDao.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return user.getUserId();
    }

    private void assertConversationAllowed(Long rentalRequestId, Long senderId, Long receiverId) {
        if (rentalRequestId == null) return;
        RentalRequest rentalRequest = rentalRequestDao.findById(rentalRequestId);
        boolean senderIsParticipant = isParticipant(rentalRequest, senderId);
        boolean receiverIsParticipant = isParticipant(rentalRequest, receiverId);
        if (!senderIsParticipant || !receiverIsParticipant) {
            throw new IllegalArgumentException("Only rental participants can message in this rental chat.");
        }
    }

    private void assertParticipant(RentalRequest rentalRequest, Long actorUserId) {
        if (!isParticipant(rentalRequest, actorUserId)) {
            throw new IllegalArgumentException("Only rental participants can view this chat.");
        }
    }

    private boolean isParticipant(RentalRequest rentalRequest, Long userId) {
        if (rentalRequest == null || userId == null) return false;
        return userId.equals(rentalRequest.getOwnerId()) || userId.equals(rentalRequest.getBorrowerId());
    }
}
