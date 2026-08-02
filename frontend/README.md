# Payment Processing Interface

A React + JavaScript frontend for a payment processing platform. This is currently a **frontend-only** build: all data comes from local demo fixtures and a stub API layer so the UI is fully interactive while the real backend is being developed.

## Tech stack

- React 19 + Vite
- React Router (`react-router-dom`) for client-side routing
- Plain, dedicated CSS files per component/page (no CSS framework/utility library)

## Features implemented

- **Dashboard / Home page** — summary stats (total paid, failed, pending/processing, upcoming scheduled) and a list of recent payments with quick actions to create or schedule a payment.
- **Payment processing (new payment) flow** — a 3-step wizard (Details → Review → Confirmation) for submitting a new payment.
- **Payment status & failure detail** — each payment has a visual status timeline (initiated → processing → completed) and, for failed payments, a detailed failure panel showing the error code, message, explanation and timestamp, plus a retry action.
- **Filter payment history** — filter past payments by vendor, tag/category (Vendor, Loan, Subscription, Utility, Payroll, Tax), status, and free-text search.
- **Invoice / receipt download** — a download action on completed payments (currently a stub that will call the backend's receipt endpoint).
- **Schedule a payment** — the same wizard supports scheduling a payment for a future date/time with a recurrence option (none, weekly, monthly, quarterly); scheduled payments are listed separately and can be cancelled.
- **Multi-currency support** — payments can be created/scheduled in any of several supported currencies, with locale-aware currency formatting and a conversion helper for future exchange-rate support.
- **Responsive layout** — navigation and tables adapt to smaller screens.
- **404 / not found page** for unmatched routes.

## Project structure

```
src/
  api/
    routes.js          Backend route/endpoint constants (single source of truth for API paths)
    paymentApi.js       Stub API functions (returns demo data) — swap for real fetch calls later
  data/                 Demo fixtures — delete once the backend is connected
    demoPayments.js
    demoScheduledPayments.js
    demoVendors.js
    demoCurrencies.js
  utils/
    currency.js         Currency formatting & conversion helpers
    format.js            Date formatting & status label/tone helpers
  styles/
    layout.css          Shared page shell (page, page-header, links, detail list, etc.)
    buttons.css          Shared button and input primitives
  components/
    layout/
      Navbar.jsx / .css
    common/
      StatCard.jsx / .css
      StatusBadge.jsx / .css
      EmptyState.jsx / .css
      Spinner.jsx / .css
    payments/
      PaymentFilters.jsx / .css
      PaymentList.jsx / .css
      TagChips.jsx / .css
      PaymentFailureDetails.jsx / .css
      PaymentStatusTimeline.jsx / .css
      ReceiptDownloadButton.jsx / .css
      CurrencySelect.jsx
  pages/
    HomePage.jsx / .css
    PaymentsHistoryPage.jsx
    PaymentDetailsPage.jsx / .css
    NewPaymentPage.jsx / .css     Handles both "pay now" and "schedule for later"
    ScheduledPaymentsPage.jsx
    NotFoundPage.jsx
```

Every component/page owns its own dedicated CSS file (imported directly in the `.jsx` file); there is no global utility-class framework. Only a small set of shared primitives (page shell, buttons/inputs) live in `src/styles/`.

## Routes

| Path | Page |
| --- | --- |
| `/` | Home dashboard |
| `/payments` | Payment history with filters |
| `/payments/new` | New payment wizard (pay now) |
| `/payments/:id` | Payment details, status & failure reason |
| `/scheduled` | Scheduled payments list |
| `/scheduled/new` | New payment wizard (schedule for later) |

## Backend integration

The frontend expects a backend exposing the following REST routes (see [src/api/routes.js](src/api/routes.js)):

- `GET /api/payments` — payment history (supports vendor/tag/status/search filters)
- `GET /api/payments/:id` — payment detail
- `GET /api/payments/tags` — available filter tags
- `POST /api/payments` — create/submit a payment
- `POST /api/payments/:id/retry` — retry a failed payment
- `GET /api/payments/:id/receipt` — download invoice/receipt
- `GET /api/payments/scheduled` — list scheduled payments
- `POST /api/payments/scheduled` — schedule a payment
- `POST /api/payments/scheduled/:id/cancel` — cancel a scheduled payment
- `GET /api/vendors` — known vendors/payees
- `GET /api/currencies` / `GET /api/currencies/rates` — supported currencies & exchange rates
- `GET /api/dashboard/summary` — dashboard aggregate stats

Once the backend is available, replace the demo-data logic in [src/api/paymentApi.js](src/api/paymentApi.js) with real `fetch` calls and delete the `src/data/` demo fixtures.

## Getting started

```bash
npm install
npm run dev
```

Other scripts: `npm run build`, `npm run preview`, `npm run lint`.

