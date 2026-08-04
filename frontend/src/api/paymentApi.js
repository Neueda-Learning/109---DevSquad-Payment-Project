// Payment API layer.
//
// Each function below first attempts to call the real backend route (see
// ./routes.js). If the request fails (network error, non-OK response, or an
// empty/falsy payload) it falls back to local demo data so the UI stays
// fully functional without a backend. Once the backend is reliably
// available, the demo-data fallback logic can be removed.

import { ROUTES } from './routes'
import { demoPayments } from '../data/demoPayments'
import { demoScheduledPayments } from '../data/demoScheduledPayments'
import { demoVendors } from '../data/demoVendors'
import { demoCurrencies } from '../data/demoCurrencies'

// Simulates network latency so loading states can be exercised in the UI.
const simulateRequest = (data, delay = 500) =>
  new Promise((resolve) => setTimeout(() => resolve(data), delay))

/**
 * Attempts a fetch call against the backend. Returns `null` (instead of
 * throwing) if the request fails, the response is not OK, or the parsed
 * body is empty/falsy — signalling that the caller should fall back to
 * demo data.
 */
async function tryFetch(url, options) {
  try {
    const res = await fetch(url, options)
    if (!res.ok) return null

    const contentType = res.headers.get('content-type') || ''
    const data = contentType.includes('application/json') ? await res.json() : await res.blob()

    if (data == null) return null
    if (Array.isArray(data) && data.length === 0) return null
    if (typeof data === 'object' && !Array.isArray(data) && Object.keys(data).length === 0) {
      return null
    }

    return data
  } catch {
    return null
  }
}

/**
 * GET ROUTES.PAYMENTS
 * Fetch payment history. Supports optional filters (vendor, tags, status, search).
 */
export async function fetchPayments(filters = {}) {
  const backendData = await tryFetch(`${ROUTES.PAYMENTS}?${new URLSearchParams(filters)}`)
  if (backendData) return backendData

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
  const backendData = await tryFetch(ROUTES.PAYMENT_BY_ID(id))
  if (backendData) return backendData

  const payment = demoPayments.find((p) => p.id === id) || null
  return simulateRequest(payment)
}

/**
 * GET ROUTES.PAYMENT_TAGS
 * Fetch the list of available filter tags/categories.
 */
export async function fetchPaymentTags() {
  const backendData = await tryFetch(ROUTES.PAYMENT_TAGS)
  if (backendData) return backendData

  const tags = Array.from(new Set(demoPayments.flatMap((p) => p.tags)))
  return simulateRequest(tags)
}

/**
 * POST ROUTES.CREATE_PAYMENT
 * Submit a new payment for processing.
 */
export async function createPayment(paymentDraft) {
  const backendData = await tryFetch(ROUTES.CREATE_PAYMENT, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(paymentDraft),
  })
  if (backendData) return backendData

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
  const backendData = await tryFetch(ROUTES.RETRY_PAYMENT(id), { method: 'POST' })
  if (backendData) return backendData

  return simulateRequest({ id, status: 'pending' }, 600)
}

/**
 * GET ROUTES.PAYMENT_RECEIPT(id)
 * Download the invoice/receipt document for a completed payment.
 * Backend is expected to respond with a PDF (application/pdf) blob.
 */
export async function downloadReceipt(id) {
  const backendData = await tryFetch(ROUTES.PAYMENT_RECEIPT(id))
  if (backendData) return backendData

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
  const backendData = await tryFetch(ROUTES.SCHEDULED_PAYMENTS)
  if (backendData) return backendData

  return simulateRequest([...demoScheduledPayments])
}

/**
 * POST ROUTES.SCHEDULE_PAYMENT
 * Schedule a new future/recurring payment.
 */
export async function schedulePayment(scheduleDraft) {
  const backendData = await tryFetch(ROUTES.SCHEDULE_PAYMENT, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(scheduleDraft),
  })
  if (backendData) return backendData

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
  const backendData = await tryFetch(ROUTES.CANCEL_SCHEDULED_PAYMENT(id), { method: 'POST' })
  if (backendData) return backendData

  return simulateRequest({ id, status: 'cancelled' }, 500)
}

/**
 * GET ROUTES.VENDORS
 * Fetch known vendors/payees (used for filters and payment creation).
 */
export async function fetchVendors() {
  const backendData = await tryFetch(ROUTES.VENDORS)
  if (backendData) return backendData

  return simulateRequest([...demoVendors])
}

/**
 * GET ROUTES.CURRENCIES
 * Fetch supported currencies.
 */
export async function fetchCurrencies() {
  const backendData = await tryFetch(ROUTES.CURRENCIES)
  if (backendData) return backendData

  return simulateRequest([...demoCurrencies])
}

/**
 * GET ROUTES.EXCHANGE_RATES
 * Fetch live exchange rates relative to USD.
 */
export async function fetchExchangeRates() {
  const backendData = await tryFetch(ROUTES.EXCHANGE_RATES)
  if (backendData) return backendData

  const rates = Object.fromEntries(demoCurrencies.map((c) => [c.code, c.rateToUSD]))
  return simulateRequest(rates)
}

/**
 * GET ROUTES.DASHBOARD_SUMMARY
 * Fetch aggregate stats for the home dashboard.
 */
export async function fetchDashboardSummary() {
  const backendData = await tryFetch(ROUTES.DASHBOARD_SUMMARY)
  if (backendData) return backendData

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
