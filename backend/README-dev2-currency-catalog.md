# Developer 2 Backend README
## Module: Currency and Catalog Service

## Overview
This module owns reference/master backend data required across the payment domain: currencies, exchange rates, payment modes, and tags.

## Responsibilities
1. Maintain supported currencies and exchange rates.
2. Provide currency conversion service.
3. Maintain payment modes catalog.
4. Maintain tags catalog for payment classification.
5. Handle external FX provider integration via mock adapter in MVP.
6. Provide cache strategy for high-read reference data.

## Features to Implement
1. Get supported currencies.
2. Convert amount from one currency to another.
3. Get payment modes.
4. Create, list, and delete tags.
5. Schedule FX-rate refresh job (mock provider first).
6. Maintain exchange-rate history snapshots.

## Business Logic
1. Conversion uses latest non-stale rate snapshot.
2. If rate is stale beyond threshold, return RATE_NOT_AVAILABLE unless fallback enabled.
3. Decimal rounding policy:
- internal calculations: high precision
- API response: fixed scale for money output
4. Tags are unique by normalized name.
5. Payment modes are controlled vocabulary and not user-defined in MVP.

## Validation Rules
1. Currency codes must be valid ISO-like uppercase code format.
2. Conversion amount must be greater than 0.
3. Exchange rate must be greater than 0.
4. fromCurrency and toCurrency must be supported.
5. Tag name must be non-empty and within length limit.
6. Duplicate normalized tag names are rejected.

## Error Handling
1. UNSUPPORTED_CURRENCY -> HTTP 400
2. RATE_NOT_AVAILABLE -> HTTP 503
3. TAG_ALREADY_EXISTS -> HTTP 409
4. TAG_NOT_FOUND -> HTTP 404
5. VALIDATION_ERROR -> HTTP 400

Standard error envelope:
1. timestamp
2. path
3. errorCode
4. message
5. details

## Dependencies
### Depends on
1. No required internal module dependency for core implementation.

### APIs Consumed
1. External FX provider adapter (mock first).

### APIs Exposed
1. GET /api/v1/currencies
2. POST /api/v1/currency/convert
3. GET /api/v1/payment-modes
4. GET /api/v1/tags
5. POST /api/v1/tags
6. DELETE /api/v1/tags/{tagId}

## API Contract
### GET /api/v1/currencies
Method: GET

Response 200:
1. currencies: array of { code, name, symbol, active }

Status Codes:
1. 200 OK

Validation Rules:
1. none

Error Codes:
1. INTERNAL_ERROR

### POST /api/v1/currency/convert
Method: POST

Request:
1. fromCurrency: string
2. toCurrency: string
3. amount: decimal

Response 200:
1. fromCurrency
2. toCurrency
3. amount
4. rate
5. convertedAmount
6. rateTimestamp

Status Codes:
1. 200 OK
2. 400 Bad Request
3. 404 Not Found
4. 503 Service Unavailable

Validation Rules:
1. amount > 0
2. currencies must be supported

Error Codes:
1. UNSUPPORTED_CURRENCY
2. RATE_NOT_AVAILABLE
3. VALIDATION_ERROR

### GET /api/v1/payment-modes
Method: GET

Response 200:
1. modes: array of { code, displayName, active }

Status Codes:
1. 200 OK

Validation Rules:
1. none

Error Codes:
1. INTERNAL_ERROR

### GET /api/v1/tags
Method: GET

Request Query Params:
1. search optional
2. page optional
3. size optional

Response 200:
1. paginated tags

Status Codes:
1. 200 OK
2. 400 Bad Request

Validation Rules:
1. page >= 0
2. size within limit

Error Codes:
1. VALIDATION_ERROR

### POST /api/v1/tags
Method: POST

Request:
1. name: string
2. category optional

Response 201:
1. tagId
2. name
3. category
4. createdAt

Status Codes:
1. 201 Created
2. 400 Bad Request
3. 409 Conflict

Validation Rules:
1. non-empty name
2. max length
3. unique normalized name

Error Codes:
1. TAG_ALREADY_EXISTS
2. VALIDATION_ERROR

### DELETE /api/v1/tags/{tagId}
Method: DELETE

Response 204

Status Codes:
1. 204 No Content
2. 404 Not Found

Validation Rules:
1. valid tagId

Error Codes:
1. TAG_NOT_FOUND
2. VALIDATION_ERROR

## Database Ownership
### Tables Created by Developer 2
1. currencies
2. exchange_rates
3. payment_modes
4. tags

### Shared Tables Used
1. payment_tags (owned by Developer 4)

### Relationships
1. payment_tags.tag_id -> tags.id

### Migration Order
1. 010_create_currencies.sql
2. 011_create_exchange_rates.sql
3. 012_create_payment_modes.sql
4. 013_create_tags.sql

## Parallel Development Notes
1. This module can begin immediately after contract freeze.
2. Provide stable conversion response schema by end of Week 1.
3. Provide sample seed data for currencies and payment modes for other modules.
