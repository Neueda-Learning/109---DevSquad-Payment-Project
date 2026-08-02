import { formatDate } from '../../utils/format'
import './PaymentFailureDetails.css'

/** Detailed failure breakdown shown when a payment's status is "failed". */
function PaymentFailureDetails({ failureReason }) {
  if (!failureReason) return null

  return (
    <div className="failure-panel">
      <div className="failure-panel-header">
        <span className="failure-icon" aria-hidden="true">
          ⚠
        </span>
        <div>
          <h3>Payment failed</h3>
          <p className="failure-code">Error code: {failureReason.code}</p>
        </div>
      </div>
      <p className="failure-message">{failureReason.message}</p>
      <p className="failure-detail">{failureReason.detail}</p>
      <p className="failure-timestamp">
        Occurred at {formatDate(failureReason.occurredAt, { withTime: true })}
      </p>
    </div>
  )
}

export default PaymentFailureDetails
