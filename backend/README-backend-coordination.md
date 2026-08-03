# Backend Coordination README
## Scope
Backend-only implementation plan for four parallel developers in a modular monolith.

## Global Architecture Decision
1. Single Spring Boot deployable with strict package/module boundaries.
2. Shared MySQL schema with strict table ownership.
3. External integrations mocked first, real providers later.
4. Authentication and authorization out of current 4-week scope.

## Module Dependency Graph
Developer 1 Payment Core
Depends on: none

Developer 2 Currency and Catalog
Depends on: none

Developer 3 Scheduling and Bulk Execution
Depends on: Developer 1 required, Developer 2 optional

Developer 4 Compliance, Receipts, Query
Depends on: Developer 1 required, Developer 2 required

## Independent Modules
1. Developer 1 and Developer 2 are fully independent after contract freeze.
2. Developer 3 and Developer 4 can scaffold in parallel with mocks from Day 1.

## APIs to Mock Initially
1. External payment gateway adapter.
2. External FX-rate provider adapter.
3. External receipt storage/provider adapter.
4. Internal Payment Core create-payment endpoint for Scheduling until stable.

## Contracts to Finalize Before Feature Coding
1. Payment DTOs and status transition rules.
2. Common error envelope and module error code catalogs.
3. Currency conversion request/response and rounding policy.
4. Schedule trigger response and execution history schema.
5. Receipt metadata schema and anomaly alert schema.

## Database Ownership and Migration Order
### Developer 1 Tables
1. payments
2. payment_recipients
3. payment_status_history
4. idempotency_keys

### Developer 2 Tables
1. currencies
2. exchange_rates
3. payment_modes
4. tags

### Developer 3 Tables
1. scheduled_payments
2. schedule_executions
3. schedule_locks

### Developer 4 Tables
1. anomaly_rules
2. anomaly_alerts
3. receipts
4. audit_logs
5. payment_tags

### Shared Tables
1. payment_tags
- Owned by Developer 4
- References payments and tags
2. audit_logs
- Owned by Developer 4
- Written by all modules through shared audit interface

### Relationship Summary
1. payment_recipients.payment_id -> payments.id
2. payment_status_history.payment_id -> payments.id
3. schedule_executions.scheduled_payment_id -> scheduled_payments.id
4. schedule_locks.scheduled_payment_id -> scheduled_payments.id
5. receipts.payment_id -> payments.id
6. anomaly_alerts.payment_id -> payments.id
7. anomaly_alerts.rule_id -> anomaly_rules.id
8. payment_tags.payment_id -> payments.id
9. payment_tags.tag_id -> tags.id

### Migration Sequence
1. 001-009 Developer 1 payment core
2. 010-019 Developer 2 catalog
3. 020-029 Developer 3 scheduling
4. 030-039 Developer 4 compliance/query
5. 040+ index and performance tuning

## API Contract Index
### Developer 1 Exposes
1. POST /api/v1/payments
2. GET /api/v1/payments/{paymentId}
3. GET /api/v1/payments
4. PATCH /api/v1/payments/{paymentId}/status

### Developer 2 Exposes
1. GET /api/v1/currencies
2. POST /api/v1/currency/convert
3. GET /api/v1/payment-modes
4. GET /api/v1/tags
5. POST /api/v1/tags
6. DELETE /api/v1/tags/{tagId}

### Developer 3 Exposes
1. POST /api/v1/schedules
2. GET /api/v1/schedules/{scheduleId}
3. PATCH /api/v1/schedules/{scheduleId}
4. DELETE /api/v1/schedules/{scheduleId}
5. POST /api/v1/schedules/{scheduleId}/trigger
6. GET /api/v1/schedules/{scheduleId}/executions

### Developer 4 Exposes
1. POST /api/v1/anomaly-rules
2. GET /api/v1/anomaly-rules
3. PATCH /api/v1/anomaly-rules/{ruleId}
4. GET /api/v1/anomaly-alerts
5. POST /api/v1/receipts/{paymentId}
6. GET /api/v1/receipts/{paymentId}
7. GET /api/v1/payments/search
8. GET /api/v1/audit-logs

