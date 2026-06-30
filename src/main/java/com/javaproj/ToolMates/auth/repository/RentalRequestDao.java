package com.javaproj.ToolMates.auth.repository;

import com.javaproj.ToolMates.auth.model.RentalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class RentalRequestDao {

    @Autowired
    private JdbcTemplate jdbc;

    private static final String RENTAL_SELECT = """
            SELECT
                r.*,
                borrower.user_id AS borrower_user_id,
                owner.user_id AS owner_user_id
            FROM rental_requests r
            JOIN tools t ON t.id = r.tool_id
            JOIN users borrower ON borrower.student_id = r.renter_id
            JOIN users owner ON owner.student_id = t.owner_id
            """;

    public RentalRequest save(RentalRequest request) {
        String sql = """
                INSERT INTO rental_requests
                    (tool_id, renter_name, renter_id, start_date, duration_days, message, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, request.getToolId());
            ps.setString(2, request.getBorrowerName());
            ps.setString(3, request.getRenterId());
            ps.setDate(4, Date.valueOf(request.getStartDate()));
            ps.setInt(5, request.getDurationDays());
            ps.setString(6, request.getMessage());
            ps.setString(7, request.getStatus());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) request.setRentalRequestId(key.longValue());
        return request;
    }

    public RentalRequest findById(Long rentalRequestId) {
        String sql = RENTAL_SELECT + " WHERE r.id = ?";
        return jdbc.queryForObject(sql, new RentalRequestRowMapper(), rentalRequestId);
    }

    public RentalRequest findByIdForUpdate(Long rentalRequestId) {
        String sql = RENTAL_SELECT + " WHERE r.id = ? FOR UPDATE";
        return jdbc.queryForObject(sql, new RentalRequestRowMapper(), rentalRequestId);
    }

    public int markOwnerAccepted(Long rentalRequestId) {
        String sql = "UPDATE rental_requests SET status = 'OWNER_ACCEPTED' WHERE id = ? AND status = 'PENDING'";
        return jdbc.update(sql, rentalRequestId);
    }

    public void markActive(Long rentalRequestId) {
        String sql = "UPDATE rental_requests SET status = 'ACTIVE', active_start_date = NOW() WHERE id = ?";
        jdbc.update(sql, rentalRequestId);
    }

    public void schedulePickup(Long rentalRequestId, java.time.LocalDate pickupDate, java.time.LocalTime pickupTime, String pickupLocation,
                               String pickupInstructions, double totalRent, double advancePaid, double remainingBalance) {
        String sql = """
                UPDATE rental_requests
                SET status = 'PICKUP_DETAILS_SUBMITTED',
                    pickup_date = ?,
                    pickup_time = ?,
                    scheduled_pickup_location = ?,
                    pickup_instructions = ?,
                    total_rent = ?,
                    advance_paid = ?,
                    remaining_balance = ?,
                    pickup_notification_sent = FALSE,
                    owner_pickup_confirmed_at = NULL,
                    borrower_pickup_confirmed_at = NULL,
                    owner_payment_confirmed_at = NULL,
                    borrower_payment_confirmed_at = NULL,
                    owner_pickup_confirmation = NULL,
                    borrower_pickup_confirmation = NULL,
                    owner_payment_confirmation = NULL,
                    borrower_payment_confirmation = NULL
                WHERE id = ?
                """;
        jdbc.update(sql, Date.valueOf(pickupDate), Time.valueOf(pickupTime), pickupLocation, pickupInstructions,
                totalRent, advancePaid, remainingBalance, rentalRequestId);
    }

    public void updateStatus(Long rentalRequestId, String status) {
        String sql = "UPDATE rental_requests SET status = ? WHERE id = ?";
        jdbc.update(sql, status, rentalRequestId);
    }

    public int recordPickupConfirmation(Long rentalRequestId, boolean owner, boolean confirmed) {
        String timestampColumn = owner ? "owner_pickup_confirmed_at" : "borrower_pickup_confirmed_at";
        String confirmationColumn = owner ? "owner_pickup_confirmation" : "borrower_pickup_confirmation";
        String sql = "UPDATE rental_requests SET " + timestampColumn + " = NOW(), " + confirmationColumn + " = ? WHERE id = ? AND " + confirmationColumn + " IS NULL";
        return jdbc.update(sql, confirmed ? "YES" : "NO", rentalRequestId);
    }

    public int recordPaymentConfirmation(Long rentalRequestId, boolean owner, boolean confirmed) {
        String timestampColumn = owner ? "owner_payment_confirmed_at" : "borrower_payment_confirmed_at";
        String confirmationColumn = owner ? "owner_payment_confirmation" : "borrower_payment_confirmation";
        String sql = "UPDATE rental_requests SET " + timestampColumn + " = NOW(), " + confirmationColumn + " = ? WHERE id = ? AND " + confirmationColumn + " IS NULL";
        return jdbc.update(sql, confirmed ? "YES" : "NO", rentalRequestId);
    }

    public void startRental(Long rentalRequestId, double totalRent, double advancePaid, double remainingBalance, LocalDateTime startAt, LocalDateTime endAt) {
        String sql = """
                UPDATE rental_requests
                SET status = 'ACTIVE',
                    total_rent = ?,
                    advance_paid = ?,
                    remaining_balance = ?,
                    payment_confirmation_time = NOW(),
                    active_start_date = ?,
                    rental_start_at = ?,
                    rental_end_at = ?
                WHERE id = ?
                """;
        Timestamp startTs = Timestamp.valueOf(startAt);
        jdbc.update(sql, totalRent, advancePaid, remainingBalance, startTs, startTs, Timestamp.valueOf(endAt), rentalRequestId);
    }

    public void confirmReturn(Long rentalRequestId, boolean owner) {
        String column = owner ? "owner_return_confirmed_at" : "borrower_return_confirmed_at";
        String sql = "UPDATE rental_requests SET " + column + " = COALESCE(" + column + ", NOW()) WHERE id = ?";
        jdbc.update(sql, rentalRequestId);
    }

    public void markClosed(Long rentalRequestId) {
        String sql = "UPDATE rental_requests SET status = 'CLOSED' WHERE id = ?";
        jdbc.update(sql, rentalRequestId);
    }

    public void markRejected(Long rentalRequestId) {
        String sql = "UPDATE rental_requests SET status = 'REJECTED' WHERE id = ?";
        jdbc.update(sql, rentalRequestId);
    }

    public void markPickupNotificationSent(Long rentalRequestId) {
        String sql = "UPDATE rental_requests SET pickup_notification_sent = TRUE WHERE id = ?";
        jdbc.update(sql, rentalRequestId);
    }

    public void markEndingReminderSent(Long rentalRequestId) {
        String sql = "UPDATE rental_requests SET ending_reminder_sent = TRUE WHERE id = ?";
        jdbc.update(sql, rentalRequestId);
    }

    public void markReturnConfirmationNotificationSent(Long rentalRequestId) {
        String sql = "UPDATE rental_requests SET return_confirmation_notification_sent = TRUE WHERE id = ?";
        jdbc.update(sql, rentalRequestId);
    }

    public List<RentalRequest> findActiveRentalsDueWithinHours(int hours) {
        String sql = RENTAL_SELECT + """
                WHERE r.status = 'ACTIVE'
                  AND r.rental_end_at IS NOT NULL
                  AND r.rental_end_at > NOW()
                  AND r.rental_end_at <= DATE_ADD(NOW(), INTERVAL ? HOUR)
                """;
        return jdbc.query(sql, new RentalRequestRowMapper(), hours);
    }

    public List<RentalRequest> findActiveRentalsExpiredForDays(int days) {
        String sql = RENTAL_SELECT + """
                WHERE r.status IN ('ACTIVE', 'AWAITING_RETURN')
                  AND r.rental_end_at IS NOT NULL
                  AND r.rental_end_at <= DATE_SUB(NOW(), INTERVAL ? DAY)
                """;
        return jdbc.query(sql, new RentalRequestRowMapper(), days);
    }

    public List<RentalRequest> findPickupRemindersDue(int minutesBefore, String notificationType) {
        String sql = RENTAL_SELECT + """
                WHERE r.status = 'PICKUP_SCHEDULED'
                  AND r.pickup_date IS NOT NULL
                  AND r.pickup_time IS NOT NULL
                  AND TIMESTAMP(r.pickup_date, r.pickup_time) > NOW()
                  AND TIMESTAMP(r.pickup_date, r.pickup_time) <= DATE_ADD(NOW(), INTERVAL ? MINUTE)
                  AND NOT EXISTS (
                      SELECT 1 FROM notifications n
                      WHERE n.rental_request_id = r.id
                        AND n.notification_type = ?
                  )
                """;
        return jdbc.query(sql, new RentalRequestRowMapper(), minutesBefore, notificationType);
    }

    public List<RentalRequest> findPickupsDueNow() {
        String sql = RENTAL_SELECT + """
                WHERE r.status = 'PICKUP_SCHEDULED'
                  AND r.pickup_date IS NOT NULL
                  AND r.pickup_time IS NOT NULL
                  AND TIMESTAMP(r.pickup_date, r.pickup_time) <= DATE_SUB(NOW(), INTERVAL 1 MINUTE)
                  AND COALESCE(r.pickup_notification_sent, FALSE) = FALSE
                """;
        return jdbc.query(sql, new RentalRequestRowMapper());
    }

    public List<RentalRequest> findPaymentConfirmationsWaiting() {
        String sql = RENTAL_SELECT + """
                WHERE r.status IN ('ADVANCE_PAYMENT_CONFIRMATION', 'PAYMENT_PENDING')
                  AND (
                      r.owner_payment_confirmation IS NULL
                      OR r.borrower_payment_confirmation IS NULL
                      OR r.owner_payment_confirmation = 'NO'
                      OR r.borrower_payment_confirmation = 'NO'
                  )
                """;
        return jdbc.query(sql, new RentalRequestRowMapper());
    }

    public List<RentalRequest> findRentalsEndingNow() {
        String sql = RENTAL_SELECT + """
                WHERE r.status = 'ACTIVE'
                  AND r.rental_end_at IS NOT NULL
                  AND r.rental_end_at <= NOW()
                """;
        return jdbc.query(sql, new RentalRequestRowMapper());
    }

    public int countClosedByToolId(Long toolId) {
        String sql = "SELECT COUNT(*) FROM rental_requests WHERE tool_id = ? AND status IN ('COMPLETED', 'CLOSED')";
        Integer count = jdbc.queryForObject(sql, Integer.class, toolId);
        return count == null ? 0 : count;
    }

    public void logAction(Long rentalRequestId, Long userId, String action, String details) {
        String sql = """
                INSERT INTO rental_action_logs (rental_id, user_id, action_performed, details)
                VALUES (?, ?, ?, ?)
                """;
        jdbc.update(sql, rentalRequestId, userId, action, details);
    }

    public void logStatusTransition(Long rentalRequestId, String fromStatus, String toStatus, Long changedBy, String reason) {
        String sql = """
                INSERT INTO rental_status_logs (rental_id, from_status, to_status, changed_by, reason)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbc.update(sql, rentalRequestId, fromStatus, toStatus, changedBy, reason);
    }

    private static class RentalRequestRowMapper implements RowMapper<RentalRequest> {
        @Override
        public RentalRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            RentalRequest request = new RentalRequest();
            request.setRentalRequestId(rs.getLong("id"));
            request.setToolId(rs.getLong("tool_id"));
            request.setBorrowerName(rs.getString("renter_name"));
            request.setRenterId(rs.getString("renter_id"));
            request.setOwnerId(rs.getLong("owner_user_id"));
            request.setBorrowerId(rs.getLong("borrower_user_id"));
            Date startDate = rs.getDate("start_date");
            if (startDate != null) request.setStartDate(startDate.toLocalDate());
            request.setDurationDays(rs.getInt("duration_days"));
            request.setMessage(rs.getString("message"));
            request.setStatus(rs.getString("status"));
            Timestamp activeStartDate = rs.getTimestamp("active_start_date");
            if (activeStartDate != null) request.setActiveStartDate(activeStartDate.toLocalDateTime());
            Date pickupDate = nullableDate(rs, "pickup_date");
            if (pickupDate != null) request.setPickupDate(pickupDate.toLocalDate());
            Time pickupTime = nullableTime(rs, "pickup_time");
            if (pickupTime != null) request.setPickupTime(pickupTime.toLocalTime());
            request.setPickupLocation(nullableString(rs, "scheduled_pickup_location"));
            request.setPickupInstructions(nullableString(rs, "pickup_instructions"));
            request.setOwnerPickupConfirmation(nullableString(rs, "owner_pickup_confirmation"));
            request.setBorrowerPickupConfirmation(nullableString(rs, "borrower_pickup_confirmation"));
            request.setOwnerPaymentConfirmation(nullableString(rs, "owner_payment_confirmation"));
            request.setBorrowerPaymentConfirmation(nullableString(rs, "borrower_payment_confirmation"));
            request.setOwnerPickupConfirmedAt(nullableTimestamp(rs, "owner_pickup_confirmed_at"));
            request.setBorrowerPickupConfirmedAt(nullableTimestamp(rs, "borrower_pickup_confirmed_at"));
            request.setOwnerPaymentConfirmedAt(nullableTimestamp(rs, "owner_payment_confirmed_at"));
            request.setBorrowerPaymentConfirmedAt(nullableTimestamp(rs, "borrower_payment_confirmed_at"));
            request.setTotalRent(nullableDouble(rs, "total_rent"));
            request.setAdvancePaid(nullableDouble(rs, "advance_paid"));
            request.setRemainingBalance(nullableDouble(rs, "remaining_balance"));
            request.setPaymentConfirmationTime(nullableTimestamp(rs, "payment_confirmation_time"));
            request.setRentalStartAt(nullableTimestamp(rs, "rental_start_at"));
            request.setRentalEndAt(nullableTimestamp(rs, "rental_end_at"));
            request.setOwnerReturnConfirmedAt(nullableTimestamp(rs, "owner_return_confirmed_at"));
            request.setBorrowerReturnConfirmedAt(nullableTimestamp(rs, "borrower_return_confirmed_at"));
            request.setPickupNotificationSent(nullableBoolean(rs, "pickup_notification_sent"));
            request.setEndingReminderSent(nullableBoolean(rs, "ending_reminder_sent"));
            request.setReturnConfirmationNotificationSent(nullableBoolean(rs, "return_confirmation_notification_sent"));
            return request;
        }

        private Date nullableDate(ResultSet rs, String column) {
            try { return rs.getDate(column); } catch (SQLException ignored) { return null; }
        }

        private Time nullableTime(ResultSet rs, String column) {
            try { return rs.getTime(column); } catch (SQLException ignored) { return null; }
        }

        private String nullableString(ResultSet rs, String column) {
            try { return rs.getString(column); } catch (SQLException ignored) { return null; }
        }

        private LocalDateTime nullableTimestamp(ResultSet rs, String column) {
            try {
                Timestamp timestamp = rs.getTimestamp(column);
                return timestamp == null ? null : timestamp.toLocalDateTime();
            } catch (SQLException ignored) {
                return null;
            }
        }

        private Double nullableDouble(ResultSet rs, String column) {
            try {
                double value = rs.getDouble(column);
                return rs.wasNull() ? null : value;
            } catch (SQLException ignored) {
                return null;
            }
        }

        private Boolean nullableBoolean(ResultSet rs, String column) {
            try {
                boolean value = rs.getBoolean(column);
                return rs.wasNull() ? null : value;
            } catch (SQLException ignored) {
                return null;
            }
        }
    }
}
