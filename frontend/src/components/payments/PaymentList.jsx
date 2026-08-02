import { Link } from 'react-router-dom'
import StatusBadge from '../common/StatusBadge'
import TagChips from './TagChips'
import { formatCurrency } from '../../utils/currency'
import { formatDate } from '../../utils/format'
import './PaymentList.css'

/** Table of past payments, linking each row to its detail page. */
function PaymentList({ payments = [] }) {
  if (!payments.length) return null

  return (
    <div className="payment-table-wrap">
      <table className="payment-table">
        <thead>
          <tr>
            <th>Reference</th>
            <th>Vendor</th>
            <th>Tags</th>
            <th>Amount</th>
            <th>Date</th>
            <th>Status</th>
            <th aria-label="actions" />
          </tr>
        </thead>
        <tbody>
          {payments.map((payment) => (
            <tr key={payment.id}>
              <td data-label="Reference">
                <Link to={`/payments/${payment.id}`} className="payment-table-ref">
                  {payment.reference}
                </Link>
                <span className="payment-table-desc">{payment.description}</span>
              </td>
              <td data-label="Vendor">{payment.vendor}</td>
              <td data-label="Tags">
                <TagChips tags={payment.tags} />
              </td>
              <td data-label="Amount">{formatCurrency(payment.amount, payment.currency)}</td>
              <td data-label="Date">{formatDate(payment.createdAt)}</td>
              <td data-label="Status">
                <StatusBadge status={payment.status} />
              </td>
              <td data-label="">
                <Link to={`/payments/${payment.id}`} className="btn btn-ghost btn-sm">
                  View
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default PaymentList
