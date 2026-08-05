CREATE DATABASE IF NOT EXISTS payment_processing;
USE payment_processing;

-- 1. Users
CREATE TABLE IF NOT EXISTS Users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    mobile VARCHAR(15) NOT NULL UNIQUE
);

-- 2. Accounts
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

-- 3. UPI
CREATE TABLE IF NOT EXISTS UPI (
    upi_id VARCHAR(100) PRIMARY KEY,
    account_number BIGINT NOT NULL,

    upi_name VARCHAR(100) NOT NULL,

    CONSTRAINT fk_upi_account
        FOREIGN KEY (account_number)
        REFERENCES Accounts(account_number)
        ON DELETE CASCADE
);

-- 4. CreditCards
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

-- 5. PaymentMethods
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

-- 6. Schedules
CREATE TABLE IF NOT EXISTS Schedules (
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    sender_account_number BIGINT NOT NULL,
    receiver_account_number BIGINT NOT NULL,

    amount DECIMAL(15,2) NOT NULL,
    currency_id INT,

    payment_method_id BIGINT NOT NULL,

    description VARCHAR(500),

    frequency ENUM('DAILY','WEEKLY','MONTHLY','YEARLY') NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE,

    next_run_date DATE,
    last_run_date DATE,

    status ENUM('ACTIVE','PAUSED','COMPLETED','CANCELLED')
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

-- 7. Payments
CREATE TABLE IF NOT EXISTS Payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    payment_invoice_number VARCHAR(50) NOT NULL UNIQUE,

    sender_account_number BIGINT NOT NULL,
    receiver_account_number BIGINT NOT NULL,

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

    CONSTRAINT fk_payment_sender
        FOREIGN KEY (sender_account_number)
        REFERENCES Accounts(account_number),

    CONSTRAINT fk_payment_receiver
        FOREIGN KEY (receiver_account_number)
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
