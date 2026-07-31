import { formatDate } from '../../utils/format'
import './PaymentStatusTimeline.css'

const STEPS = ['pending', 'processing', 'success']

/** Visual step tracker showing where a payment is in its lifecycle. */
function PaymentStatusTimeline({ payment }) {
  const isFailed = payment.status === 'failed'
  const isRefunded = payment.status === 'refunded'
  const currentIndex = isFailed
    ? 1
    : STEPS.indexOf(payment.status === 'refunded' ? 'success' : payment.status)

  return (
    <ol className="status-timeline">
      <li className={currentIndex >= 0 ? 'status-timeline-step done' : 'status-timeline-step'}>
        <span className="status-timeline-dot" />
        <div>
          <p className="status-timeline-title">Payment initiated</p>
          <p className="status-timeline-time">{formatDate(payment.createdAt, { withTime: true })}</p>
        </div>
      </li>
      <li
        className={
          'status-timeline-step' +
          (currentIndex >= 1 ? ' done' : '') +
          (isFailed ? ' failed' : '')
        }
      >
        <span className="status-timeline-dot" />
        <div>
          <p className="status-timeline-title">Processing</p>
          <p className="status-timeline-time">
            {isFailed ? 'Failed during processing' : 'Payment gateway processing'}
          </p>
        </div>
      </li>
      <li
        className={
          'status-timeline-step' +
          (currentIndex >= 2 && !isFailed ? ' done' : '') +
          (isFailed ? ' skipped' : '')
        }
      >
        <span className="status-timeline-dot" />
        <div>
          <p className="status-timeline-title">{isRefunded ? 'Completed & refunded' : 'Completed'}</p>
          <p className="status-timeline-time">
            {payment.completedAt
              ? formatDate(payment.completedAt, { withTime: true })
              : isFailed
                ? 'Not completed'
                : 'Awaiting completion'}
          </p>
        </div>
      </li>
    </ol>
  )
}

export default PaymentStatusTimeline
