import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import CurrencySelect from '../components/payments/CurrencySelect'
import { RECURRENCE_OPTIONS } from '../data/demoScheduledPayments'
import { AVAILABLE_TAGS } from '../data/demoPayments'
import { fetchVendors, createPayment, schedulePayment } from '../api/paymentApi'
import { formatCurrency } from '../utils/currency'
import { formatDate } from '../utils/format'
import './NewPaymentPage.css'

const emptyForm = {
  vendor: '',
  tag: AVAILABLE_TAGS[0],
  description: '',
  amount: '',
  currency: 'USD',
  timing: 'now',
  scheduledFor: '',
  recurrence: 'none',
}

/**
 * Multi-step payment creation wizard:
 * 1. Enter details  2. Review & confirm  3. Confirmation
 * Also handles "schedule for later" when `defaultTiming` is 'schedule'.
 */
function NewPaymentPage({ defaultTiming = 'now' }) {
  const [step, setStep] = useState(1)
  const [form, setForm] = useState({ ...emptyForm, timing: defaultTiming })
  const [vendors, setVendors] = useState([])
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState(null)

  useEffect(() => {
    fetchVendors().then(setVendors)
  }, [])

  const updateField = (field) => (e) =>
    setForm((f) => ({ ...f, [field]: e.target.value }))

  const isScheduling = form.timing === 'schedule'
  const isStep1Valid = form.vendor && form.description && Number(form.amount) > 0 &&
    (!isScheduling || form.scheduledFor)

  const handleConfirm = async () => {
    setSubmitting(true)
    const payload = {
      vendor: form.vendor,
      tags: [form.tag],
      description: form.description,
      amount: Number(form.amount),
      currency: form.currency,
    }

    let created
    if (isScheduling) {
      created = await schedulePayment({
        ...payload,
        scheduledFor: new Date(form.scheduledFor).toISOString(),
        recurrence: form.recurrence,
      })
    } else {
      created = await createPayment(payload)
    }
    setResult({ ...created, isScheduling })
    setSubmitting(false)
    setStep(3)
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>{isScheduling ? 'Schedule a payment' : 'New payment'}</h1>
          <p className="page-subtitle">
            {step === 1 && 'Enter the payment details.'}
            {step === 2 && 'Review the payment before confirming.'}
            {step === 3 && 'Your payment has been submitted.'}
          </p>
        </div>
      </div>

      <ol className="wizard-steps">
        <li className={step >= 1 ? 'active' : ''}>1. Details</li>
        <li className={step >= 2 ? 'active' : ''}>2. Review</li>
        <li className={step >= 3 ? 'active' : ''}>3. Confirmation</li>
      </ol>

      {step === 1 && (
        <form
          className="form-card"
          onSubmit={(e) => {
            e.preventDefault()
            setStep(2)
          }}
        >
          <label className="form-field">
            <span>Vendor / Payee</span>
            <input
              className="input"
              list="vendor-options"
              value={form.vendor}
              onChange={updateField('vendor')}
              placeholder="Select or type a vendor name"
              required
            />
            <datalist id="vendor-options">
              {vendors.map((v) => (
                <option key={v.id} value={v.name} />
              ))}
            </datalist>
          </label>

          <label className="form-field">
            <span>Category / Tag</span>
            <select className="input" value={form.tag} onChange={updateField('tag')}>
              {AVAILABLE_TAGS.map((tag) => (
                <option key={tag} value={tag}>
                  {tag}
                </option>
              ))}
            </select>
          </label>

          <label className="form-field">
            <span>Description</span>
            <input
              className="input"
              value={form.description}
              onChange={updateField('description')}
              placeholder="What is this payment for?"
              required
            />
          </label>

          <div className="form-row">
            <label className="form-field">
              <span>Amount</span>
              <input
                className="input"
                type="number"
                min="0.01"
                step="0.01"
                value={form.amount}
                onChange={updateField('amount')}
                placeholder="0.00"
                required
              />
            </label>

            <label className="form-field">
              <span>Currency</span>
              <CurrencySelect value={form.currency} onChange={(v) => setForm((f) => ({ ...f, currency: v }))} />
            </label>
          </div>

          <fieldset className="form-field">
            <legend>When should this be paid?</legend>
            <div className="radio-row">
              <label>
                <input
                  type="radio"
                  name="timing"
                  value="now"
                  checked={form.timing === 'now'}
                  onChange={updateField('timing')}
                />
                Pay now
              </label>
              <label>
                <input
                  type="radio"
                  name="timing"
                  value="schedule"
                  checked={form.timing === 'schedule'}
                  onChange={updateField('timing')}
                />
                Schedule for later
              </label>
            </div>
          </fieldset>

          {isScheduling && (
            <div className="form-row">
              <label className="form-field">
                <span>Date & time</span>
                <input
                  className="input"
                  type="datetime-local"
                  value={form.scheduledFor}
                  onChange={updateField('scheduledFor')}
                  required
                />
              </label>
              <label className="form-field">
                <span>Repeat</span>
                <select className="input" value={form.recurrence} onChange={updateField('recurrence')}>
                  {RECURRENCE_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </label>
            </div>
          )}

          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={!isStep1Valid}>
              Continue to review
            </button>
          </div>
        </form>
      )}

      {step === 2 && (
        <div className="form-card">
          <dl className="detail-list">
            <div>
              <dt>Vendor</dt>
              <dd>{form.vendor}</dd>
            </div>
            <div>
              <dt>Category</dt>
              <dd>{form.tag}</dd>
            </div>
            <div>
              <dt>Description</dt>
              <dd>{form.description}</dd>
            </div>
            <div>
              <dt>Amount</dt>
              <dd>{formatCurrency(Number(form.amount) || 0, form.currency)}</dd>
            </div>
            {isScheduling && (
              <>
                <div>
                  <dt>Scheduled for</dt>
                  <dd>{formatDate(form.scheduledFor, { withTime: true })}</dd>
                </div>
                <div>
                  <dt>Repeats</dt>
                  <dd>{RECURRENCE_OPTIONS.find((o) => o.value === form.recurrence)?.label}</dd>
                </div>
              </>
            )}
          </dl>

          <div className="form-actions">
            <button type="button" className="btn btn-ghost" onClick={() => setStep(1)}>
              Back
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleConfirm}
              disabled={submitting}
            >
              {submitting ? 'Submitting…' : isScheduling ? 'Confirm schedule' : 'Confirm & pay'}
            </button>
          </div>
        </div>
      )}

      {step === 3 && result && (
        <div className="form-card confirmation-card">
          <span className="confirmation-icon" aria-hidden="true">
            ✓
          </span>
          <h2>{result.isScheduling ? 'Payment scheduled' : 'Payment submitted'}</h2>
          <p>
            {result.isScheduling
              ? `Your payment to ${form.vendor} has been scheduled for ${formatDate(form.scheduledFor, { withTime: true })}.`
              : `Your payment of ${formatCurrency(Number(form.amount) || 0, form.currency)} to ${form.vendor} is now ${result.status}.`}
          </p>
          <div className="form-actions">
            <Link to={result.isScheduling ? '/scheduled' : '/payments'} className="btn btn-primary">
              {result.isScheduling ? 'View scheduled payments' : 'View payment history'}
            </Link>
            <Link to="/" className="btn btn-ghost">
              Back to home
            </Link>
          </div>
        </div>
      )}
    </div>
  )
}

export default NewPaymentPage
