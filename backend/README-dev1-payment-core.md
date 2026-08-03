# Developer 1 Backend README
## Module: Payment Core Service

## Overview
This module owns the transactional payment domain. It is the source of truth for payment lifecycle, recipient allocation, status transitions, and idempotent payment creation.

## Responsibilities
1. Own payment write path and lifecycle state machine.
2. Own payment recipients and allocation integrity.
3. Own idempotency handling for create-payment APIs.
4. Expose core payment APIs consumed by other backend modules.
5. Publish domain events for scheduling and compliance modules.

## Features to Implement
1. Create payment (single and multi-recipient).
2. Retrieve payment by ID.
3. List payments with core filters (status, mode, date, amount).
4. Update payment status with valid transition rules.
5. Cancel/refund hooks (domain-level readiness and API contract).
6. Payment status history tracking.

## Business Logic
1. Amount conservation rule: sum of recipient amounts equals total amount.
2. Status transition rules:
- CREATED -> PROCESSING
- PROCESSING -> SUCCESS
- PROCESSING -> FAILED
- SUCCESS -> REFUNDED
3. Terminal states are immutable except explicit allowed actions.
4. Idempotent create-payment must return existing result for repeated idempotency key.
5. No partial recipient settlement state is persisted in MVP unless explicitly required.

## Validation Rules
1. totalAmount must be greater than 0.
2. recipients must contain at least 1 entry.
3. each recipient amount must be greater than 0.
4. sum(recipientAmounts) must equal totalAmount.
5. currency must be supported (validated through catalog contract).
6. paymentMode must be supported.
7. idempotency key header is required for create-payment.
8. payerId and recipientId values must be non-empty and valid format.

## Error Handling
1. PAYMENT_NOT_FOUND -> HTTP 404
2. INVALID_PAYMENT_STATE -> HTTP 409
3. DUPLICATE_IDEMPOTENCY_KEY -> HTTP 409
4. VALIDATION_ERROR -> HTTP 400
5. UNSUPPORTED_PAYMENT_MODE -> HTTP 400
6. DEPENDENCY_UNAVAILABLE -> HTTP 503

Standard error envelope:
1. timestamp
2. path
3. errorCode
4. message
5. details

## Dependencies
### Depends on
1. Currency/Catalog module APIs for supported currency and mode validation.

### APIs Consumed
1. GET /api/v1/currencies
2. GET /api/v1/payment-modes

### APIs Exposed
1. POST /api/v1/payments
2. GET /api/v1/payments/{paymentId}
3. GET /api/v1/payments
4. PATCH /api/v1/payments/{paymentId}/status

## API Contract
### POST /api/v1/payments
Method: POST

Request:
1. payerId: string
2. totalAmount: decimal
3. currency: string
4. paymentMode: string
5. recipients: array of { recipientId: string, amount: decimal }
6. tags: optional string[]
7. header: Idempotency-Key

Response 201:
1. paymentId: string
2. status: string
3. totalAmount: decimal
4. currency: string
5. recipients: array
6. createdAt: datetime

Status Codes:
1. 201 Created
2. 400 Bad Request
3. 409 Conflict
4. 422 Unprocessable Entity
5. 503 Service Unavailable

Validation Rules:
1. Positive amounts
2. Non-empty recipient list
3. Amount conservation
4. Valid enums
5. Required idempotency key

Error Codes:
1. VALIDATION_ERROR
2. DUPLICATE_IDEMPOTENCY_KEY
3. UNSUPPORTED_PAYMENT_MODE
4. DEPENDENCY_UNAVAILABLE

### GET /api/v1/payments/{paymentId}
Method: GET

Response 200:
1. payment aggregate details
2. status history

Status Codes:
1. 200 OK
2. 404 Not Found

Validation Rules:
1. paymentId must be valid format

Error Codes:
1. PAYMENT_NOT_FOUND
2. VALIDATION_ERROR

### GET /api/v1/payments
Method: GET

Request Query Params:
1. status
2. mode
3. fromDate
4. toDate
5. minAmount
6. maxAmount
7. page
8. size

Response 200:
1. paginated payment list

Status Codes:
1. 200 OK
2. 400 Bad Request

Validation Rules:
1. page >= 0
2. size in allowed limit
3. fromDate <= toDate
4. minAmount <= maxAmount

Error Codes:
1. SEARCH_FILTER_INVALID
2. VALIDATION_ERROR

### PATCH /api/v1/payments/{paymentId}/status
Method: PATCH

Request:
1. targetStatus: string
2. reason: optional string

Response 200:
1. paymentId
2. previousStatus
3. targetStatus
4. updatedAt

Status Codes:
1. 200 OK
2. 400 Bad Request
3. 404 Not Found
4. 409 Conflict

Validation Rules:
1. targetStatus must be known enum
2. transition must be valid from current state

Error Codes:
1. INVALID_PAYMENT_STATE
2. PAYMENT_NOT_FOUND
3. VALIDATION_ERROR

## Database Ownership
### Tables Created by Developer 1
1. payments
2. payment_recipients
3. payment_status_history
4. idempotency_keys

### Shared Tables Used
1. payment_tags (owned by Developer 4)

### Relationships
1. payment_recipients.payment_id -> payments.id
2. payment_status_history.payment_id -> payments.id

### Migration Order
1. 001_create_payments.sql
2. 002_create_payment_recipients.sql
3. 003_create_payment_status_history.sql
4. 004_create_idempotency_keys.sql

## Parallel Development Notes
1. This module can begin immediately after contract freeze.
2. Provide mock responses early for Scheduling and Compliance modules.
3. Freeze DTOs and error enums before Week 2 to prevent downstream rework.
