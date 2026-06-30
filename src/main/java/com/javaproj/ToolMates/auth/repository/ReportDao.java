package com.javaproj.ToolMates.auth.repository;

import com.javaproj.ToolMates.auth.dto.ReportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReportDao {

    @Autowired
    private JdbcTemplate jdbc;

    public boolean existsByRentalAndReporter(Long rentalId, Long reporterId) {
        String sql = "SELECT COUNT(*) FROM user_reports WHERE rental_id = ? AND reporter_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, rentalId, reporterId);
        return count != null && count > 0;
    }

    public int countReceivedByUser(Long userId) {
        String sql = "SELECT COUNT(*) FROM user_reports WHERE reported_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId);
        return count == null ? 0 : count;
    }

    public void save(ReportRequest request) {
        String sql = """
                INSERT INTO user_reports
                    (reporter_id, reported_id, rental_id, tool_id, reason_category, additional_details, evidence_url, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'OPEN')
                """;
        jdbc.update(sql,
                request.getReporterId(),
                request.getReportedId(),
                request.getRentalId(),
                request.getToolId(),
                request.getReasonCategory(),
                request.getAdditionalDetails(),
                request.getEvidenceUrl());
    }
}
