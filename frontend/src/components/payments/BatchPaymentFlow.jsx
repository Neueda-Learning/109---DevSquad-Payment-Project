import { useState } from 'react'
import { createBatchPayment, createBatchScheduledPayment } from '../../api/paymentApi'
import { formatCurrency, CURRENCIES } from '../../utils/currency'
import StatusBadge from '../common/StatusBadge'
import Spinner from '../common/Spinner'
import './BatchPaymentFlow.css'

let nextRowId = 1
const createEmptyRow = () => ({
  id: nextRowId++,
  receiverAccountNumber: '',
  amount: '',
  currency: CURRENCIES[0].currency,
  description: '',
})

/**
 * Multi-step Batch Payment flow: input recipients -> review summary -> submit -> result.
 */
function BatchPaymentFlow({ senderAccountNumber, paymentModeId = 1, paymentTiming = 'now' }) {
  const today = new Date().toISOString().split('T')[0]
  const [step, setStep] = useState('input') // 'input' | 'summary' | 'result'
  const [rows, setRows] = useState([createEmptyRow()])
  const [description, setDescription] = useState('')
  const [scheduledDate, setScheduledDate] = useState(today)
  const [validationError, setValidationError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [batchResult, setBatchResult] = useState(null)

  const updateRow = (id, field, value) => {
    setRows((prev) => prev.map((row) => (row.id === id ? { ...row, [field]: value } : row)))
  }

  const addRow = () => {
    setRows((prev) => [...prev, createEmptyRow()])
    console.log('[BatchPaymentFlow] Recipient row added')
  }

  const removeRow = (id) => {
    setRows((prev) => (prev.length > 1 ? prev.filter((row) => row.id !== id) : prev))
    console.log('[BatchPaymentFlow] Recipient row removed:', id)
  }

  const validateRows = () => {
    if (rows.some((row) => !row.receiverAccountNumber || !row.amount)) {
      return 'Receiver account number and amount are required for every recipient.'
    }
    if (rows.some((row) => Number(row.amount) <= 0)) {
      return 'Amount must be greater than zero for every recipient.'
    }

    if (paymentTiming === 'schedule') {
      if (!scheduledDate) {
        return 'Scheduled date is required for batch scheduled payments.'
      }
      if (scheduledDate < today) {
        return 'Scheduled date cannot be in the past.'
      }
    }

    return ''
  }

  const handleContinue = () => {
    console.log('[BatchPaymentFlow] Continue clicked. Current rows:', rows)

    const error = validateRows()
    if (error) {
      console.log('[BatchPaymentFlow] Validation failed:', error)
      setValidationError(error)
      return
    }

    setValidationError('')
    console.log('[BatchPaymentFlow] Validation passed. Moving to summary step.')
    setStep('summary')
  }

  const handleBack = () => {
    console.log('[BatchPaymentFlow] Navigating back to input step')
    setStep('input')
  }

  const totalsByCurrency = rows.reduce((totals, row) => {
    const currency = row.currency || CURRENCIES[0].currency
    totals[currency] = (totals[currency] || 0) + (Number(row.amount) || 0)
    return totals
  }, {})

  const handleConfirm = async () => {
    setSubmitting(true)
    setSubmitError('')

    const batchRequest = {
      senderAccountNumber,
      paymentModeId,
      description,
      ...(paymentTiming === 'schedule' ? { scheduledDate } : {}),
      recipients: rows.map((row) => ({
        receiverAccountNumber: Number(row.receiverAccountNumber),
        amount: Number(row.amount),
        currencyId: CURRENCIES.findIndex((c) => c.currency === row.currency) + 1,
        description: row.description,
      })),
    }

    console.log('[BatchPaymentFlow] Confirming batch payment. Request:', batchRequest)

    try {
      if (paymentTiming === 'schedule') {
        const result = await createBatchScheduledPayment(batchRequest)
        console.log('[BatchPaymentFlow] Batch scheduled payment created. Result:', result)
        setBatchResult({
          batchId: result.batchId,
          totalPayments: result.totalPayments,
          successfulPayments: 0,
          failedPayments: 0,
          results: [],
          scheduledDate: result.scheduledDate,
          status: result.status,
          isScheduled: true,
        })
      } else {
        const result = await createBatchPayment(batchRequest)
        console.log('[BatchPaymentFlow] Batch payment succeeded. Result:', result)
        setBatchResult(result)
      }

      setStep('result')
    } catch (error) {
      console.error('[BatchPaymentFlow] Batch payment submission error:', error)
      setSubmitError(error?.message || 'Failed to submit batch payment. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  if (step === 'result' && batchResult) {
    console.log('[BatchPaymentFlow] Rendering result step:', batchResult)

    return (
      <div className="batch-flow">
        <h2 className="batch-heading">
          {batchResult.isScheduled ? 'Batch Scheduled Payment Created' : 'Batch Payment Completed'}
        </h2>

        <div className="batch-summary-grid">
          <div>
            <span className="batch-label">Batch ID</span>
            <div className="batch-value">{batchResult.batchId}</div>
          </div>

          <div>
            <span className="batch-label">Total Payments</span>
            <div className="batch-value">{batchResult.totalPayments}</div>
          </div>

          {batchResult.isScheduled && (
            <div>
              <span className="batch-label">Scheduled Date</span>
              <div className="batch-value">{batchResult.scheduledDate}</div>
            </div>
          )}

          {batchResult.isScheduled && (
            <div>
              <span className="batch-label">Status</span>
              <div className="batch-value">{batchResult.status}</div>
            </div>
          )}

          {!batchResult.isScheduled && <div>
            <span className="batch-label">Successful</span>
            <div className="batch-value batch-value-success">
              {batchResult.successfulPayments ?? batchResult.successful ?? 0}
            </div>
          </div>}

          {!batchResult.isScheduled && <div>
            <span className="batch-label">Failed</span>
            <div className="batch-value batch-value-failed">
              {batchResult.failedPayments ?? batchResult.failed ?? 0}
            </div>
          </div>}
        </div>

        {!batchResult.isScheduled && <table className="batch-table">
          <thead>
            <tr>
              <th>Account</th>
              <th>Amount</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {batchResult.results.map((result, index) => (
              <tr key={`${result.receiverAccountNumber}-${index}`}>
                <td>{result.receiverAccountNumber}</td>
                <td>{formatCurrency(result.amount ?? 0, result.currency || CURRENCIES[0].currency)}</td>
                <td>
                  <StatusBadge status={result.status === 'SUCCESS' ? 'COMPLETED' : 'FAILED'} />
                  {(result.error || result.errorMessage) && (
                    <div className="batch-error">Error: {result.error || result.errorMessage}</div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>}
      </div>
    )
  }

  if (step === 'summary') {
    console.log('[BatchPaymentFlow] Rendering summary step. Rows:', rows)

    return (
      <div className="batch-flow">
        <h2 className="batch-heading">Batch Payment Summary</h2>

        <div className="batch-summary-grid">
          <div>
            <span className="batch-label">Sender Account</span>
            <div className="batch-value">{senderAccountNumber || '—'}</div>
          </div>

          <div>
            <span className="batch-label">Payment Timing</span>
            <div className="batch-value">
              {paymentTiming === 'schedule' ? 'Scheduled' : 'Pay Now'}
            </div>
          </div>

          <div>
            <span className="batch-label">Number of Payments</span>
            <div className="batch-value">{rows.length}</div>
          </div>

          <div>
            <span className="batch-label">Total Amount</span>
            <div className="batch-value">
              {Object.entries(totalsByCurrency).map(([currency, amount]) => (
                <div key={currency}>{formatCurrency(amount, currency)}</div>
              ))}
            </div>
          </div>
        </div>

        <table className="batch-table">
          <thead>
            <tr>
              <th>Account</th>
              <th>Amount</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id}>
                <td>{row.receiverAccountNumber}</td>
                <td>{formatCurrency(Number(row.amount) || 0, row.currency)}</td>
              </tr>
            ))}
          </tbody>
        </table>

        {submitError && <p className="batch-error">{submitError}</p>}

        {submitting ? (
          <Spinner label="Submitting batch payment…" />
        ) : (
          <div className="batch-actions">
            <button type="button" className="btn btn-ghost" onClick={handleBack}>
              Back to Edit
            </button>
            <button type="button" className="btn btn-primary" onClick={handleConfirm}>
              Confirm Batch Payment
            </button>
          </div>
        )}
      </div>
    )
  }

  console.log('[BatchPaymentFlow] Rendering input step. Rows:', rows)

  return (
    <div className="batch-flow">
      <label className="form-field">
        <span>Batch Description</span>
        <input
          className="input"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Optional note for this batch"
        />
      </label>

      {paymentTiming === 'schedule' && (
        <label className="form-field">
          <span>Scheduled Date</span>
          <input
            type="date"
            className="input"
            value={scheduledDate}
            min={today}
            onChange={(e) => setScheduledDate(e.target.value)}
          />
        </label>
      )}

      <table className="batch-table">
        <thead>
          <tr>
            <th>Receiver Account</th>
            <th>Amount</th>
            <th>Currency</th>
            <th>Description</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id}>
              <td>
                <input
                  className="input"
                  value={row.receiverAccountNumber}
                  onChange={(e) => updateRow(row.id, 'receiverAccountNumber', e.target.value)}
                  placeholder="Account number"
                />
              </td>
              <td>
                <input
                  className="input"
                  type="number"
                  value={row.amount}
                  onChange={(e) => updateRow(row.id, 'amount', e.target.value)}
                  placeholder="0.00"
                />
              </td>
              <td>
                <select
                  className="input"
                  value={row.currency}
                  onChange={(e) => updateRow(row.id, 'currency', e.target.value)}
                >
                  {CURRENCIES.map((currency) => (
                    <option key={currency.currency} value={currency.currency}>
                      {currency.currency} ({currency.symbol})
                    </option>
                  ))}
                </select>
              </td>
              <td>
                <input
                  className="input"
                  value={row.description}
                  onChange={(e) => updateRow(row.id, 'description', e.target.value)}
                  placeholder="Optional"
                />
              </td>
              <td>
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  onClick={() => removeRow(row.id)}
                  disabled={rows.length === 1}
                >
                  Remove
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <button type="button" className="btn btn-secondary batch-add-btn" onClick={addRow}>
        + Add Recipient
      </button>

      {validationError && <p className="batch-error">{validationError}</p>}

      <div className="batch-actions">
        <button type="button" className="btn btn-primary" onClick={handleContinue}>
          Continue
        </button>
      </div>
    </div>
  )
}

export default BatchPaymentFlow
