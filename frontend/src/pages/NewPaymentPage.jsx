import { useState , useEffect } from 'react'
import { CURRENCIES } from '../utils/currency'
import { createSchedule } from '../api/paymentApi'
import BatchPaymentFlow from '../components/payments/BatchPaymentFlow'
import PaymentLoader from '../components/payments/PaymentLoader'
import './NewPaymentPage.css'

const PAYMENT_METHOD_OPTIONS = [
  { id: 1, label: '1 — UPI' },
  { id: 2, label: '2 — UPI' },
  { id: 3, label: '3 — Credit Card' },
  { id: 4, label: '4 — Bank Transfer' },
]

const SCHEDULE_FREQUENCY_OPTIONS = ['DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY']

function validateScheduleDraft(scheduleDraft) {
  if (!scheduleDraft.senderAccountNumber) {
    return 'Sender account number is required.'
  }

  if (!scheduleDraft.receiverAccountNumber) {
    return 'Receiver account number is required.'
  }

  if (scheduleDraft.senderAccountNumber === scheduleDraft.receiverAccountNumber) {
    return 'Sender and receiver account numbers must be different.'
  }

  if (!Number.isFinite(scheduleDraft.amount) || scheduleDraft.amount <= 0) {
    return 'Amount must be greater than zero.'
  }

  if (!scheduleDraft.currencyId) {
    return 'Currency is required.'
  }

  if (!scheduleDraft.paymentModeId) {
    return 'Payment mode is required.'
  }

  if (!scheduleDraft.frequency) {
    return 'Frequency is required.'
  }

  if (!scheduleDraft.startDate) {
    return 'Start date is required.'
  }

  if (scheduleDraft.endDate && scheduleDraft.endDate <= scheduleDraft.startDate) {
    return 'End date must be after the start date.'
  }

  return null
}

