// Centralised backend API route definitions.
// These paths are the contract the frontend expects the backend to implement.
// Update the base URL via VITE_API_BASE_URL once a real backend is available.

const rawBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '')

export const API_BASE_URL = rawBaseUrl.endsWith('/api/v1')
  ? rawBaseUrl
  : rawBaseUrl.endsWith('/api')
    ? `${rawBaseUrl}/v1`
    : `${rawBaseUrl}/api/v1`

export const ROUTES = {
  // Payments
  PAYMENTS: `${API_BASE_URL}/payments`,
  PAYMENT_BY_ID: (id) => `${API_BASE_URL}/payments/${id}`,
  PAYMENT_TAGS: `${API_BASE_URL}/payments/tags`,
  PAYMENT_RECEIPT: (id) => `${API_BASE_URL}/payments/${id}/receipt`,
  CREATE_PAYMENT: `${API_BASE_URL}/payments/create`,
  RETRY_PAYMENT: (id) => `${API_BASE_URL}/payments/${id}/retry`,
  BATCH_PAYMENT: `${API_BASE_URL}/payments/batch`,
  BATCH_SCHEDULE_PAYMENT: `${API_BASE_URL}/payments/batch/scheduled`,


  // Scheduled payments
  SCHEDULED_PAYMENTS: `${API_BASE_URL}/schedules`,
  SCHEDULE_PAYMENT: `${API_BASE_URL}/schedules`,
  CANCEL_SCHEDULED_PAYMENT: (id) => `${API_BASE_URL}/schedules/${id}`,
  BATCH_SCHEDULED_PAYMENTS: `${API_BASE_URL}/batch-schedules`,
  BATCH_SCHEDULED_PAYMENT_BY_ID: (batchId) => `${API_BASE_URL}/batch-schedules/${batchId}`,

  // Reference data
  VENDORS: `${API_BASE_URL}/vendors`,
  CURRENCIES: `${API_BASE_URL}/currencies`,
  EXCHANGE_RATES: `${API_BASE_URL}/currencies/rates`,

  // Dashboard
  DASHBOARD_SUMMARY: `${API_BASE_URL}/dashboard/summary`,
}

export default ROUTES
