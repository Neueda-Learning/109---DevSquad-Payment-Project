# Developer 4 Backend README
## Module: Compliance, Receipt, and Query Service

## Overview
This module owns risk/compliance and advanced query capabilities. It handles anomaly rules, anomaly alerts, receipt metadata generation, advanced payment filtering, and audit logs.

## Responsibilities
1. Own anomaly rule definitions and evaluation logic.
2. Own anomaly alert persistence and retrieval.
3. Own receipt generation metadata and retrieval contract.
4. Own advanced search/filter APIs for payments.
5. Own centralized audit log persistence contract.
6. Own global exception mapping consistency enforcement.

## Features to Implement
1. Create/list/update anomaly rules.
2. Evaluate threshold-based anomalies.
3. List anomaly alerts with filters.
4. Generate receipt metadata for eligible payments.
5. Retrieve receipt metadata/download location.
6. Advanced payment search by tags, status, mode, amount, date, currency.
7. Record and expose audit entries.

## Business Logic
1. Threshold rule checks compare payment amount against configured rule threshold.
2. Alerts include severity and reason and are deduplicated in configured time window.
3. Receipts are generated only for successful/settled payments.
4. Search API supports bounded date windows and paginated responses.
5. payment_tags junction supports many-to-many payment and tag mapping.

## Validation Rules
1. rule threshold must be greater than 0.
2. rule type and severity must be valid enums.
3. receipt request requires payment in eligible status.
4. date ranges must satisfy fromDate <= toDate and max range limit.
5. tag filters must reference existing tags.

## Error Handling
1. ANOMALY_RULE_INVALID -> HTTP 400
2. RECEIPT_NOT_AVAILABLE -> HTTP 404
3. PAYMENT_NOT_SETTLED -> HTTP 409
4. SEARCH_FILTER_INVALID -> HTTP 400
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
1. Payment Core module for payment data and status.
2. Currency/Catalog module for tag and currency metadata.

### APIs Consumed
1. GET /api/v1/payments/{paymentId}
2. GET /api/v1/tags
3. GET /api/v1/currencies

### APIs Exposed
1. POST /api/v1/anomaly-rules
2. GET /api/v1/anomaly-rules
3. PATCH /api/v1/anomaly-rules/{ruleId}
4. GET /api/v1/anomaly-alerts
5. POST /api/v1/receipts/{paymentId}
6. GET /api/v1/receipts/{paymentId}
7. GET /api/v1/payments/search
8. GET /api/v1/audit-logs

## API Contract
### POST /api/v1/anomaly-rules
Method: POST

Request:
1. name: string
2. ruleType: string
3. threshold: decimal
4. currency optional
5. severity: string
6. active: boolean

Response 201:
1. ruleId
2. name
3. ruleType
4. threshold
5. severity
6. active

Status Codes:
1. 201 Created
2. 400 Bad Request
3. 409 Conflict

Validation Rules:
1. threshold > 0
2. valid enums

Error Codes:
1. ANOMALY_RULE_INVALID
2. VALIDATION_ERROR

### GET /api/v1/anomaly-rules
Method: GET

Response 200:
1. rules list

Status Codes:
1. 200 OK

Validation Rules:
1. none

Error Codes:
1. INTERNAL_ERROR

### PATCH /api/v1/anomaly-rules/{ruleId}
Method: PATCH

Request:
1. mutable fields only

Response 200:
1. updated rule

Status Codes:
1. 200 OK
2. 400 Bad Request
3. 404 Not Found

Validation Rules:
1. threshold if present must be > 0

Error Codes:
1. ANOMALY_RULE_INVALID
2. RESOURCE_NOT_FOUND

### GET /api/v1/anomaly-alerts
Method: GET

Request Query Params:
1. fromDate optional
2. toDate optional
3. severity optional
4. page
5. size

Response 200:
1. paginated alerts

Status Codes:
1. 200 OK
2. 400 Bad Request

Validation Rules:
1. valid date range
2. pagination bounds

Error Codes:
1. SEARCH_FILTER_INVALID
2. VALIDATION_ERROR

### POST /api/v1/receipts/{paymentId}
Method: POST

Request:
1. optional template or format selector

Response 201:
1. receiptId
2. paymentId
3. downloadUrl or storageKey
4. generatedAt

Status Codes:
1. 201 Created
2. 404 Not Found
3. 409 Conflict
4. 503 Service Unavailable

Validation Rules:
1. payment must exist
2. payment must be settled/successful

Error Codes:
1. PAYMENT_NOT_SETTLED
2. RECEIPT_NOT_AVAILABLE
3. DEPENDENCY_UNAVAILABLE

### GET /api/v1/receipts/{paymentId}
Method: GET

Response 200:
1. receipt metadata

Status Codes:
1. 200 OK
2. 404 Not Found

Validation Rules:
1. valid paymentId

Error Codes:
1. RECEIPT_NOT_AVAILABLE
2. VALIDATION_ERROR

### GET /api/v1/payments/search
Method: GET

Request Query Params:
1. tags optional list
2. status optional
3. mode optional
4. currency optional
5. minAmount optional
6. maxAmount optional
7. fromDate optional
8. toDate optional
9. page
10. size

Response 200:
1. paginated payment search results

Status Codes:
1. 200 OK
2. 400 Bad Request

Validation Rules:
1. minAmount <= maxAmount
2. fromDate <= toDate
3. bounded date range

Error Codes:
1. SEARCH_FILTER_INVALID
2. VALIDATION_ERROR

## Database Ownership
### Tables Created by Developer 4
1. anomaly_rules
2. anomaly_alerts
3. receipts
4. audit_logs
5. payment_tags

### Shared Tables Used
1. payments (owned by Developer 1)
2. tags (owned by Developer 2)

### Relationships
1. anomaly_alerts.payment_id -> payments.id
2. anomaly_alerts.rule_id -> anomaly_rules.id
3. receipts.payment_id -> payments.id
4. payment_tags.payment_id -> payments.id
5. payment_tags.tag_id -> tags.id

### Migration Order
1. 030_create_anomaly_rules.sql
2. 031_create_anomaly_alerts.sql
3. 032_create_receipts.sql
4. 033_create_audit_logs.sql
5. 034_create_payment_tags.sql

## Parallel Development Notes
1. Scaffold can start in Week 1 using mocked Payment Core and Catalog responses.
2. Finalize search and error envelope contracts early to reduce downstream conflicts.
3. Keep anomaly evaluation engine isolated behind service interface for future rule expansion.
