# Developer 3 Backend README
## Module: Scheduling and Bulk Execution Service

## Overview
This module owns future and recurring payment orchestration. It schedules payment execution, performs safe triggering, and stores execution history with retry behavior.

## Responsibilities
1. Own schedule lifecycle (create, update, pause, cancel).
2. Own recurrence evaluation and next-run computation.
3. Trigger payment execution through Payment Core API contract.
4. Own retry policy and timeout handling.
5. Own execution history and reconciliation endpoints.

## Features to Implement
1. Create schedule for one-time and recurring payments.
2. Update schedule details and status.
3. Cancel schedule.
4. Manual schedule trigger endpoint.
5. Automatic scheduler worker.
6. Execution history retrieval.
7. Retry with backoff for transient failures.
8. Locking to prevent duplicate concurrent execution.

## Business Logic
1. nextRunAt is computed from schedule type and recurrence settings.
2. A schedule lock is acquired before trigger and released after completion.
3. Retries occur for retryable failures only.
4. Non-retryable errors mark execution as FAILED permanently.
5. Duplicate execution at same scheduled window is prevented.
6. Catch-up policy in MVP: execute next upcoming run only.

## Validation Rules
1. startAt must be in the future when creating schedule.
2. recurrence config must match supported patterns.
3. payment template reference must be valid.
4. schedule status transitions must be valid.
5. retry config values must be within allowed limits.

## Error Handling
1. SCHEDULE_NOT_FOUND -> HTTP 404
2. INVALID_RECURRENCE -> HTTP 400
3. SCHEDULE_CONFLICT -> HTTP 409
4. EXECUTION_TIMEOUT -> HTTP 504
5. DEPENDENCY_UNAVAILABLE -> HTTP 503
6. VALIDATION_ERROR -> HTTP 400

Standard error envelope:
1. timestamp
2. path
3. errorCode
4. message
5. details

## Dependencies
### Depends on
1. Payment Core module contract for payment creation/trigger.
2. Optional Currency/Catalog conversion API for schedule-time conversion behavior.

### APIs Consumed
1. POST /api/v1/payments
2. POST /api/v1/currency/convert optional

### APIs Exposed
1. POST /api/v1/schedules
2. GET /api/v1/schedules/{scheduleId}
3. PATCH /api/v1/schedules/{scheduleId}
4. DELETE /api/v1/schedules/{scheduleId}
5. POST /api/v1/schedules/{scheduleId}/trigger
6. GET /api/v1/schedules/{scheduleId}/executions

## API Contract
### POST /api/v1/schedules
Method: POST

Request:
1. paymentTemplateRef or inline payment payload
2. scheduleType: ONCE or RECURRING
3. startAt: datetime
4. recurrence object optional for recurring schedule
5. retryPolicy optional

Response 201:
1. scheduleId
2. status
3. nextRunAt
4. createdAt

Status Codes:
1. 201 Created
2. 400 Bad Request
3. 409 Conflict

Validation Rules:
1. startAt in future
2. valid recurrence object
3. valid payment template reference

Error Codes:
1. INVALID_RECURRENCE
2. VALIDATION_ERROR
3. SCHEDULE_CONFLICT

### GET /api/v1/schedules/{scheduleId}
Method: GET

Response 200:
1. schedule details
2. current status
3. nextRunAt

Status Codes:
1. 200 OK
2. 404 Not Found

Validation Rules:
1. valid scheduleId

Error Codes:
1. SCHEDULE_NOT_FOUND
2. VALIDATION_ERROR

### PATCH /api/v1/schedules/{scheduleId}
Method: PATCH

Request:
1. allowed mutable fields
2. target status optional

Response 200:
1. updated schedule

Status Codes:
1. 200 OK
2. 400 Bad Request
3. 404 Not Found
4. 409 Conflict

Validation Rules:
1. no illegal transition
2. recurrence remains valid

Error Codes:
1. SCHEDULE_NOT_FOUND
2. SCHEDULE_CONFLICT
3. INVALID_RECURRENCE

### DELETE /api/v1/schedules/{scheduleId}
Method: DELETE

Response 204

Status Codes:
1. 204 No Content
2. 404 Not Found
3. 409 Conflict

Validation Rules:
1. valid scheduleId
2. schedule not in non-cancellable terminal execution state

Error Codes:
1. SCHEDULE_NOT_FOUND
2. SCHEDULE_CONFLICT

### POST /api/v1/schedules/{scheduleId}/trigger
Method: POST

Response 202:
1. executionId
2. scheduleId
3. acceptedAt

Status Codes:
1. 202 Accepted
2. 404 Not Found
3. 409 Conflict
4. 503 Service Unavailable

Validation Rules:
1. schedule active and triggerable
2. no active lock conflict

Error Codes:
1. SCHEDULE_NOT_FOUND
2. SCHEDULE_CONFLICT
3. DEPENDENCY_UNAVAILABLE

### GET /api/v1/schedules/{scheduleId}/executions
Method: GET

Request Query Params:
1. page
2. size
3. status optional

Response 200:
1. paginated execution records

Status Codes:
1. 200 OK
2. 400 Bad Request
3. 404 Not Found

Validation Rules:
1. pagination bounds

Error Codes:
1. VALIDATION_ERROR
2. SCHEDULE_NOT_FOUND

## Database Ownership
### Tables Created by Developer 3
1. scheduled_payments
2. schedule_executions
3. schedule_locks

### Shared Tables Used
1. payments via API contract only (no direct cross-module writes)

### Relationships
1. schedule_executions.scheduled_payment_id -> scheduled_payments.id
2. schedule_locks.scheduled_payment_id -> scheduled_payments.id

### Migration Order
1. 020_create_scheduled_payments.sql
2. 021_create_schedule_executions.sql
3. 022_create_schedule_locks.sql

## Parallel Development Notes
1. Scaffold can start in Week 1 using mocked Payment Core API.
2. Replace mocks with real contract integration when Payment Core reaches stable endpoint behavior.
3. Keep scheduling worker and API layer independently testable.