function NewPaymentPage({
                          defaultTiming = 'now',
                          selectedUser,
                        }) {
  const MAX_PAYMENT_AMOUNT = 1000000
  const HIGH_VALUE_CONFIRMATION_AMOUNT = 50000
  const [paymentMode, setPaymentMode] = useState('single')
  const [paymentType, setPaymentType] = useState('now')
  const [formResetKey, setFormResetKey] = useState(0)
  const [amountError, setAmountError] = useState('')
  const [showHighValueConfirmation, setShowHighValueConfirmation] = useState(false)
  const today = new Date().toISOString().split('T')[0]

  const [paymentMode, setPaymentMode] = useState(
    defaultTiming === 'schedule' ? 'scheduled' : 'single'
  )
  const [paymentType, setPaymentType] = useState(defaultTiming)

  const initialPaymentState = {
    senderAccountNumber: '',
    receiverAccountNumber: '',
    amount: '',
    currencyId: 1,
    paymentModeId: 1,
    paymentDate: '',
    paymentTime: '',
    description: '',
    scheduleId: null,
    batchId: null,
    status: 'CREATED',
  }

  const [payment, setPayment] = useState(initialPaymentState)
    frequency: 'DAILY',
    startDate: defaultTiming === 'schedule' ? today : '',
    endDate: '',
  })

  const [selectedCurrency, setSelectedCurrency] = useState(
    CURRENCIES[0].currency
  )

  // Drives the payment loading animation: null | 'processing' | 'success' | 'error'
  const [loaderStatus, setLoaderStatus] = useState(null)
  const [loaderDone, setLoaderDone] = useState(false)
  const [submitError, setSubmitError] = useState('')


  useEffect(() => {
    if (selectedUser?.accounts?.length) {
      setPayment((prev) => ({
        ...prev,
        senderAccountNumber: selectedUser.accounts[0],
      }))
    }
  }, [selectedUser])

  const updatePayment = (field, value) => {
    if (field === 'amount') {
      setAmountError('')
    }

    setPayment((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

    const handleSubmit = async (e) => {
      e.preventDefault()
      setSubmitError('')
  const resetPaymentForm = () => {
    setPaymentMode('single')
    setPaymentType('now')
    setSelectedCurrency(CURRENCIES[1].currency)
    setPayment({
      ...initialPaymentState,
      senderAccountNumber: selectedUser?.accounts?.[0] ?? '',
    })
    setAmountError('')
    setShowHighValueConfirmation(false)
    setFormResetKey((prev) => prev + 1)
    setLoaderStatus(null)
    setLoaderDone(false)
  }

    const submitPayment = async () => {
      // Guarantee the loader plays through its full
      // creating -> processing -> validating -> processing stages
      // even if the API responds (or fails) quickly.
      const minAnimationDelay = new Promise((resolve) =>
        setTimeout(resolve, 5200)
      )

      try {

        // Schedule payment will be handled separately
        if (paymentType === 'schedule') {

          const scheduleRequest = {
            senderAccountNumber: Number(payment.senderAccountNumber),
            receiverAccountNumber: Number(payment.receiverAccountNumber),
            amount: Number(payment.amount),
            currencyId: Number(payment.currencyId),
            paymentModeId: Number(payment.paymentModeId),
            description: payment.description.trim(),
            frequency: payment.frequency,
            startDate: payment.startDate,
            endDate: payment.endDate || null,
          }

          const validationError = validateScheduleDraft(scheduleRequest)
          if (validationError) {
            setSubmitError(validationError)
            return
          }

          setLoaderDone(false)
          setLoaderStatus('processing')

          const requestSchedule = createSchedule(scheduleRequest)

          const [createdSchedule] = await Promise.all([
            requestSchedule,
            minAnimationDelay,
          ])

          console.log('Schedule created:', createdSchedule)

          setLoaderStatus('success')

          return
        }


        // PAY NOW FLOW

        setLoaderDone(false)
        setLoaderStatus('processing')

        const now = new Date()

        const paymentRequest = {
          ...payment,

          paymentDate:
            now.toISOString().split('T')[0],

          paymentTime:
            now.toTimeString().split(' ')[0],

          status: "CREATED",

          scheduleId: null
        }


        console.log(
          "Sending payment:",
          paymentRequest
        )

        const requestPayment = fetch(
          `${import.meta.env.VITE_API_BASE_URL}/api/v1/payments/create`,
          {
            method: "POST",

            headers: {
              "Content-Type": "application/json",
            },

            body: JSON.stringify(paymentRequest),
          }
        ).then(async (response) => {
          if (!response.ok) {
            throw new Error(
              "Payment creation failed"
            )
          }

          return response.json()
        })

        const [createdPayment] = await Promise.all([
          requestPayment,
          minAnimationDelay,
        ])


        console.log(
          "Payment created:",
          createdPayment
        )


        setLoaderStatus('success')


      } catch (error) {

        console.error(
          "Payment submission error:",
          error
        )

        setSubmitError(
          error?.message || 'Unable to submit the payment. Please try again.'
        )

        // Wait for the same minimum delay so the animation isn't cut short.
        await minAnimationDelay
        setLoaderStatus('error')

      }
    }

    const handleSubmit = async (e) => {
      e.preventDefault()

      if (Number(payment.amount) > MAX_PAYMENT_AMOUNT) {
        setAmountError(
          `Maximum payment allowed is ${MAX_PAYMENT_AMOUNT.toLocaleString()}.`
        )
        return
      }

      setAmountError('')

      if (Number(payment.amount) > HIGH_VALUE_CONFIRMATION_AMOUNT) {
        setShowHighValueConfirmation(true)
        return
      }

      await submitPayment()
    }

    const confirmHighValuePayment = async () => {
      setShowHighValueConfirmation(false)
      await submitPayment()
    }

    const cancelHighValuePayment = () => {
      setShowHighValueConfirmation(false)
    }

    const formattedConfirmationAmount = `${
      CURRENCIES.find((c) => c.currency === selectedCurrency)?.symbol ?? ''
    }${Number(payment.amount || 0).toLocaleString()}`

    const closeLoader = () => {
      resetPaymentForm()
    }


  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>New Payment</h1>
          <p className="page-subtitle">
            Enter payment details below.
          </p>
        </div>
      </div>

      <div className="form-card" key={formResetKey}>

        {/* Payment Type */}

        <fieldset className="form-field">
          <legend>Payment Type</legend>

          <div className="radio-row">

            <label>
              <input
                type="radio"
                checked={paymentMode === 'single'}
                onChange={() => {
                  setPaymentMode('single')
                  setSubmitError('')
                }}
              />

              Single Payment
            </label>

            <label>
              <input
                type="radio"
                checked={paymentMode === 'batch'}
                onChange={() => {
                  setPaymentMode('batch')
                  setSubmitError('')
                }}
              />

              Batch Payment
            </label>

            <label>
              <input
                type="radio"
                checked={paymentMode === 'scheduled'}
                onChange={() => {
                  setPaymentMode('scheduled')
                  setPaymentType('schedule')
                  setSubmitError('')
                  setPayment((prev) => ({
                    ...prev,
                    startDate: prev.startDate || today,
                  }))
                }}
              />

              Scheduled Payment
            </label>

          </div>
        </fieldset>

        {paymentMode === 'batch' ? (
          <>
            <label className="form-field">
              <span>From Account</span>

              <select
                className="input"
                value={payment.senderAccountNumber}
                onChange={(e) =>
                  updatePayment(
                    'senderAccountNumber',
                    Number(e.target.value)
                  )
                }
              >
                {selectedUser?.accounts?.map((account) => (
                  <option key={account} value={account}>
                    {account}
                  </option>
                ))}
              </select>
            </label>

            <fieldset className="form-field">
              <legend>Payment Timing</legend>

              <div className="radio-row">

                <label>
                  <input
                    type="radio"
                    checked={paymentType === 'now'}
                    onChange={() => setPaymentType('now')}
                  />

                  Pay Now
                </label>

                <label>
                  <input
                    type="radio"
                    checked={paymentType === 'schedule'}
                    onChange={() => setPaymentType('schedule')}
                  />

                  Schedule Payment
                </label>

              </div>
            </fieldset>

            <BatchPaymentFlow
              senderAccountNumber={payment.senderAccountNumber}
              paymentTiming={paymentType}
            />
          </>
        ) : (

      <form onSubmit={handleSubmit}>

        {submitError && (
          <div className="form-alert" role="alert">
            {submitError}
          </div>
        )}

        {/* Sender Account */}

        <label className="form-field">
          <span>
            From Account
          </span>

          <select
            className="input"
            value={payment.senderAccountNumber}
            onChange={(e) =>
              updatePayment(
                'senderAccountNumber',
                Number(e.target.value)
              )
            }
          >
            {selectedUser?.accounts?.map((account) => (
              <option key={account} value={account}>
                {account}
              </option>
            ))}
          </select>
        </label>

        {/* Receiver */}

        <label className="form-field">
          <span>Receiver Account Number</span>

         <input
           className="input"
           value={payment.receiverAccountNumber}
           onChange={(e) =>
             updatePayment(
               'receiverAccountNumber',
               Number(e.target.value)
             )
           }
         />
        </label>

        {/* Amount */}

        <div className="form-row">

          <label className="form-field">
            <span>Amount</span>

            <input
              className="input"
              type="number"
              max={MAX_PAYMENT_AMOUNT}
              value={payment.amount}
              onChange={(e) =>
                updatePayment(
                  'amount',
                  Number(e.target.value)
                )
              }
            />
            {amountError && <small className="form-error">{amountError}</small>}
          </label>

          <label className="form-field">
            <span>Currency</span>

            <select
              className="input"
              value={selectedCurrency}
              onChange={(e) => {
                const currency = e.target.value

                setSelectedCurrency(currency)

                const index = CURRENCIES.findIndex(
                  (c) => c.currency === currency
                )

                setPayment((prev) => ({
                  ...prev,
                  currencyId: index + 1
                }))
              }}
            >
              {CURRENCIES.map((currency) => (
                <option
                  key={currency.currency}
                  value={currency.currency}
                >
                  {currency.currency} ({currency.symbol})
                </option>
              ))}
            </select>
          </label>

        </div>

        <label className="form-field">
          <span>Payment Mode</span>

          <select
            className="input"
            value={payment.paymentModeId}
            onChange={(e) =>
              updatePayment(
                'paymentModeId',
                Number(e.target.value)
              )
            }
          >
            {PAYMENT_METHOD_OPTIONS.map((option) => (
              <option key={option.id} value={option.id}>
                {option.label}
              </option>
            ))}
          </select>
        </label>

        {/* Description */}

        <label className="form-field">
          <span>Reason for Payment</span>

          <textarea
            className="input"
            value={payment.description}
            onChange={(e) =>
              updatePayment(
                'description',
                e.target.value
              )
            }
          />
        </label>

        {/* Payment Timing */}

        <fieldset className="form-field">
          <legend>Payment Timing</legend>

          <div className="radio-row">

            <label>
              <input
                type="radio"
                checked={paymentType === 'now'}
                    onChange={() => {
                      setPaymentType('now')
                      setSubmitError('')
                    }}
              />

              Pay Now
            </label>

            <label>
              <input
                type="radio"
                checked={paymentType === 'schedule'}
                    onChange={() => {
                      setPaymentType('schedule')
                      setSubmitError('')
                      setPayment((prev) => ({
                        ...prev,
                        startDate: prev.startDate || today,
                      }))
                    }}
              />

              Schedule Payment
            </label>

          </div>
        </fieldset>

        {/* Schedule Fields */}

        {paymentType === 'schedule' && (

          <>
            <div className="form-row">

              <label className="form-field">
                <span>Frequency</span>

                <select
                  className="input"
                  value={payment.frequency}
                  onChange={(e) => updatePayment('frequency', e.target.value)}
                >
                  {SCHEDULE_FREQUENCY_OPTIONS.map((option) => (
                    <option key={option} value={option}>
                      {option}
                    </option>
                  ))}
                </select>
              </label>

              <label className="form-field">
                <span>Start Date</span>

                <input
                  type="date"
                  className="input"
                  value={payment.startDate}
                  min={today}
                  onChange={(e) => updatePayment('startDate', e.target.value)}
                />
              </label>

            </div>

            <div className="form-row">
              <label className="form-field">
                <span>End Date (Optional)</span>

                <input
                  type="date"
                  className="input"
                  value={payment.endDate}
                  min={payment.startDate || today}
                  onChange={(e) => updatePayment('endDate', e.target.value)}
                />
              </label>

            </div>
          </>

        )}

        <div className="form-actions">
          <button
            type="submit"
            className="btn btn-primary"
          >
            {paymentType === 'schedule' ? 'Create Scheduled Payment' : 'Create Payment'}
          </button>
        </div>

      </form>
        )}
      </div>

      {showHighValueConfirmation && (
        <div className="popup-overlay">
          <div className="popup-card confirmation-card">
            <h2>Confirm Payment</h2>
            <p>
              This payment amount is {formattedConfirmationAmount}. Are you sure you want
              to continue?
            </p>

            <div className="popup-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={cancelHighValuePayment}
              >
                Cancel
              </button>

              <button
                type="button"
                className="btn btn-primary"
                onClick={confirmHighValuePayment}
              >
                Yes, Continue
              </button>
            </div>
          </div>
        </div>
      )}

      {loaderStatus && (
        <div className="popup-overlay">
          <div className="popup-card loader-card">
            <PaymentLoader
              status={loaderStatus}
              amount={`${
                CURRENCIES.find((c) => c.currency === selectedCurrency)?.symbol ?? ''
              }${payment.amount || '0'}`}
              senderName={payment.senderAccountNumber ? `Acct ${payment.senderAccountNumber}` : 'Sender'}
              receiverName={payment.receiverAccountNumber ? `Acct ${payment.receiverAccountNumber}` : 'Receiver'}
              onSettled={() => setLoaderDone(true)}
            />

            {loaderDone && (
              <button className="btn btn-primary" onClick={closeLoader}>
                {loaderStatus === 'success' ? 'Done' : 'Try Again'}
              </button>
            )}
          </div>
        </div>
      )}

    </div>
  )
}

export default NewPaymentPage