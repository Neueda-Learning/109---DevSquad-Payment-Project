import { useEffect, useState } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import StatusBadge from '../components/common/StatusBadge'
import TagChips from '../components/payments/TagChips'
import PaymentFailureDetails from '../components/payments/PaymentFailureDetails'
import PaymentStatusTimeline from '../components/payments/PaymentStatusTimeline'
import ReceiptDownloadButton from '../components/payments/ReceiptDownloadButton'
import Spinner from '../components/common/Spinner'
import EmptyState from '../components/common/EmptyState'
import { fetchPaymentById, retryPayment } from '../api/paymentApi'
import { formatCurrency } from '../utils/currency'
import { formatDate } from '../utils/format'
import './PaymentDetailsPage.css'

function PaymentDetailsPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [payment, setPayment] = useState(null)
  const [loading, setLoading] = useState(true)
  const [retrying, setRetrying] = useState(false)

  useEffect(() => {
    let active = true
    async function load() {
      setLoading(true)
      const data = await fetchPaymentById(id)
      if (active) {
        setPayment(data)
        setLoading(false)
      }
    }
    load()
    return () => {
      active = false
    }
  }, [id])

  const handleRetry = async () => {
    setRetrying(true)
    await retryPayment(id)
    setRetrying(false)
    navigate('/payments')
  }

  if (loading) return <Spinner label="Loading payment…" />

  if (!payment) {
    return (
      <EmptyState
        title="Payment not found"
        message="This payment may have been removed."
        action={
          <Link to="/payments" className="btn btn-secondary">
            Back to payments
          </Link>
        }
      />
    )
  }

  return (
    <div className="page">
      <Link to="/payments" className="link back-link">
        ← Back to payments
      </Link>

      <div className="page-header">
        <div>
          <h1>{payment.reference}</h1>
          <p className="page-subtitle">{payment.description}</p>
        </div>
        <StatusBadge status={payment.status} />
      </div>

      <div className="detail-grid">
        <div className="detail-card">
          <h2>Payment details</h2>
          <dl className="detail-list">
            <div>
              <dt>Vendor</dt>
              <dd>{payment.vendor}</dd>
            </div>
            <div>
              <dt>Amount</dt>
              <dd>{formatCurrency(payment.amount, payment.currency)}</dd>
            </div>
            <div>
              <dt>Currency</dt>
              <dd>{payment.currency}</dd>
            </div>
            <div>
              <dt>Method</dt>
              <dd>{payment.method}</dd>
            </div>
            <div>
              <dt>Tags</dt>
              <dd>
                <TagChips tags={payment.tags} />
              </dd>
            </div>
            <div>
              <dt>Created</dt>
              <dd>{formatDate(payment.createdAt, { withTime: true })}</dd>
            </div>
          </dl>

          {payment.invoiceAvailable && <ReceiptDownloadButton paymentId={payment.id} />}

          {payment.status === 'failed' && (
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleRetry}
              disabled={retrying}
            >
              {retrying ? 'Retrying…' : 'Retry payment'}
            </button>
          )}
        </div>

        <div className="detail-card">
          <h2>Status</h2>
          <PaymentStatusTimeline payment={payment} />
          {payment.status === 'failed' && (
            <PaymentFailureDetails failureReason={payment.failureReason} />
          )}
        </div>
      </div>
    </div>
  )
}

export default PaymentDetailsPage
