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
  success: 'Successful',
  pending: 'Pending',
  processing: 'Processing',
  failed: 'Failed',
  refunded: 'Refunded',
  scheduled: 'Scheduled',
  paused: 'Paused',
  cancelled: 'Cancelled',
}

export const STATUS_TONES = {
  success: 'success',
  pending: 'warning',
  processing: 'info',
  failed: 'danger',
  refunded: 'neutral',
  scheduled: 'info',
  paused: 'warning',
  cancelled: 'neutral',
}

export function getStatusLabel(status) {
  return STATUS_LABELS[status] || status
}

export function getStatusTone(status) {
  return STATUS_TONES[status] || 'neutral'
}
