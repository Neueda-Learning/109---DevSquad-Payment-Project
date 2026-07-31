// Payment API layer.
//
// IMPORTANT: This module currently returns local demo data so the UI is fully
// functional without a backend. Every function below documents the real
// backend route (see ./routes.js) it should call once available. When the
// backend is ready, replace the demo-data logic in each function with a
// `fetch(ROUTE, options)` call and delete src/data/*.
//
// No business logic lives here — these are thin pass-through stubs only.

import { demoPayments } from '../data/demoPayments'
import { demoScheduledPayments } from '../data/demoScheduledPayments'
import { demoVendors } from '../data/demoVendors'
import { demoCurrencies } from '../data/demoCurrencies'

// Simulates network latency so loading states can be exercised in the UI.
const simulateRequest = (data, delay = 500) =>
  new Promise((resolve) => setTimeout(() => resolve(data), delay))

/**
 * GET ROUTES.PAYMENTS
 * Fetch payment history. Supports optional filters (vendor, tags, status, search).
 */
export async function fetchPayments(filters = {}) {
  // TODO(backend): fetch(`${ROUTES.PAYMENTS}?${new URLSearchParams(filters)}`)
  let results = [...demoPayments]

  if (filters.tags?.length) {
    results = results.filter((p) => p.tags.some((t) => filters.tags.includes(t)))
  }
  if (filters.vendor) {
    results = results.filter((p) => p.vendor === filters.vendor)
  }
  if (filters.status) {
    results = results.filter((p) => p.status === filters.status)
  }
  if (filters.search) {
    const q = filters.search.toLowerCase()
    results = results.filter(
      (p) =>
        p.vendor.toLowerCase().includes(q) ||
        p.reference.toLowerCase().includes(q) ||
        p.description.toLowerCase().includes(q),
    )
  }

  return simulateRequest(results)
}

/**
 * GET ROUTES.PAYMENT_BY_ID(id)
 * Fetch a single payment's full detail, including failure reason if applicable.
 */
export async function fetchPaymentById(id) {
  // TODO(backend): fetch(ROUTES.PAYMENT_BY_ID(id))
  const payment = demoPayments.find((p) => p.id === id) || null
  return simulateRequest(payment)
}

/**
 * GET ROUTES.PAYMENT_TAGS
 * Fetch the list of available filter tags/categories.
 */
export async function fetchPaymentTags() {
  // TODO(backend): fetch(ROUTES.PAYMENT_TAGS)
  const tags = Array.from(new Set(demoPayments.flatMap((p) => p.tags)))
  return simulateRequest(tags)
}

/**
 * POST ROUTES.CREATE_PAYMENT
 * Submit a new payment for processing.
 */
export async function createPayment(paymentDraft) {
  // TODO(backend): fetch(ROUTES.CREATE_PAYMENT, { method: 'POST', body: JSON.stringify(paymentDraft) })
  const newPayment = {
    id: `pay_${Math.floor(Math.random() * 100000)}`,
    reference: `INV-${Date.now()}`,
    status: 'pending',
    createdAt: new Date().toISOString(),
    completedAt: null,
    invoiceAvailable: false,
    failureReason: null,
    ...paymentDraft,
  }
  return simulateRequest(newPayment, 800)
}

/**
 * POST ROUTES.RETRY_PAYMENT(id)
 * Retry a previously failed payment.
 */
export async function retryPayment(id) {
  // TODO(backend): fetch(ROUTES.RETRY_PAYMENT(id), { method: 'POST' })
  return simulateRequest({ id, status: 'pending' }, 600)
}

/**
 * GET ROUTES.PAYMENT_RECEIPT(id)
 * Download the invoice/receipt document for a completed payment.
 * Backend is expected to respond with a PDF (application/pdf) blob.
 */
export async function downloadReceipt(id) {
  // TODO(backend): const res = await fetch(ROUTES.PAYMENT_RECEIPT(id)); return res.blob()
  return simulateRequest(
    { id, url: null, message: 'Receipt download will be available once the backend is connected.' },
    400,
  )
}

/**
 * GET ROUTES.SCHEDULED_PAYMENTS
 * Fetch upcoming scheduled/recurring payments.
 */
export async function fetchScheduledPayments() {
  // TODO(backend): fetch(ROUTES.SCHEDULED_PAYMENTS)
  return simulateRequest([...demoScheduledPayments])
}

/**
 * POST ROUTES.SCHEDULE_PAYMENT
 * Schedule a new future/recurring payment.
 */
export async function schedulePayment(scheduleDraft) {
  // TODO(backend): fetch(ROUTES.SCHEDULE_PAYMENT, { method: 'POST', body: JSON.stringify(scheduleDraft) })
  const newSchedule = {
    id: `sch_${Math.floor(Math.random() * 100000)}`,
    status: 'scheduled',
    ...scheduleDraft,
  }
  return simulateRequest(newSchedule, 800)
}

/**
 * POST ROUTES.CANCEL_SCHEDULED_PAYMENT(id)
 * Cancel a previously scheduled payment.
 */
export async function cancelScheduledPayment(id) {
  // TODO(backend): fetch(ROUTES.CANCEL_SCHEDULED_PAYMENT(id), { method: 'POST' })
  return simulateRequest({ id, status: 'cancelled' }, 500)
}

/**
 * GET ROUTES.VENDORS
 * Fetch known vendors/payees (used for filters and payment creation).
 */
export async function fetchVendors() {
  // TODO(backend): fetch(ROUTES.VENDORS)
  return simulateRequest([...demoVendors])
}

/**
 * GET ROUTES.CURRENCIES
 * Fetch supported currencies.
 */
export async function fetchCurrencies() {
  // TODO(backend): fetch(ROUTES.CURRENCIES)
  return simulateRequest([...demoCurrencies])
}

/**
 * GET ROUTES.EXCHANGE_RATES
 * Fetch live exchange rates relative to USD.
 */
export async function fetchExchangeRates() {
  // TODO(backend): fetch(ROUTES.EXCHANGE_RATES)
  const rates = Object.fromEntries(demoCurrencies.map((c) => [c.code, c.rateToUSD]))
  return simulateRequest(rates)
}

/**
 * GET ROUTES.DASHBOARD_SUMMARY
 * Fetch aggregate stats for the home dashboard.
 */
export async function fetchDashboardSummary() {
  // TODO(backend): fetch(ROUTES.DASHBOARD_SUMMARY)
  const totalPaid = demoPayments
    .filter((p) => p.status === 'success')
    .reduce((sum, p) => sum + p.amount, 0)
  const totalFailed = demoPayments.filter((p) => p.status === 'failed').length
  const totalPending = demoPayments.filter(
    (p) => p.status === 'pending' || p.status === 'processing',
  ).length
  const upcoming = demoScheduledPayments.filter((s) => s.status === 'scheduled').length

  return simulateRequest({
    totalPaid,
    totalFailed,
    totalPending,
    upcomingScheduled: upcoming,
    recentPayments: demoPayments.slice(0, 5),
  })
}
