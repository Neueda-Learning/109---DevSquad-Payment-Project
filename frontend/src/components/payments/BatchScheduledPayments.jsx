import { Fragment } from 'react'
import StatusBadge from '../common/StatusBadge'
import EmptyState from '../common/EmptyState'
import Spinner from '../common/Spinner'
import { formatCurrency } from '../../utils/currency'
import { formatDate } from '../../utils/format'

function formatTotalAmount(value) {
  const amount = Number(value || 0)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

function BatchScheduledPayments({
  loading,
  error,
  batches,
  expandedBatchId,
  detailsByBatchId,
  detailsLoadingByBatchId,
  detailsErrorByBatchId,
  onToggleExpand,
}) {
  if (loading) {
    return <Spinner label="Loading batch scheduled payments..." />
  }

  if (error) {
    return <EmptyState title="Unable to load batch schedules" message={error} />
  }

  if (!batches.length) {
    return (
      <EmptyState
        title="No batch scheduled payments"
        message="Create a batch scheduled payment to see it listed here."
      />
    )
  }

  return (
    <div className="payment-table-wrap">
      <table className="payment-table">
        <thead>
          <tr>
            <th>Batch ID</th>
            <th>Total Recipients</th>
            <th>Total Amount</th>
            <th>Scheduled Date</th>
            <th>Created Date</th>
            <th>Status</th>
            <th aria-label="actions" />
          </tr>
        </thead>
        <tbody>
          {batches.map((batch) => {
            const isExpanded = expandedBatchId === batch.batchId
            const details = detailsByBatchId[batch.batchId]
            const loadingDetails = detailsLoadingByBatchId[batch.batchId]
            const detailsError = detailsErrorByBatchId[batch.batchId]

            return (
              <Fragment key={batch.batchId}>
                <tr>
                  <td data-label="Batch ID">{batch.batchId}</td>
                  <td data-label="Total Recipients">{batch.totalRecipients}</td>
                  <td data-label="Total Amount">{formatTotalAmount(batch.totalAmount)}</td>
                  <td data-label="Scheduled Date">{formatDate(batch.scheduledDate)}</td>
                  <td data-label="Created Date">{formatDate(batch.createdAt, { withTime: true })}</td>
                  <td data-label="Status">
                    <StatusBadge status={batch.status} />
                  </td>
                  <td data-label="">
                    <button
                      type="button"
                      className="btn btn-ghost btn-sm"
                      onClick={() => onToggleExpand(batch.batchId)}
                    >
                      {isExpanded ? 'Hide Payments' : 'View Payments'}
                    </button>
                  </td>
                </tr>

                {isExpanded && (
                  <tr>
                    <td colSpan={7}>
                      {loadingDetails ? (
                        <Spinner label="Loading batch payment details..." />
                      ) : detailsError ? (
                        <p className="batch-error">{detailsError}</p>
                      ) : (
                        <div>
                          <strong>Payments</strong>
                          <table className="batch-table" style={{ marginTop: '10px' }}>
                            <thead>
                              <tr>
                                <th>Receiver</th>
                                <th>Amount</th>
                                <th>Currency</th>
                                <th>Description</th>
                              </tr>
                            </thead>
                            <tbody>
                              {(details?.payments || []).map((payment, index) => (
                                <tr key={`${batch.batchId}-payment-${index}`}>
                                  <td>{payment.receiverAccountNumber}</td>
                                  <td>{formatCurrency(payment.amount, payment.currency)}</td>
                                  <td>{payment.currency}</td>
                                  <td>{payment.description || '—'}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      )}
                    </td>
                  </tr>
                )}
              </Fragment>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

export default BatchScheduledPayments


