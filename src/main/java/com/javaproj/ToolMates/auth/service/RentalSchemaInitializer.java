package com.javaproj.ToolMates.auth.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RentalSchemaInitializer {

    @Autowired
    private JdbcTemplate jdbc;

    @PostConstruct
    public void ensureRentalWorkflowColumns() {
        addColumnIfMissing("tools", "is_active", "BOOLEAN NOT NULL DEFAULT TRUE");
        addColumnIfMissing("users", "bio", "TEXT NULL");

        addColumnIfMissing("pickup_instructions", "TEXT NULL");
        addColumnIfMissing("owner_pickup_confirmation", "VARCHAR(3) NULL");
        addColumnIfMissing("borrower_pickup_confirmation", "VARCHAR(3) NULL");
        addColumnIfMissing("owner_payment_confirmation", "VARCHAR(3) NULL");
        addColumnIfMissing("borrower_payment_confirmation", "VARCHAR(3) NULL");
        addColumnIfMissing("pickup_notification_sent", "BOOLEAN NOT NULL DEFAULT FALSE");
        addColumnIfMissing("ending_reminder_sent", "BOOLEAN NOT NULL DEFAULT FALSE");
        addColumnIfMissing("return_confirmation_notification_sent", "BOOLEAN NOT NULL DEFAULT FALSE");

        addIndexIfMissing(
                "idx_rental_requests_pickup_scheduler",
                "(status, pickup_date, pickup_time, pickup_notification_sent)"
        );
        addIndexIfMissing(
                "idx_rental_requests_payment_scheduler",
                "(status, owner_payment_confirmation, borrower_payment_confirmation)"
        );
    }

    private void addColumnIfMissing(String columnName, String columnDefinition) {
        addColumnIfMissing("rental_requests", columnName, columnDefinition);
    }

    private void addColumnIfMissing(String tableName, String columnName, String columnDefinition) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """,
                Integer.class,
                tableName,
                columnName
        );
        if (count != null && count > 0) return;
        jdbc.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
    }

    private void addIndexIfMissing(String indexName, String indexDefinition) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = 'rental_requests'
                  AND index_name = ?
                """,
                Integer.class,
                indexName
        );
        if (count != null && count > 0) return;
        jdbc.execute("CREATE INDEX " + indexName + " ON rental_requests " + indexDefinition);
    }
}
