import { Link } from 'react-router-dom'
import StatusBadge from '../common/StatusBadge'
import EmptyState from '../common/EmptyState'
import Spinner from '../common/Spinner'
import { formatCurrency } from '../../utils/currency'
import { formatDate } from '../../utils/format'

function NormalScheduledPayments({ loading, error, schedules, cancellingId, onCancel }) {
  if (loading) {
    return <Spinner label="Loading normal scheduled payments..." />
  }

  if (error) {
    return <EmptyState title="Unable to load schedules" message={error} />
  }

  if (!schedules.length) {
    return (
      <EmptyState
        title="No normal scheduled payments"
        message="Create a scheduled payment to see it listed here."
        action={
          <Link to="/scheduled/new" className="btn btn-primary">
            Schedule a payment
          </Link>
        }
      />
    )
  }

  return (
    <div className="payment-table-wrap">
      <table className="payment-table">
        <thead>
          <tr>
            <th>Schedule ID</th>
            <th>Receiver</th>
            <th>Amount</th>
            <th>Currency</th>
            <th>Frequency</th>
            <th>Start Date</th>
            <th>Next Payment Date</th>
            <th>Status</th>
            <th aria-label="actions" />
          </tr>
        </thead>
        <tbody>
          {schedules.map((schedule) => (
            <tr key={schedule.scheduleId}>
              <td data-label="Schedule ID">{schedule.scheduleId}</td>
              <td data-label="Receiver">{schedule.receiverAccountNumber}</td>
              <td data-label="Amount">{formatCurrency(schedule.amount, schedule.currency)}</td>
              <td data-label="Currency">{schedule.currency}</td>
              <td data-label="Frequency">{schedule.frequency || '—'}</td>
              <td data-label="Start Date">{formatDate(schedule.startDate)}</td>
              <td data-label="Next Payment Date">{formatDate(schedule.nextRunDate)}</td>
              <td data-label="Status">
                <StatusBadge status={schedule.status} />
              </td>
              <td data-label="">
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  onClick={() => onCancel(schedule.scheduleId)}
                  disabled={cancellingId === schedule.scheduleId}
                >
                  {cancellingId === schedule.scheduleId ? 'Cancelling...' : 'Cancel'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default NormalScheduledPayments

