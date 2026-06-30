package com.javaproj.ToolMates.auth.repository;

import com.javaproj.ToolMates.auth.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Repository
public class MessageDao {

    @Autowired
    private JdbcTemplate jdbc;

    public Message save(Message message) {
        String sql = """
                INSERT INTO messages (rental_request_id, sender_id, receiver_id, message_text)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (message.getRentalRequestId() == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, message.getRentalRequestId());
            }
            ps.setLong(2, message.getSenderId());
            ps.setLong(3, message.getReceiverId());
            ps.setString(4, message.getMessageText());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) message.setMessageId(key.longValue());
        return message;
    }

    public List<Message> findByRentalRequestId(Long rentalRequestId) {
        String sql = "SELECT * FROM messages WHERE rental_request_id = ? ORDER BY sent_at ASC";
        return jdbc.query(sql, new MessageRowMapper(), rentalRequestId);
    }

    public List<Message> findConversationBetweenUsers(Long userId, Long otherUserId) {
        String sql = """
                SELECT *
                FROM messages
                WHERE (sender_id = ? AND receiver_id = ?)
                   OR (sender_id = ? AND receiver_id = ?)
                ORDER BY sent_at ASC, message_id ASC
                """;
        return jdbc.query(sql, new MessageRowMapper(), userId, otherUserId, otherUserId, userId);
    }

    public void markConversationRead(Long userId, Long otherUserId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE receiver_id = ? AND sender_id = ?";
        jdbc.update(sql, userId, otherUserId);
    }

    public List<Map<String, Object>> findConversationsForUser(Long userId) {
        String sql = """
                SELECT
                    CASE WHEN m.sender_id = ? THEN m.receiver_id ELSE m.sender_id END AS otherUserId,
                    MAX(m.sent_at) AS lastSentAt,
                    SUBSTRING_INDEX(GROUP_CONCAT(m.message_text ORDER BY m.sent_at DESC SEPARATOR '\\n'), '\\n', 1) AS lastMessage,
                    MAX(CASE WHEN m.sender_id = ? THEN receiver.first_name ELSE sender.first_name END) AS otherFirstName,
                    MAX(CASE WHEN m.sender_id = ? THEN receiver.last_name ELSE sender.last_name END) AS otherLastName,
                    MAX(CASE WHEN m.sender_id = ? THEN receiver.student_id ELSE sender.student_id END) AS otherStudentId,
                    SUM(CASE WHEN m.receiver_id = ? AND m.is_read = FALSE THEN 1 ELSE 0 END) AS unreadCount
                FROM messages m
                JOIN users sender ON sender.user_id = m.sender_id
                JOIN users receiver ON receiver.user_id = m.receiver_id
                WHERE m.sender_id = ? OR m.receiver_id = ?
                GROUP BY otherUserId
                ORDER BY lastSentAt DESC
                """;
        return jdbc.queryForList(sql, userId, userId, userId, userId, userId, userId, userId);
    }

    private static class MessageRowMapper implements RowMapper<Message> {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            Message message = new Message();
            message.setMessageId(rs.getLong("message_id"));
            long rentalRequestId = rs.getLong("rental_request_id");
            if (!rs.wasNull()) message.setRentalRequestId(rentalRequestId);
            message.setSenderId(rs.getLong("sender_id"));
            message.setReceiverId(rs.getLong("receiver_id"));
            message.setMessageText(rs.getString("message_text"));
            Timestamp sentAt = rs.getTimestamp("sent_at");
            if (sentAt != null) message.setSentAt(sentAt.toLocalDateTime());
            try {
                message.setRead(rs.getBoolean("is_read"));
            } catch (SQLException ignored) {
                message.setRead(false);
            }
            return message;
        }
    }
}
