import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import StatusBadge from '../components/common/StatusBadge'
import TagChips from '../components/payments/TagChips'
import EmptyState from '../components/common/EmptyState'
import Spinner from '../components/common/Spinner'
import { fetchScheduledPayments, cancelScheduledPayment } from '../api/paymentApi'
import { formatCurrency } from '../utils/currency'
import { formatDate } from '../utils/format'
import { RECURRENCE_OPTIONS } from '../data/demoScheduledPayments'
import '../components/payments/PaymentList.css'

function recurrenceLabel(value) {
  return RECURRENCE_OPTIONS.find((o) => o.value === value)?.label || value
}

function ScheduledPaymentsPage() {
  const [scheduled, setScheduled] = useState([])
  const [loading, setLoading] = useState(true)
  const [cancellingId, setCancellingId] = useState(null)

  useEffect(() => {
    let active = true
    async function load() {
      setLoading(true)
      const data = await fetchScheduledPayments()
      if (active) {
        setScheduled(data)
        setLoading(false)
      }
    }
    load()
    return () => {
      active = false
    }
  }, [])

  const handleCancel = async (id) => {
    setCancellingId(id)
    await cancelScheduledPayment(id)
    setScheduled((prev) => prev.filter((s) => s.id !== id))
    setCancellingId(null)
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Scheduled payments</h1>
          <p className="page-subtitle">Upcoming and recurring payments waiting to run.</p>
        </div>
        <Link to="/scheduled/new" className="btn btn-primary">
          + Schedule Payment
        </Link>
      </div>

      {loading ? (
        <Spinner label="Loading scheduled payments…" />
      ) : scheduled.length ? (
        <div className="payment-table-wrap">
          <table className="payment-table">
            <thead>
              <tr>
                <th>Vendor</th>
                <th>Tags</th>
                <th>Amount</th>
                <th>Scheduled for</th>
                <th>Repeats</th>
                <th>Status</th>
                <th aria-label="actions" />
              </tr>
            </thead>
            <tbody>
              {scheduled.map((s) => (
                <tr key={s.id}>
                  <td data-label="Vendor">
                    <span className="payment-table-ref">{s.vendor}</span>
                    <span className="payment-table-desc">{s.description}</span>
                  </td>
                  <td data-label="Tags">
                    <TagChips tags={s.tags} />
                  </td>
                  <td data-label="Amount">{formatCurrency(s.amount, s.currency)}</td>
                  <td data-label="Scheduled for">{formatDate(s.scheduledFor, { withTime: true })}</td>
                  <td data-label="Repeats">{recurrenceLabel(s.recurrence)}</td>
                  <td data-label="Status">
                    <StatusBadge status={s.status} />
                  </td>
                  <td data-label="">
                    <button
                      type="button"
                      className="btn btn-ghost btn-sm"
                      onClick={() => handleCancel(s.id)}
                      disabled={cancellingId === s.id}
                    >
                      {cancellingId === s.id ? 'Cancelling…' : 'Cancel'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState
          title="No scheduled payments"
          message="Schedule a payment to see it listed here."
          action={
            <Link to="/scheduled/new" className="btn btn-primary">
              Schedule a payment
            </Link>
          }
        />
      )}
    </div>
  )
}

export default ScheduledPaymentsPage
