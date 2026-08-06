import { useState , useEffect } from 'react'
import { CURRENCIES } from '../utils/currency'
import BatchPaymentFlow from '../components/payments/BatchPaymentFlow'
import PaymentLoader from '../components/payments/PaymentLoader'
import './NewPaymentPage.css'

function NewPaymentPage({
                          defaultTiming = 'now',
                          selectedUser,
                        }) {
  const [paymentMode, setPaymentMode] = useState('single')
  const [paymentType, setPaymentType] = useState('now')

  const [payment, setPayment] = useState({
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
  })

  const [selectedCurrency, setSelectedCurrency] = useState(
    CURRENCIES[1].currency
  )

  // Drives the payment loading animation: null | 'processing' | 'success' | 'error'
  const [loaderStatus, setLoaderStatus] = useState(null)
  const [loaderDone, setLoaderDone] = useState(false)


  useEffect(() => {
    if (selectedUser?.accounts?.length) {
      setPayment((prev) => ({
        ...prev,
        senderAccountNumber: selectedUser.accounts[0],
      }))
    }
  }, [selectedUser])

  const updatePayment = (field, value) => {
    setPayment((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

    const handleSubmit = async (e) => {
      e.preventDefault()

      // Guarantee the loader plays through its full
      // creating -> processing -> validating -> processing stages
      // even if the API responds (or fails) quickly.
      const minAnimationDelay = new Promise((resolve) =>
        setTimeout(resolve, 5200)
      )

      try {

        // Schedule payment will be handled separately
        if (paymentType === 'schedule') {

          console.log(
            "TODO: Implement schedule payment API"
          )

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

        // Wait for the same minimum delay so the animation isn't cut short.
        await minAnimationDelay

        setLoaderStatus('error')

      }
    }

    const closeLoader = () => {
      setLoaderStatus(null)
      setLoaderDone(false)
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

      <div className="form-card">

        {/* Payment Type */}

        <fieldset className="form-field">
          <legend>Payment Type</legend>

          <div className="radio-row">

            <label>
              <input
                type="radio"
                checked={paymentMode === 'single'}
                onChange={() => setPaymentMode('single')}
              />

              Single Payment
            </label>

            <label>
              <input
                type="radio"
                checked={paymentMode === 'batch'}
                onChange={() => setPaymentMode('batch')}
              />

              Batch Payment
            </label>

            <label>
              <input
                type="radio"
                checked={paymentMode === 'scheduled'}
                onChange={() => setPaymentMode('scheduled')}
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
              value={payment.amount}
              onChange={(e) =>
                updatePayment(
                  'amount',
                  Number(e.target.value)
                )
              }
            />
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

        {/* Schedule Fields */}

        {paymentType === 'schedule' && (

          <>
            <div className="form-row">

              <label className="form-field">
                <span>Frequency</span>

                <select className="input">
                  <option>One Time</option>
                  <option>Daily</option>
                  <option>Weekly</option>
                  <option>Monthly</option>
                  <option>Yearly</option>
                </select>
              </label>

              <label className="form-field">
                <span>Start Date</span>

                <input
                  type="date"
                  className="input"
                />
              </label>

            </div>

            <div className="form-row">

              <label className="form-field">
                <span>Time</span>

                <input
                  type="time"
                  className="input"
                />
              </label>

              <label className="form-field">
                <span>End Date (Optional)</span>

                <input
                  type="date"
                  className="input"
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
            Create Payment
          </button>
        </div>

      </form>
        )}
      </div>

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