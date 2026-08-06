--data will be added later

USE payment_processing;

-- USERS
INSERT IGNORE INTO Users (user_id, name, mobile)
VALUES
(1, 'Aman Chaurasiya', '9876543210'),
(2, 'Rohit Sharma', '9876543211'),
(3, 'Priya Singh', '9876543212');

-- ACCOUNTS
INSERT IGNORE INTO Accounts (
    account_number,
    user_id,
    bank_name,
    account_type,
    balance,
    ifsc,
    bank_address,
    country,
    is_active,
    not_active_reason
)
VALUES
(100000001, 1, 'HDFC Bank', 'SAVINGS', 50000.00,
 'HDFC0001234', 'Bangalore Branch', 'India', TRUE, NULL),

(100000002, 2, 'ICICI Bank', 'CURRENT', 75000.00,
 'ICIC0005678', 'Mumbai Branch', 'India', TRUE, NULL),

(100000003, 3, 'SBI', 'SAVINGS', 120000.00,
 'SBIN0009876', 'Delhi Branch', 'India', TRUE, NULL);

-- UPI
INSERT IGNORE INTO UPI (
    upi_id,
    account_number,
    upi_name
)
VALUES
('aman@paytm', 100000001, 'Aman UPI'),
('rohit@gpay', 100000002, 'Rohit UPI'),
('priya@phonepe', 100000003, 'Priya UPI');

-- CREDIT CARDS
INSERT IGNORE INTO CreditCards (
    card_number,
    bank,
    cvv,
    expiry_date,
    holder_name,
    user_id
)
VALUES
('4111111111111111', 'HDFC Bank', '123',
 '2028-12-31', 'Aman Chaurasiya', 1),

('4222222222222222', 'ICICI Bank', '456',
 '2029-06-30', 'Rohit Sharma', 2);

-- PAYMENT METHODS
INSERT IGNORE INTO PaymentMethods (
    payment_method_id,
    method_type,
    upi_id,
    card_number,
    account_number
)
VALUES
(1, 'UPI', 'aman@paytm', NULL, NULL),
(2, 'UPI', 'rohit@gpay', NULL, NULL),
(3, 'CREDIT_CARD', NULL, '4111111111111111', NULL),
(4, 'BANK_TRANSFER', NULL, NULL, 100000001);

-- PAYMENTS
INSERT IGNORE INTO Payments (
    payment_id,
    payment_invoice_number,
    sender_account_number,
    receiver_account_number,
    amount,
    payment_date,
    payment_time,
    status,
    description,
    payment_mode,
    schedule_id,
    payment_method_id
)
VALUES
(
    1,
    'INV-1001',
    100000001,
    100000002,
    2500.00,
    '2026-08-02',
    '10:15:00',
    'COMPLETED',
    'Rent Payment',
    'UPI',
    NULL,
    1
),

(
    2,
    'INV-1002',
    100000002,
    100000003,
    1500.00,
    '2026-08-02',
    '11:30:00',
    'COMPLETED',
    'Electricity Bill',
    'CREDIT_CARD',
    NULL,
    3
),

(
    3,
    'INV-1003',
    100000003,
    100000001,
    5000.00,
    '2026-08-03',
    '09:00:00',
    'CREATED',
    'Monthly Transfer',
    'BANK_TRANSFER',
    NULL,
    4
);

-- SCHEDULES
INSERT IGNORE INTO Schedules (
    schedule_id,
    sender_account_number,
    receiver_account_number,
    amount,
    currency_id,
    payment_method_id,
    description,
    frequency,
    start_date,
    execution_time,
    end_date,
    next_run_date,
    last_run_date,
    status
)
VALUES
(
    1,
    100000001,
    100000002,
    2500.00,
    NULL,
    1,
    'Monthly Rent',
    'MONTHLY',
    '2026-08-01',
    '09:00:00',
    '2027-08-01',
    '2026-09-01',
    NULL,
    'ACTIVE'
),
(
    2,
    100000002,
    100000003,
    500.00,
    NULL,
    2,
    'Weekly Grocery Transfer',
    'WEEKLY',
    '2026-08-04',
    '10:00:00',
    NULL,
    '2026-08-11',
    '2026-08-04',
    'ACTIVE'
);


-- TAGS
INSERT IGNORE INTO tags (tag_id, tag_name, description)
VALUES
(1, 'Bills',    'Utility and recurring bills'),
(2, 'Personal', 'Personal payments'),
(3, 'Business', 'Business related transfers'),
(4, 'Shopping', 'Shopping expenses'),
(5, 'Travel',   'Travel and transport'),
(6, 'Others',   'Miscellaneous');


INSERT IGNORE INTO payment_tags (payment_id, tag_id) VALUES
(1, 1), -- Bills
(1, 2), -- Personal
(2, 1), -- Bills
(3, 3); -- Business

