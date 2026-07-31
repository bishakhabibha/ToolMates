package com.javaproj.ToolMates.auth.repository;

import com.javaproj.ToolMates.auth.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class NotificationDao {

    @Autowired
    private JdbcTemplate jdbc;

    public void create(Long userId, Long rentalRequestId, String messageText, String notificationType) {
        String sql = """
                INSERT INTO notifications (user_id, rental_request_id, message_text, notification_type)
                VALUES (?, ?, ?, ?)
                """;
        jdbc.update(sql, userId, rentalRequestId, messageText, notificationType);
    }

    public void createOnce(Long userId, Long rentalRequestId, String messageText, String notificationType) {
        if (existsByRentalRequestUserAndType(rentalRequestId, userId, notificationType)) return;
        create(userId, rentalRequestId, messageText, notificationType);
    }

    public void replaceUserRentalNotifications(Long userId, Long rentalRequestId, String messageText, String notificationType, String... typesToReplace) {
        if (typesToReplace != null && typesToReplace.length > 0) {
            String placeholders = String.join(",", java.util.Collections.nCopies(typesToReplace.length, "?"));
            Object[] params = new Object[typesToReplace.length + 2];
            params[0] = rentalRequestId;
            params[1] = userId;
            System.arraycopy(typesToReplace, 0, params, 2, typesToReplace.length);
            jdbc.update("DELETE FROM notifications WHERE rental_request_id = ? AND user_id = ? AND notification_type IN (" + placeholders + ")", params);
        }
        create(userId, rentalRequestId, messageText, notificationType);
    }

    public boolean existsByRentalRequestAndType(Long rentalRequestId, String notificationType) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE rental_request_id = ? AND notification_type = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, rentalRequestId, notificationType);
        return count != null && count > 0;
    }

    public boolean existsByRentalRequestUserAndType(Long rentalRequestId, Long userId, String notificationType) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE rental_request_id = ? AND user_id = ? AND notification_type = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, rentalRequestId, userId, notificationType);
        return count != null && count > 0;
    }

    public boolean existsRecentByRentalRequestUserAndType(Long rentalRequestId, Long userId, String notificationType, int minutes) {
        String sql = """
                SELECT COUNT(*)
                FROM notifications
                WHERE rental_request_id = ?
                  AND user_id = ?
                  AND notification_type = ?
                  AND created_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE)
                """;
        Integer count = jdbc.queryForObject(sql, Integer.class, rentalRequestId, userId, notificationType, minutes);
        return count != null && count > 0;
    }

    public List<Notification> findByUserId(Long userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        return jdbc.query(sql, new NotificationRowMapper(), userId);
    }

    public List<Map<String, Object>> findDetailedByUserId(Long userId) {
        String sql = """
                SELECT
                    n.notification_id AS notificationId,
                    n.user_id AS userId,
                    n.rental_request_id AS rentalRequestId,
                    n.message_text AS messageText,
                    n.notification_type AS notificationType,
                    n.is_read AS isRead,
                    n.is_read AS `read`,
                    n.created_at AS createdAt,
                    r.status AS rentalStatus,
                    r.start_date AS pickupDate,
                    r.pickup_date AS scheduledPickupDate,
                    r.pickup_time AS scheduledPickupTime,
                    r.scheduled_pickup_location AS scheduledPickupLocation,
                    r.pickup_instructions AS pickupInstructions,
                    r.owner_pickup_confirmation AS ownerPickupConfirmation,
                    r.borrower_pickup_confirmation AS borrowerPickupConfirmation,
                    r.owner_payment_confirmation AS ownerPaymentConfirmation,
                    r.borrower_payment_confirmation AS borrowerPaymentConfirmation,
                    r.owner_return_confirmed_at AS ownerReturnConfirmedAt,
                    r.borrower_return_confirmed_at AS borrowerReturnConfirmedAt,
                    r.total_rent AS totalRent,
                    r.advance_paid AS advancePaid,
                    r.remaining_balance AS remainingBalance,
                    r.rental_end_at AS rentalEndAt,
                    r.renter_id AS renterStudentId,
                    r.renter_name AS renterName,
                    t.id AS toolId,
                    t.name AS toolName,
                    owner.user_id AS ownerUserId,
                    owner.student_id AS ownerStudentId,
                    CONCAT(owner.first_name, ' ', owner.last_name) AS ownerName,
                    borrower.user_id AS borrowerUserId,
                    borrower.student_id AS borrowerStudentId,
                    CONCAT(borrower.first_name, ' ', borrower.last_name) AS borrowerName
                FROM notifications n
                LEFT JOIN rental_requests r ON r.id = n.rental_request_id
                LEFT JOIN tools t ON t.id = r.tool_id
                LEFT JOIN users owner ON owner.student_id = t.owner_id
                LEFT JOIN users borrower ON borrower.student_id = r.renter_id
                WHERE n.user_id = ?
                ORDER BY n.created_at DESC
                """;
        try {
            return jdbc.queryForList(sql, userId);
        } catch (DataAccessException ex) {
            return findBasicDetailsByUserId(userId);
        }
    }

    private List<Map<String, Object>> findBasicDetailsByUserId(Long userId) {
        String sql = """
                SELECT
                    notification_id AS notificationId,
                    user_id AS userId,
                    rental_request_id AS rentalRequestId,
                    message_text AS messageText,
                    notification_type AS notificationType,
                    is_read AS isRead,
                    is_read AS `read`,
                    created_at AS createdAt
                FROM notifications
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("notificationId", rs.getLong("notificationId"));
            row.put("userId", rs.getLong("userId"));
            long rentalRequestId = rs.getLong("rentalRequestId");
            row.put("rentalRequestId", rs.wasNull() ? null : rentalRequestId);
            row.put("messageText", rs.getString("messageText"));
            row.put("notificationType", rs.getString("notificationType"));
            row.put("isRead", rs.getBoolean("isRead"));
            row.put("read", rs.getBoolean("read"));
            Timestamp createdAt = rs.getTimestamp("createdAt");
            row.put("createdAt", createdAt == null ? null : createdAt.toLocalDateTime());
            return row;
        }, userId);
    }

    public int countUnread(Long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId);
        return count == null ? 0 : count;
    }

    public void markReadForUser(Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        jdbc.update(sql, userId);
    }

    public void markReadByRentalRequestAndTypes(Long rentalRequestId, String... notificationTypes) {
        if (notificationTypes == null || notificationTypes.length == 0) return;
        String placeholders = String.join(",", java.util.Collections.nCopies(notificationTypes.length, "?"));
        Object[] params = new Object[notificationTypes.length + 1];
        params[0] = rentalRequestId;
        System.arraycopy(notificationTypes, 0, params, 1, notificationTypes.length);
        jdbc.update("UPDATE notifications SET is_read = TRUE WHERE rental_request_id = ? AND notification_type IN (" + placeholders + ")", params);
    }

    public void markReadByRentalRequestUserAndTypes(Long rentalRequestId, Long userId, String... notificationTypes) {
        if (notificationTypes == null || notificationTypes.length == 0) return;
        String placeholders = String.join(",", java.util.Collections.nCopies(notificationTypes.length, "?"));
        Object[] params = new Object[notificationTypes.length + 2];
        params[0] = rentalRequestId;
        params[1] = userId;
        System.arraycopy(notificationTypes, 0, params, 2, notificationTypes.length);
        jdbc.update("UPDATE notifications SET is_read = TRUE WHERE rental_request_id = ? AND user_id = ? AND notification_type IN (" + placeholders + ")", params);
    }

    private static class NotificationRowMapper implements RowMapper<Notification> {
        @Override
        public Notification mapRow(ResultSet rs, int rowNum) throws SQLException {
            Notification notification = new Notification();
            notification.setNotificationId(rs.getLong("notification_id"));
            notification.setUserId(rs.getLong("user_id"));
            long rentalRequestId = rs.getLong("rental_request_id");
            if (!rs.wasNull()) notification.setRentalRequestId(rentalRequestId);
            notification.setMessageText(rs.getString("message_text"));
            notification.setNotificationType(rs.getString("notification_type"));
            notification.setRead(rs.getBoolean("is_read"));
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) notification.setCreatedAt(createdAt.toLocalDateTime());
            return notification;
        }
    }
}
