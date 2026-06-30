CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(60) NOT NULL,
    last_name VARCHAR(60) NOT NULL,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    department VARCHAR(100),
    student_email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_tools_rented INT NOT NULL DEFAULT 0,
    total_tools_received INT NOT NULL DEFAULT 0,
    suspended_times INT NOT NULL DEFAULT 0,
    avatar_url TEXT NULL,
    bio TEXT NULL
);

CREATE TABLE IF NOT EXISTS tools (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_name VARCHAR(140) NOT NULL,
    owner_id VARCHAR(20) NOT NULL,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    tool_condition VARCHAR(80) NOT NULL,
    price_per_day DOUBLE NOT NULL,
    max_renting_period INT NOT NULL,
    pickup_location VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    additional_info TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tools_owner_id (owner_id),
    CONSTRAINT fk_tools_owner_student FOREIGN KEY (owner_id) REFERENCES users(student_id)
);

CREATE TABLE IF NOT EXISTS tool_images (
    image_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    image_url TEXT NOT NULL,
    CONSTRAINT fk_tool_images_tool FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS rental_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    renter_name VARCHAR(140) NOT NULL,
    renter_id VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    duration_days INT NOT NULL,
    message TEXT,
    status VARCHAR(60) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active_start_date TIMESTAMP NULL,
    pickup_date DATE NULL,
    pickup_time TIME NULL,
    scheduled_pickup_location VARCHAR(255) NULL,
    pickup_instructions TEXT NULL,
    owner_pickup_confirmed_at TIMESTAMP NULL,
    borrower_pickup_confirmed_at TIMESTAMP NULL,
    owner_payment_confirmed_at TIMESTAMP NULL,
    borrower_payment_confirmed_at TIMESTAMP NULL,
    owner_pickup_confirmation VARCHAR(3) NULL,
    borrower_pickup_confirmation VARCHAR(3) NULL,
    owner_payment_confirmation VARCHAR(3) NULL,
    borrower_payment_confirmation VARCHAR(3) NULL,
    total_rent DOUBLE NULL,
    advance_paid DOUBLE NULL,
    remaining_balance DOUBLE NULL,
    payment_confirmation_time TIMESTAMP NULL,
    rental_start_at TIMESTAMP NULL,
    rental_end_at TIMESTAMP NULL,
    owner_return_confirmed_at TIMESTAMP NULL,
    borrower_return_confirmed_at TIMESTAMP NULL,
    pickup_notification_sent BOOLEAN NOT NULL DEFAULT FALSE,
    ending_reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
    return_confirmation_notification_sent BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_rental_requests_tool_id (tool_id),
    INDEX idx_rental_requests_renter_id (renter_id),
    INDEX idx_rental_requests_pickup_scheduler (status, pickup_date, pickup_time, pickup_notification_sent),
    INDEX idx_rental_requests_payment_scheduler (status, owner_payment_confirmation, borrower_payment_confirmation),
    CONSTRAINT fk_rental_requests_tool FOREIGN KEY (tool_id) REFERENCES tools(id),
    CONSTRAINT fk_rental_requests_renter FOREIGN KEY (renter_id) REFERENCES users(student_id)
);

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

CREATE TABLE IF NOT EXISTS user_reports (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    reported_id BIGINT NOT NULL,
    rental_id BIGINT NOT NULL,
    tool_id BIGINT NULL,
    reason_category VARCHAR(80) NOT NULL,
    additional_details TEXT,
    evidence_url TEXT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
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
