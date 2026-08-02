-- =====================================================
-- PAYMENT PROCESSING SYSTEM DATABASE SCHEMA
-- =====================================================

CREATE DATABASE IF NOT EXISTS payment_processing;
USE payment_processing;

-- =====================================================
-- USERS
-- =====================================================

CREATE TABLE Users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    mobile VARCHAR(15) NOT NULL UNIQUE
);

-- =====================================================
-- ACCOUNTS
-- =====================================================

CREATE TABLE Accounts (
    account_number BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    bank_name VARCHAR(100) NOT NULL,

    account_type ENUM(
        'SAVINGS',
        'CURRENT',
        'SALARY',
        'FIXED_DEPOSIT'
    ) NOT NULL,

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

-- =====================================================
-- UPI
-- =====================================================

CREATE TABLE UPI (
    upi_id VARCHAR(100) PRIMARY KEY,
    account_number BIGINT NOT NULL,
    upi_name VARCHAR(100) NOT NULL,

    CONSTRAINT fk_upi_account
        FOREIGN KEY (account_number)
        REFERENCES Accounts(account_number)
        ON DELETE CASCADE
);

-- =====================================================
-- CREDIT CARDS
-- =====================================================

CREATE TABLE CreditCards (
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

-- =====================================================
-- PAYMENT METHODS
-- Used to identify which payment instrument
-- (UPI, Credit Card, Bank Account) was used
-- =====================================================

CREATE TABLE PaymentMethods (
    payment_method_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    method_type ENUM(
        'UPI',
        'CREDIT_CARD',
        'BANK_TRANSFER'
    ) NOT NULL,

    upi_id VARCHAR(100) NULL,
    card_number VARCHAR(20) NULL,
    account_number BIGINT NULL,

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

-- =====================================================
-- PAYMENTS
-- =====================================================

CREATE TABLE Payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    payment_invoice_number VARCHAR(50) NOT NULL UNIQUE,

    sender_account_number BIGINT NOT NULL,
    receiver_account_number BIGINT NOT NULL,

    amount DECIMAL(15,2) NOT NULL,

    payment_date DATE NOT NULL,
    payment_time TIME NOT NULL,

    status ENUM(
        'CREATED',
        'VALIDATED',
        'SENT',
        'FAILED',
        'COMPLETED'
    ) NOT NULL DEFAULT 'CREATED',

    description VARCHAR(500),

    payment_mode ENUM(
        'UPI',
        'CREDIT_CARD',
        'BANK_TRANSFER'
    ) NOT NULL,

    is_scheduled_payment BOOLEAN NOT NULL DEFAULT FALSE,

    schedule_period VARCHAR(50),

    payment_method_id BIGINT NOT NULL,

    CONSTRAINT fk_payment_sender
        FOREIGN KEY (sender_account_number)
        REFERENCES Accounts(account_number),

    CONSTRAINT fk_payment_receiver
        FOREIGN KEY (receiver_account_number)
        REFERENCES Accounts(account_number),

    CONSTRAINT fk_payment_method
        FOREIGN KEY (payment_method_id)
        REFERENCES PaymentMethods(payment_method_id)
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

CREATE INDEX idx_user_mobile
ON Users(mobile);

CREATE INDEX idx_account_user
ON Accounts(user_id);

CREATE INDEX idx_upi_account
ON UPI(account_number);

CREATE INDEX idx_card_user
ON CreditCards(user_id);

CREATE INDEX idx_payment_sender
ON Payments(sender_account_number);

CREATE INDEX idx_payment_receiver
ON Payments(receiver_account_number);

CREATE INDEX idx_payment_status
ON Payments(status);

CREATE INDEX idx_payment_date
ON Payments(payment_date);

CREATE INDEX idx_payment_method
ON Payments(payment_method_id);