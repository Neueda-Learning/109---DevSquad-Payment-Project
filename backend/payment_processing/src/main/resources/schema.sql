CREATE DATABASE IF NOT EXISTS payment_processing;
USE payment_processing;

CREATE TABLE IF NOT EXISTS Users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    mobile VARCHAR(15) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Accounts (
    account_number BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    account_type ENUM('SAVINGS','CURRENT','SALARY','FIXED_DEPOSIT') NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    ifsc VARCHAR(20) NOT NULL,
    bank_address VARCHAR(255),
    country VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    not_active_reason VARCHAR(255),

    CONSTRAINT fk_account_user
        FOREIGN KEY (user_id)
        REFERENCES Users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS UPI (
    upi_id VARCHAR(100) PRIMARY KEY,
    account_number BIGINT NOT NULL,
    upi_name VARCHAR(100) NOT NULL,

    CONSTRAINT fk_upi_account
        FOREIGN KEY (account_number)
        REFERENCES Accounts(account_number)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS CreditCards (
    card_number VARCHAR(20) PRIMARY KEY,
    bank VARCHAR(100) NOT NULL,
    cvv CHAR(3) NOT NULL,
    expiry_date DATE NOT NULL,
    holder_name VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,

    CONSTRAINT fk_creditcard_user
        FOREIGN KEY (user_id)
        REFERENCES Users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PaymentMethods (
    payment_method_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    method_type ENUM('UPI','CREDIT_CARD','BANK_TRANSFER') NOT NULL,

    upi_id VARCHAR(100),
    card_number VARCHAR(20),
    account_number BIGINT,

    CONSTRAINT fk_pm_upi
        FOREIGN KEY (upi_id)
        REFERENCES UPI(upi_id),

    CONSTRAINT fk_pm_card
        FOREIGN KEY (card_number)
        REFERENCES CreditCards(card_number),

    CONSTRAINT fk_pm_account
        FOREIGN KEY (account_number)
        REFERENCES Accounts(account_number)
);

CREATE TABLE IF NOT EXISTS Schedules (
    schedule_id        BIGINT AUTO_INCREMENT PRIMARY KEY,

    sender_account_number   BIGINT NOT NULL,
    receiver_account_number BIGINT NOT NULL,

    amount             DECIMAL(15,2) NOT NULL,
    currency_id        INT,

    payment_method_id  BIGINT NOT NULL,

    description        VARCHAR(500),

    frequency          ENUM('DAILY','WEEKLY','MONTHLY','YEARLY') NOT NULL,

    start_date         DATE NOT NULL,
    end_date           DATE,
    next_run_date      DATE,
    last_run_date      DATE,

    status             ENUM('ACTIVE','PAUSED','COMPLETED','CANCELLED')
                           NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_schedule_sender
        FOREIGN KEY (sender_account_number)
        REFERENCES Accounts(account_number),

    CONSTRAINT fk_schedule_receiver
        FOREIGN KEY (receiver_account_number)
        REFERENCES Accounts(account_number),

    CONSTRAINT fk_schedule_method
        FOREIGN KEY (payment_method_id)
        REFERENCES PaymentMethods(payment_method_id)
);

CREATE TABLE IF NOT EXISTS Payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    payment_invoice_number VARCHAR(50) NOT NULL UNIQUE,

    sender_account_number BIGINT NULL,
    receiver_account_number BIGINT NULL,

    amount DECIMAL(15,2) NOT NULL,

    currency_id INT,

    payment_date DATE NOT NULL,
    payment_time TIME NOT NULL,

    status ENUM('CREATED','VALIDATED','SENT','FAILED','COMPLETED')
        NOT NULL DEFAULT 'CREATED',

    description VARCHAR(500),

    payment_mode ENUM('UPI','CREDIT_CARD','BANK_TRANSFER')
        NOT NULL,

    schedule_id BIGINT,

    batch_id VARCHAR(50),

    payment_method_id BIGINT NOT NULL,

    payment_log VARCHAR(500),

    CONSTRAINT fk_payment_sender
        FOREIGN KEY (sender_account_number)
        REFERENCES Accounts(account_number),


    CONSTRAINT fk_payment_method
        FOREIGN KEY (payment_method_id)
        REFERENCES PaymentMethods(payment_method_id),

    CONSTRAINT fk_payment_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES Schedules(schedule_id)
        ON DELETE SET NULL,

    INDEX idx_batch_id (batch_id)
);

CREATE TABLE IF NOT EXISTS tags (
    tag_id      INT AUTO_INCREMENT PRIMARY KEY,
    tag_name    VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Make account numbers nullable to allow failed payments to be inserted
ALTER TABLE Payments MODIFY COLUMN sender_account_number BIGINT NULL;
ALTER TABLE Payments MODIFY COLUMN receiver_account_number BIGINT NULL;

-- Safely drop fk_payment_receiver if it still exists (idempotent - safe to run on every startup)
-- Receiver account is validated manually in application code instead of via FK constraint,
-- so payments can be inserted with FAILED status even when receiver account does not exist.
SET @fk_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'Payments'
      AND CONSTRAINT_NAME = 'fk_payment_receiver'
);
SET @drop_fk_sql = IF(@fk_exists > 0,
    'ALTER TABLE Payments DROP FOREIGN KEY fk_payment_receiver',
    'SELECT 1');
PREPARE stmt FROM @drop_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS payment_tags (
  payment_id BIGINT NOT NULL,
  tag_id INT NOT NULL,
  PRIMARY KEY (payment_id, tag_id),
  CONSTRAINT fk_payment_tags_payment
    FOREIGN KEY (payment_id) REFERENCES Payments(payment_id) ON DELETE CASCADE,
  CONSTRAINT fk_payment_tags_tag
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
);

ALTER TABLE Payments ADD COLUMN payment_log VARCHAR(500);

