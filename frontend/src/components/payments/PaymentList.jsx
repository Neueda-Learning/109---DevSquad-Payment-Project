import StatusBadge from '../common/StatusBadge'
import { formatCurrency } from '../../utils/currency'
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
            <th>Sender</th>
            <th>Receiver</th>
            <th>Amount</th>
            <th>Date</th>
            <th>Status</th>
            <th>Description</th>
          </tr>
        </thead>

        <tbody>
          {payments.map((payment) => {
            const isIncoming =
              selectedUser?.accounts?.includes(
                payment.receiverAccountNumber
              )

            return (
              <tr key={payment.paymentId}>
                <td data-label="Invoice">
                  {payment.invoiceNumber}
                </td>

                <td data-label="Sender">
                  {payment.senderAccountNumber}
                </td>

                <td data-label="Receiver">
                  {payment.receiverAccountNumber}
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
                    payment.currencyCode || 'INR'
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
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

export default PaymentList