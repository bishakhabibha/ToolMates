ALTER TABLE tools
    CHANGE price_per_hr price_per_day DOUBLE NOT NULL;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS suspended_times INT NOT NULL DEFAULT 0;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS avatar_url TEXT NULL;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS bio TEXT NULL;

CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    rental_request_id BIGINT NULL,
    message_text TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL DEFAULT 'general',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_notifications_rental_request FOREIGN KEY (rental_request_id) REFERENCES rental_requests(id)
);

CREATE TABLE IF NOT EXISTS messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rental_request_id BIGINT NULL,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_rental_request FOREIGN KEY (rental_request_id) REFERENCES rental_requests(id),
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(user_id),
    CONSTRAINT fk_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);

ALTER TABLE messages
    MODIFY COLUMN rental_request_id BIGINT NULL;

ALTER TABLE messages
    ADD COLUMN IF NOT EXISTS is_read BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS user_reports (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    reported_id BIGINT NOT NULL,
    rental_id BIGINT NOT NULL,
    reason_category VARCHAR(80) NOT NULL,
    additional_details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id),
    CONSTRAINT fk_user_reports_reported FOREIGN KEY (reported_id) REFERENCES users(user_id),
    CONSTRAINT fk_user_reports_rental FOREIGN KEY (rental_id) REFERENCES rental_requests(id)
);

CREATE TABLE IF NOT EXISTS user_reviews (
    review_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reviewer_id BIGINT NOT NULL,
    reviewed_user_id BIGINT NOT NULL,
    rating_stars INT NOT NULL,
    review_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_reviews_rating CHECK (rating_stars BETWEEN 1 AND 5),
    CONSTRAINT fk_user_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(user_id),
    CONSTRAINT fk_user_reviews_reviewed FOREIGN KEY (reviewed_user_id) REFERENCES users(user_id)
);

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS pickup_date DATE NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS pickup_time TIME NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS scheduled_pickup_location VARCHAR(255) NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS owner_pickup_confirmed_at TIMESTAMP NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS borrower_pickup_confirmed_at TIMESTAMP NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS owner_payment_confirmed_at TIMESTAMP NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS borrower_payment_confirmed_at TIMESTAMP NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS total_rent DOUBLE NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS advance_paid DOUBLE NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS remaining_balance DOUBLE NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS payment_confirmation_time TIMESTAMP NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS rental_start_at TIMESTAMP NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS rental_end_at TIMESTAMP NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS owner_return_confirmed_at TIMESTAMP NULL;

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS borrower_return_confirmed_at TIMESTAMP NULL;

CREATE TABLE IF NOT EXISTS rental_action_logs (
    action_log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rental_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    action_performed VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rental_action_logs_rental FOREIGN KEY (rental_id) REFERENCES rental_requests(id),
    CONSTRAINT fk_rental_action_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS rental_status_logs (
    status_log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rental_id BIGINT NOT NULL,
    from_status VARCHAR(60) NULL,
    to_status VARCHAR(60) NOT NULL,
    changed_by BIGINT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rental_status_logs_rental FOREIGN KEY (rental_id) REFERENCES rental_requests(id),
    CONSTRAINT fk_rental_status_logs_user FOREIGN KEY (changed_by) REFERENCES users(user_id)
);

ALTER TABLE user_reports
    ADD COLUMN IF NOT EXISTS tool_id BIGINT NULL;

ALTER TABLE user_reports
    ADD COLUMN IF NOT EXISTS evidence_url TEXT NULL;

ALTER TABLE user_reports
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'OPEN';

CREATE TABLE IF NOT EXISTS moderation_history (
    moderation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    admin_id BIGINT NULL,
    action VARCHAR(80) NOT NULL,
    details TEXT,
    suspension_days INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_moderation_history_report FOREIGN KEY (report_id) REFERENCES user_reports(report_id),
    CONSTRAINT fk_moderation_history_admin FOREIGN KEY (admin_id) REFERENCES users(user_id)
);
