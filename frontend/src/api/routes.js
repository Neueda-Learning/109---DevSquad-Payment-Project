// Centralised backend API route definitions.
// These paths are the contract the frontend expects the backend to implement.
// Update the base URL via VITE_API_BASE_URL once a real backend is available.

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

export const ROUTES = {
  // Payments
  PAYMENTS: `${API_BASE_URL}/payments`,
  PAYMENT_BY_ID: (id) => `${API_BASE_URL}/payments/${id}`,
  PAYMENT_TAGS: `${API_BASE_URL}/payments/tags`,
  PAYMENT_RECEIPT: (id) => `${API_BASE_URL}/payments/${id}/receipt`,
  CREATE_PAYMENT: `${API_BASE_URL}/payments`,
  RETRY_PAYMENT: (id) => `${API_BASE_URL}/payments/${id}/retry`,


  // Scheduled payments
  SCHEDULED_PAYMENTS: `${API_BASE_URL}/v1/schedules/all`,
  SCHEDULE_PAYMENT: `${API_BASE_URL}/v1/schedules`,
  CANCEL_SCHEDULED_PAYMENT: (id) => `${API_BASE_URL}/v1/schedules/${id}`,

  // Reference data
  VENDORS: `${API_BASE_URL}/vendors`,
  CURRENCIES: `${API_BASE_URL}/currencies`,
  EXCHANGE_RATES: `${API_BASE_URL}/currencies/rates`,

  // Dashboard
  DASHBOARD_SUMMARY: `${API_BASE_URL}/dashboard/summary`,
}

export default ROUTES
