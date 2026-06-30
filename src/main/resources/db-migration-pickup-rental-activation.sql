USE toolmates_db;

DELIMITER $$

DROP PROCEDURE IF EXISTS add_rental_column_if_missing $$
CREATE PROCEDURE add_rental_column_if_missing(
    IN column_name_to_add VARCHAR(64),
    IN column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'rental_requests'
          AND column_name = column_name_to_add
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE rental_requests ADD COLUMN ', column_name_to_add, ' ', column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DROP PROCEDURE IF EXISTS add_rental_index_if_missing $$
CREATE PROCEDURE add_rental_index_if_missing(
    IN index_name_to_add VARCHAR(64),
    IN index_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'rental_requests'
          AND index_name = index_name_to_add
    ) THEN
        SET @ddl = CONCAT('CREATE INDEX ', index_name_to_add, ' ON rental_requests ', index_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DELIMITER ;

CALL add_rental_column_if_missing('pickup_instructions', 'TEXT NULL');
CALL add_rental_column_if_missing('owner_pickup_confirmation', 'VARCHAR(3) NULL');
CALL add_rental_column_if_missing('borrower_pickup_confirmation', 'VARCHAR(3) NULL');
CALL add_rental_column_if_missing('owner_payment_confirmation', 'VARCHAR(3) NULL');
CALL add_rental_column_if_missing('borrower_payment_confirmation', 'VARCHAR(3) NULL');
CALL add_rental_column_if_missing('pickup_notification_sent', 'BOOLEAN NOT NULL DEFAULT FALSE');
CALL add_rental_column_if_missing('ending_reminder_sent', 'BOOLEAN NOT NULL DEFAULT FALSE');
CALL add_rental_column_if_missing('return_confirmation_notification_sent', 'BOOLEAN NOT NULL DEFAULT FALSE');

CALL add_rental_index_if_missing('idx_rental_requests_pickup_scheduler', '(status, pickup_date, pickup_time, pickup_notification_sent)');
CALL add_rental_index_if_missing('idx_rental_requests_payment_scheduler', '(status, owner_payment_confirmation, borrower_payment_confirmation)');

DROP PROCEDURE IF EXISTS add_rental_column_if_missing;
DROP PROCEDURE IF EXISTS add_rental_index_if_missing;
