// Formatting helpers for dates and payment status display.

export function formatDate(isoString, options) {
  if (!isoString) return '—'

  const date = new Date(isoString)

  return date.toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: options?.withTime ? 'short' : undefined,
  })
}

export const STATUS_LABELS = {
  COMPLETED: 'Completed',
  PENDING: 'Pending',
  PROCESSING: 'Processing',
  FAILED: 'Failed',
  REFUNDED: 'Refunded',
  SCHEDULED: 'Scheduled',
  PAUSED: 'Paused',
  CANCELLED: 'Cancelled',
}

export const STATUS_TONES = {
  COMPLETED: 'success',
  PENDING: 'warning',
  PROCESSING: 'info',
  FAILED: 'danger',
  REFUNDED: 'neutral',
  SCHEDULED: 'info',
  PAUSED: 'warning',
  CANCELLED: 'neutral',
}

export function getStatusLabel(status) {
  return STATUS_LABELS[status?.toUpperCase()] || status
}

export function getStatusTone(status) {
  return STATUS_TONES[status?.toUpperCase()] || 'neutral'
}