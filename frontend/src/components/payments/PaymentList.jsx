import { Link } from 'react-router-dom'
import StatusBadge from '../common/StatusBadge'
import { CURRENCIES, formatCurrency } from '../../utils/currency'
import { formatDate } from '../../utils/format'
import './PaymentList.css'

function PaymentList({
  payments = [],
  selectedUser,
}) {
  if (!payments.length) return null

  return (
    <div className="payment-table-wrap">
      <table className="payment-table">
        <thead>
          <tr>
            <th>Invoice</th>
            <th>Amount</th>
            <th>Date</th>
            <th>Status</th>
            <th>Description</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {payments.map((payment) => {
            const isIncoming =
              selectedUser?.accounts?.includes(
                payment.receiverAccountNumber
              )

            const currency =
              CURRENCIES[payment.currencyId - 1]?.currency || 'INR'

            return (
              <tr key={payment.paymentId}>
                <td data-label="Invoice">
                  {payment.invoiceNumber}
                </td>

                <td
                  data-label="Amount"
                  className={
                    isIncoming
                      ? 'amount-credit'
                      : 'amount-debit'
                  }
                >
                  {isIncoming ? '+' : '-'}{' '}
                  {formatCurrency(
                    payment.amount,
                    currency
                  )}
                </td>

                <td data-label="Date">
                  {formatDate(payment.paymentDate)}
                </td>

                <td data-label="Status">
                  <StatusBadge
                    status={payment.status}
                  />
                </td>

                <td data-label="Description">
                  {payment.description}
                </td>

                <td data-label="Action">
                  <Link
                    to={`/payments/${payment.paymentId}`}
                    className="view-btn"
                  >
                    View
                  </Link>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

export default PaymentList