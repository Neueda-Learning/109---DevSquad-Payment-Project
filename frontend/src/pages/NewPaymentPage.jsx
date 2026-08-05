import { useState , useEffect } from 'react'
import { CURRENCIES } from '../utils/currency'
import './NewPaymentPage.css'

function NewPaymentPage({
                          defaultTiming = 'now',
                          selectedUser,
                        }) {
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

  const [popup, setPopup] = useState({
    show: false,
    success: true,
    title: '',
    message: '',
  })


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

      try {

        // Schedule payment will be handled separately
        if (paymentType === 'schedule') {

          console.log(
            "TODO: Implement schedule payment API"
          )

          return
        }


        // PAY NOW FLOW

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


        const response = await fetch(
          `${import.meta.env.VITE_API_BASE_URL}/api/v1/payments/create`,
          {
            method: "POST",

            headers: {
              "Content-Type": "application/json",
            },

            body: JSON.stringify(paymentRequest),
          }
        )


        if (!response.ok) {
          throw new Error(
            "Payment creation failed"
          )
        }


        const createdPayment =
          await response.json()


        console.log(
          "Payment created:",
          createdPayment
        )


        setPopup({
          show: true,
          success: true,
          title: 'Payment Successful',
          message: 'Your payment has been created successfully.',
        })


      } catch (error) {

        console.error(
          "Payment submission error:",
          error
        )

        setPopup({
          show: true,
          success: false,
          title: 'Payment Failed',
          message: 'Unable to create payment. Please try again.',
        })

      }
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

      <form className="form-card" onSubmit={handleSubmit}>

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

      {popup.show && (
        <div className="popup-overlay">
          <div className="popup-card">
            <div
              className={
                popup.success
                  ? 'popup-icon success'
                  : 'popup-icon error'
              }
            >
              {popup.success ? '✓' : '✕'}
            </div>

            <h2>{popup.title}</h2>

            <p>{popup.message}</p>

            <button
              className="btn btn-primary"
              onClick={() =>
                setPopup({
                  ...popup,
                  show: false,
                })
              }
            >
              OK
            </button>
          </div>
        </div>
      )}

    </div>
  )
}

export default NewPaymentPage