### Shared Error Envelope
1. timestamp
2. path
3. errorCode
4. message
5. details

### Shared Error Categories
1. VALIDATION_ERROR
2. RESOURCE_NOT_FOUND
3. CONFLICT
4. DEPENDENCY_UNAVAILABLE
5. INTERNAL_ERROR

## Git Strategy
1. Keep main protected with required CI checks.
2. Use module integration branches:
- dev1/payment-core
- dev2/catalog
- dev3/scheduling
- dev4/compliance
3. Create short-lived feature branches from module branches.
4. Merge contract-first PRs into main before feature-heavy PRs.
5. Weekly module-to-main integration PR after tests pass.
6. Maintain CODEOWNERS by module folder to reduce accidental cross-edits.

## Merge Order
1. Developer 1 and Developer 2 foundational merges.
2. Developer 3 merge after Payment Core endpoint stability.
3. Developer 4 merge after Payment Core and Catalog endpoint stability.

## Conflict Prevention
1. Do not directly import another module domain entity.
2. Communicate through API DTOs or service interfaces only.
3. Keep shared contracts in a single owned package and require owner approval for changes.
4. Freeze contract versions for each week to avoid churn.

## 4-Week Backend Development Roadmap
### Week 1
1. Developer 1: payment schema and create/get contracts.
2. Developer 2: catalog schema and conversion contract.
3. Developer 3: scheduling skeleton with mocked payment execution client.
4. Developer 4: anomaly/receipt/query skeleton and common error mapping.

### Week 2
1. Developer 1: lifecycle transitions and idempotency enforcement.
2. Developer 2: FX refresh scheduler and caching.
3. Developer 3: schedule CRUD and recurrence engine.
4. Developer 4: anomaly rules CRUD and receipt metadata flow.

### Week 3
1. Developer 1: transaction hardening and integration stabilization.
2. Developer 2: stale-rate strategy and conversion edge handling.
3. Developer 3: retry/backoff and reconciliation paths.
4. Developer 4: advanced search filters and audit logging.

### Week 4
1. Developer 1: integration fixes and contract compliance tests.
2. Developer 2: load and cache behavior tuning.
3. Developer 3: concurrency and reliability validation.
4. Developer 4: end-to-end compliance validation and error consistency.
5. Team: full migration dry run, regression, release candidate.

## Architecture Review Recommendations
### Package Organization
1. common
2. payments
3. catalog
4. scheduling
5. compliance

Each package contains:
1. controller
2. service
3. domain
4. repository
5. dto
6. mapper

### Design Patterns
1. Hexagonal boundaries for external adapters.
2. Strategy pattern for payment mode handling and anomaly rule evaluation.
3. Application service and domain service separation.

### Transaction Management
1. Use service-layer transactional boundaries for writes.
2. Use read-only transactions for query APIs.
3. Avoid multi-module distributed transactions in MVP.

### Exception Handling
1. Centralize mapping through ControllerAdvice.
2. Keep module-specific error enums with shared envelope.

### Validation
1. Bean validation for request DTOs.
2. Domain invariant checks in service layer.

### Logging
1. Structured logs with correlation ID.
2. No sensitive payment data in logs.

### Security
1. Keep authentication out of scope for this phase.
2. Enforce strict input validation and secret management.

### Caching
1. Cache currencies, rates, modes, tags with TTL.
2. Provide explicit cache invalidation on updates.

### Scheduling
1. Use Spring scheduling for MVP workers.
2. Persist locks and execution records for safety.

### Asynchronous Processing
1. Use async executor for non-critical post-processing.
2. Isolate provider calls behind adapters for retry and timeout control.

### Scalability Improvements
1. Add filter-friendly indexes.
2. Enforce pagination defaults and max page size.
3. Add query timeout and fallback behavior for dependent services.
