package com.javaproj.ToolMates.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RentalHistoryService {

    @Autowired
    private JdbcTemplate jdbc;

    private static final List<String> ACTIVE_STATUSES = List.of(
            "PENDING", "OWNER_ACCEPTED", "PICKUP_DETAILS_SUBMITTED", "BORROWER_REVIEWING_PICKUP_DETAILS",
            "PICKUP_SCHEDULED", "WAITING_FOR_PICKUP", "WAITING_FOR_PICKUP_TIME", "WAITING_FOR_PICKUP_CONFIRMATION",
            "PICKUP_CONFIRMATION", "ADVANCE_PAYMENT_CONFIRMATION", "ACTIVE", "AWAITING_RETURN",
            "PAYMENT_PENDING", "PAYMENT_DISPUTE", "PICKUP_DISPUTE", "RETURN_CONFIRMATION", "RETURN_DISPUTE"
    );

    private static final List<String> REPORTED_STATUSES = List.of("REPORTED", "PICKUP_DISPUTE", "PAYMENT_DISPUTE", "RETURN_DISPUTE");
    private static final List<String> COMPLETED_STATUSES = List.of("COMPLETED", "CLOSED");
    private static final List<String> CANCELLED_STATUSES = List.of("CANCELLED", "REJECTED");
    private static final List<String> FINANCIALLY_CLOSED_STATUSES = List.of("COMPLETED", "CLOSED", "CANCELLED", "REJECTED", "REPORTED");
    private static final List<String> ACCEPTED_HISTORY_STATUSES = List.of(
            "OWNER_ACCEPTED", "PICKUP_DETAILS_SUBMITTED", "BORROWER_REVIEWING_PICKUP_DETAILS",
            "PICKUP_SCHEDULED", "WAITING_FOR_PICKUP", "WAITING_FOR_PICKUP_TIME", "WAITING_FOR_PICKUP_CONFIRMATION",
            "PICKUP_CONFIRMATION", "ADVANCE_PAYMENT_CONFIRMATION", "ACTIVE", "AWAITING_RETURN",
            "PAYMENT_PENDING", "PAYMENT_DISPUTE", "PICKUP_DISPUTE", "PICKUP_FAILED",
            "RETURN_CONFIRMATION", "RETURN_DISPUTE", "COMPLETED", "CLOSED", "REPORTED", "CANCELLED"
    );

    public Map<String, Object> findBorrowed(Long userId, String search, String filter, String sort, int page, int size) {
        return findRentals(userId, false, search, filter, sort, page, size);
    }

    public Map<String, Object> findLent(Long userId, String search, String filter, String sort, int page, int size) {
        return findRentals(userId, true, search, filter, sort, page, size);
    }

    public Map<String, Object> findDetails(Long userId, Long rentalId) {
        List<Map<String, Object>> rows = jdbc.queryForList(baseRentalSelect() + """
                WHERE r.id = ?
                  AND (owner.user_id = ? OR borrower.user_id = ?)
                  AND r.status <> 'PENDING'
                  AND r.status <> 'REJECTED'
                """, rentalId, userId, userId);
        if (rows.isEmpty()) throw new IllegalArgumentException("Rental not found.");
        Map<String, Object> rental = decorateRental(rows.get(0));
        rental.put("pastRentals", findAcceptedRentalsForSameTool(userId, rental));
        return rental;
    }

    public List<Map<String, Object>> findTimeline(Long userId, Long rentalId) {
        assertCanAccess(userId, rentalId);
        String sql = """
                SELECT 'STATUS' AS timeline_type,
                       status_log_id AS timeline_id,
                       COALESCE(reason, CONCAT('Status changed to ', to_status)) AS action,
                       from_status AS from_status,
                       to_status AS status,
                       changed_by AS user_id,
                       created_at AS created_at
                FROM rental_status_logs
                WHERE rental_id = ?
                UNION ALL
                SELECT 'ACTION' AS timeline_type,
                       action_log_id AS timeline_id,
                       action_performed AS action,
                       NULL AS from_status,
                       details AS status,
                       user_id AS user_id,
                       created_at AS created_at
                FROM rental_action_logs
                WHERE rental_id = ?
                ORDER BY created_at ASC, timeline_id ASC
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, rentalId, rentalId);
        List<Map<String, Object>> timeline = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", row.get("timeline_type"));
            item.put("action", humanize(row.get("action")));
            item.put("fromStatus", humanize(row.get("from_status")));
            item.put("status", humanize(row.get("status")));
            item.put("userId", row.get("user_id"));
            item.put("createdAt", stringifyDateTime(row.get("created_at")));
            timeline.add(item);
        }
        return timeline;
    }

    public Map<String, Object> calculateProgress(Long userId, Long rentalId) {
        Map<String, Object> rental = findDetails(userId, rentalId);
        return progressFrom(rental.get("rentalStartAt"), rental.get("rentalEndAt"));
    }

    private Map<String, Object> findRentals(Long userId, boolean lent, String search, String filter, String sort, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 50));
        int offset = safePage * safeSize;
        String normalizedFilter = filter == null ? "all" : filter.toLowerCase(Locale.ROOT);
        String normalizedSort = "oldest".equalsIgnoreCase(sort) ? "ASC" : "DESC";

        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(lent ? " WHERE ownerUserId = ? " : " WHERE borrowerUserId = ? ");
        args.add(userId);
        appendStatusIn(where, ACCEPTED_HISTORY_STATUSES);

        if (search != null && !search.isBlank()) {
            where.append(" AND (LOWER(toolName) LIKE ? OR LOWER(owner_name_full) LIKE ? OR LOWER(borrower_name_full) LIKE ?) ");
            String like = "%" + search.toLowerCase(Locale.ROOT).trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }

        appendFilter(where, normalizedFilter);

        String sql = baseRentalSelectWithNames() + where + " ORDER BY COALESCE(rentalStartAt, createdAt) " + normalizedSort + ", rentalRequestId " + normalizedSort;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args.toArray());

        List<Map<String, Object>> rentals = groupRentalsByTool(rows);
        int fromIndex = Math.min(offset, rentals.size());
        int toIndex = Math.min(fromIndex + safeSize, rentals.size());
        List<Map<String, Object>> pageItems = rentals.subList(fromIndex, toIndex);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", pageItems);
        result.put("page", safePage);
        result.put("size", safeSize);
        result.put("hasMore", toIndex < rentals.size());
        return result;
    }

    private String baseRentalSelectWithNames() {
        return """
                SELECT * FROM (
                    SELECT
                        r.id AS rentalRequestId,
                        r.status AS status,
                        r.start_date AS requestedStartDate,
                        r.created_at AS createdAt,
                        r.duration_days AS durationDays,
                        r.pickup_date AS pickupDate,
                        r.pickup_time AS pickupTime,
                        r.scheduled_pickup_location AS scheduledPickupLocation,
                        r.pickup_instructions AS pickupInstructions,
                        r.total_rent AS totalRent,
                        r.advance_paid AS advancePaid,
                        r.remaining_balance AS remainingBalance,
                        r.owner_payment_confirmation AS ownerPaymentConfirmation,
                        r.borrower_payment_confirmation AS borrowerPaymentConfirmation,
                        r.owner_return_confirmed_at AS ownerReturnConfirmedAt,
                        r.borrower_return_confirmed_at AS borrowerReturnConfirmedAt,
                        r.rental_start_at AS rentalStartAt,
                        r.rental_end_at AS rentalEndAt,
                        t.id AS toolId,
                        t.name AS toolName,
                        t.category AS category,
                        t.price_per_day AS pricePerDay,
                        (SELECT ti.image_url FROM tool_images ti WHERE ti.tool_id = t.id LIMIT 1) AS toolImage,
                        owner.user_id AS ownerUserId,
                        owner.student_id AS ownerStudentId,
                        CONCAT(owner.first_name, ' ', owner.last_name) AS ownerName,
                        LOWER(CONCAT(owner.first_name, ' ', owner.last_name)) AS owner_name_full,
                        borrower.user_id AS borrowerUserId,
                        borrower.student_id AS borrowerStudentId,
                        CONCAT(borrower.first_name, ' ', borrower.last_name) AS borrowerName,
                        LOWER(CONCAT(borrower.first_name, ' ', borrower.last_name)) AS borrower_name_full
                    FROM rental_requests r
                    JOIN tools t ON t.id = r.tool_id
                    JOIN users owner ON owner.student_id = t.owner_id
                    JOIN users borrower ON borrower.student_id = r.renter_id
                ) rental_view
                """;
    }

    private String baseRentalSelect() {
        return """
                SELECT
                    r.id AS rentalRequestId,
                    r.status AS status,
                    r.start_date AS requestedStartDate,
                    r.created_at AS createdAt,
                    r.duration_days AS durationDays,
                    r.pickup_date AS pickupDate,
                    r.pickup_time AS pickupTime,
                    r.scheduled_pickup_location AS scheduledPickupLocation,
                    r.pickup_instructions AS pickupInstructions,
                    r.total_rent AS totalRent,
                    r.advance_paid AS advancePaid,
                    r.remaining_balance AS remainingBalance,
                    r.owner_payment_confirmation AS ownerPaymentConfirmation,
                    r.borrower_payment_confirmation AS borrowerPaymentConfirmation,
                    r.owner_return_confirmed_at AS ownerReturnConfirmedAt,
                    r.borrower_return_confirmed_at AS borrowerReturnConfirmedAt,
                    r.rental_start_at AS rentalStartAt,
                    r.rental_end_at AS rentalEndAt,
                    t.id AS toolId,
                    t.name AS toolName,
                    t.category AS category,
                    t.price_per_day AS pricePerDay,
                    t.description AS toolDescription,
                    (SELECT ti.image_url FROM tool_images ti WHERE ti.tool_id = t.id LIMIT 1) AS toolImage,
                    owner.user_id AS ownerUserId,
                    owner.student_id AS ownerStudentId,
                    CONCAT(owner.first_name, ' ', owner.last_name) AS ownerName,
                    owner.phone AS ownerPhone,
                    owner.student_email AS ownerEmail,
                    borrower.user_id AS borrowerUserId,
                    borrower.student_id AS borrowerStudentId,
                    CONCAT(borrower.first_name, ' ', borrower.last_name) AS borrowerName,
                    borrower.phone AS borrowerPhone,
                    borrower.student_email AS borrowerEmail
                FROM rental_requests r
                JOIN tools t ON t.id = r.tool_id
                JOIN users owner ON owner.student_id = t.owner_id
                JOIN users borrower ON borrower.student_id = r.renter_id
                """;
    }

    private void appendFilter(StringBuilder where, String filter) {
        if ("active".equals(filter)) {
            appendStatusIn(where, ACTIVE_STATUSES);
        } else if ("completed".equals(filter)) {
            where.append(" AND status IN ('COMPLETED', 'CLOSED') ");
        } else if ("reported".equals(filter)) {
            appendStatusIn(where, REPORTED_STATUSES);
        } else if ("cancelled".equals(filter)) {
            where.append(" AND status IN ('CANCELLED', 'REJECTED') ");
        }
    }

    private void appendStatusIn(StringBuilder where, List<String> statuses) {
        where.append(" AND status IN (");
        for (int i = 0; i < statuses.size(); i++) {
            if (i > 0) where.append(", ");
            where.append("'").append(statuses.get(i)).append("'");
        }
        where.append(") ");
    }

    private void assertCanAccess(Long userId, Long rentalId) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM rental_requests r
                JOIN tools t ON t.id = r.tool_id
                JOIN users owner ON owner.student_id = t.owner_id
                JOIN users borrower ON borrower.student_id = r.renter_id
                WHERE r.id = ?
                  AND (owner.user_id = ? OR borrower.user_id = ?)
                  AND r.status <> 'PENDING'
                  AND r.status <> 'REJECTED'
                """, Integer.class, rentalId, userId, userId);
        if (count == null || count == 0) throw new IllegalArgumentException("Rental not found.");
    }

    private List<Map<String, Object>> groupRentalsByTool(List<Map<String, Object>> rows) {
        Map<String, List<Map<String, Object>>> byTool = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String toolKey = String.valueOf(row.get("toolId"));
            byTool.computeIfAbsent(toolKey, ignored -> new ArrayList<>()).add(row);
        }

        List<Map<String, Object>> grouped = new ArrayList<>();
        for (List<Map<String, Object>> toolRows : byTool.values()) {
            Map<String, Object> representative = decorateRental(toolRows.get(0));
            representative.put("rentalCount", toolRows.size());
            representative.put("acceptedRentalIds", toolRows.stream()
                    .map(row -> stringifyValue(row.get("rentalRequestId")))
                    .toList());
            grouped.add(representative);
        }
        return grouped;
    }

    private List<Map<String, Object>> findAcceptedRentalsForSameTool(Long userId, Map<String, Object> rental) {
        Long toolId = longFrom(rental.get("toolId"));
        boolean ownerView = userId.equals(longFrom(rental.get("ownerUserId")));
        String participantClause = ownerView ? "owner.user_id = ?" : "borrower.user_id = ?";
        StringBuilder where = new StringBuilder(" WHERE r.tool_id = ? AND ");
        where.append(participantClause);
        appendStatusIn(where, ACCEPTED_HISTORY_STATUSES);
        String sql = baseRentalSelect() + where + " ORDER BY COALESCE(r.rental_start_at, r.created_at) DESC, r.id DESC";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, toolId, userId);
        List<Map<String, Object>> rentals = new ArrayList<>();
        for (Map<String, Object> row : rows) rentals.add(decorateRental(row));
        return rentals;
    }

    private Map<String, Object> decorateRental(Map<String, Object> row) {
        Map<String, Object> rental = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("_full")) continue;
            rental.put(key, stringifyValue(entry.getValue()));
        }
        String status = String.valueOf(row.getOrDefault("status", ""));
        applyMoneyFallbacks(rental);
        applyPaymentDisplayStatus(rental, status);
        rental.put("statusLabel", humanize(status));
        rental.put("group", groupFor(status));
        rental.put("progress", progressFrom(row.get("rentalStartAt"), row.get("rentalEndAt")));
        return rental;
    }

    private void applyMoneyFallbacks(Map<String, Object> rental) {
        double totalRent = numberFrom(rental.get("totalRent"));
        double advancePaid = numberFrom(rental.get("advancePaid"));
        double remainingBalance = numberFrom(rental.get("remainingBalance"));
        double pricePerDay = numberFrom(rental.get("pricePerDay"));
        int durationDays = (int) Math.round(numberFrom(rental.get("durationDays")));
        boolean advanceConfirmed = "YES".equalsIgnoreCase(String.valueOf(rental.get("ownerPaymentConfirmation")))
                && "YES".equalsIgnoreCase(String.valueOf(rental.get("borrowerPaymentConfirmation")));

        if (totalRent <= 0 && pricePerDay > 0 && durationDays > 0) {
            totalRent = roundMoney(pricePerDay * durationDays);
            rental.put("totalRent", totalRent);
        }
        if (advanceConfirmed && advancePaid <= 0 && totalRent > 0) {
            advancePaid = roundMoney(totalRent * 0.40);
            rental.put("advancePaid", advancePaid);
        }
        if (!advanceConfirmed) {
            advancePaid = 0;
            remainingBalance = totalRent;
            rental.put("advancePaid", advancePaid);
            rental.put("remainingBalance", roundMoney(remainingBalance));
        } else if (remainingBalance <= 0 && totalRent > 0) {
            remainingBalance = roundMoney(totalRent - advancePaid);
            rental.put("remainingBalance", remainingBalance);
        }
    }

    private void applyPaymentDisplayStatus(Map<String, Object> rental, String status) {
        String upper = status == null ? "" : status.toUpperCase(Locale.ROOT);
        if (COMPLETED_STATUSES.contains(upper)) {
            rental.put("remainingBalance", 0);
        } else if (CANCELLED_STATUSES.contains(upper)) {
            rental.put("advancePaid", 0);
            rental.put("remainingBalance", 0);
        } else if (FINANCIALLY_CLOSED_STATUSES.contains(upper)) {
            rental.put("remainingBalance", 0);
        }
    }

    private Map<String, Object> progressFrom(Object startValue, Object endValue) {
        LocalDateTime start = toLocalDateTime(startValue);
        LocalDateTime end = toLocalDateTime(endValue);
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> progress = new LinkedHashMap<>();
        if (start == null || end == null || !end.isAfter(start)) {
            progress.put("percentage", 0);
            progress.put("remainingText", "Not started");
            progress.put("elapsedText", "0 minutes");
            progress.put("overdueText", "");
            progress.put("overdue", false);
            return progress;
        }
        long totalSeconds = Math.max(1, Duration.between(start, end).getSeconds());
        long elapsedSeconds = Math.max(0, Duration.between(start, now).getSeconds());
        int percentage = (int) Math.min(100, Math.round((elapsedSeconds * 100.0) / totalSeconds));
        boolean overdue = now.isAfter(end);
        progress.put("percentage", percentage);
        progress.put("remainingText", overdue ? "Overdue by " + humanDuration(Duration.between(end, now)) : humanDuration(Duration.between(now, end)) + " Left");
        progress.put("elapsedText", humanDuration(Duration.between(start, now)));
        progress.put("overdueText", overdue ? humanDuration(Duration.between(end, now)) : "");
        progress.put("overdue", overdue);
        return progress;
    }

    private String groupFor(String status) {
        String upper = status == null ? "" : status.toUpperCase(Locale.ROOT);
        if (COMPLETED_STATUSES.contains(upper)) return "completed";
        if (REPORTED_STATUSES.contains(upper)) return "reported";
        if (CANCELLED_STATUSES.contains(upper)) return "cancelled";
        return "active";
    }

    private String humanize(Object value) {
        if (value == null) return "";
        String text = String.valueOf(value).replace('_', ' ').trim().toLowerCase(Locale.ROOT);
        if (text.isBlank()) return "";
        StringBuilder out = new StringBuilder();
        for (String part : text.split(" ")) {
            if (part.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    private Object stringifyValue(Object value) {
        if (value instanceof Timestamp || value instanceof Date || value instanceof Time) return value.toString();
        return value;
    }

    private String stringifyDateTime(Object value) {
        Object stringValue = stringifyValue(value);
        return stringValue == null ? "" : String.valueOf(stringValue);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime();
        if (value instanceof LocalDateTime localDateTime) return localDateTime;
        if (value == null) return null;
        try { return Timestamp.valueOf(String.valueOf(value)).toLocalDateTime(); }
        catch (Exception ignored) { return null; }
    }

    private double numberFrom(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        if (value == null) return 0;
        try { return Double.parseDouble(String.valueOf(value)); }
        catch (Exception ignored) { return 0; }
    }

    private Long longFrom(Object value) {
        if (value instanceof Number number) return number.longValue();
        if (value == null) return null;
        try { return Long.parseLong(String.valueOf(value)); }
        catch (Exception ignored) { return null; }
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String humanDuration(Duration duration) {
        long minutes = Math.max(0, Math.abs(duration.toMinutes()));
        long days = minutes / (24 * 60);
        long hours = (minutes % (24 * 60)) / 60;
        long mins = minutes % 60;
        if (days > 0) return days + (days == 1 ? " Day" : " Days");
        if (hours > 0) return hours + (hours == 1 ? " Hour" : " Hours");
        return Math.max(1, mins) + (mins == 1 ? " Minute" : " Minutes");
    }
}
