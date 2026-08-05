import { useEffect, useState, useRef } from 'react'
import { Link, useParams } from 'react-router-dom'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import StatusBadge from '../components/common/StatusBadge'
import Spinner from '../components/common/Spinner'
import EmptyState from '../components/common/EmptyState'
import { formatCurrency } from '../utils/currency'
import { formatDate } from '../utils/format'
import './PaymentDetailsPage.css'

function PaymentDetailsPage() {
  const { id } = useParams()

  const [payment, setPayment] = useState(null)
  const [loading, setLoading] = useState(true)

  const invoiceRef = useRef(null)

  const apiUrl = import.meta.env.VITE_API_BASE_URL

  useEffect(() => {
    let active = true

    async function loadPayment() {
      try {
        setLoading(true)

        const response = await fetch(
          `${apiUrl}/api/v1/payments/${id}`
        )

        if (!response.ok) {
          throw new Error('Payment not found')
        }

        const data = await response.json()

        if (active) {
          setPayment(data)
        }
      } catch (error) {
        console.error(error)

        if (active) {
          setPayment(null)
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    loadPayment()

    return () => {
      active = false
    }
  }, [id, apiUrl])

  const downloadInvoice = async () => {
    try {
      const invoiceElement =
        invoiceRef.current

      if (!invoiceElement) return

      const canvas =
        await html2canvas(
          invoiceElement,
          {
            scale: 2,
            useCORS: true,
          }
        )

      const imgData =
        canvas.toDataURL('image/png')

      const pdf = new jsPDF(
        'p',
        'mm',
        'a4'
      )

      const pdfWidth =
        pdf.internal.pageSize.getWidth()

      const pdfHeight =
        (canvas.height * pdfWidth) /
        canvas.width

      pdf.addImage(
        imgData,
        'PNG',
        0,
        0,
        pdfWidth,
        pdfHeight
      )

      pdf.save(
        `Invoice-${payment.invoiceNumber}.pdf`
      )
    } catch (error) {
      console.error(
        'Invoice generation failed',
        error
      )
    }
  }

  if (loading) {
    return (
      <Spinner label="Loading payment..." />
    )
  }

  if (!payment) {
    return (
      <EmptyState
        title="Payment not found"
        message="This payment does not exist or may have been removed."
        action={
          <Link
            to="/payments"
            className="btn btn-secondary"
          >
            Back to Payments
          </Link>
        }
      />
    )
  }

  return (
    <div className="page">
     <Link
       to="/payments"
       className="btn btn-secondary"
     >
       ← Back to Payments
     </Link>

      <div className="page-header">
        <div>
          <h1>
            {payment.invoiceNumber}
          </h1>

          <p className="page-subtitle">
            {payment.description}
          </p>
        </div>

        <StatusBadge
          status={payment.status}
        />
      </div>

      <div className="detail-grid">
        <div className="detail-card">
          <h2>Payment Details</h2>

          <dl className="detail-list">

            <div>
              <dt>Invoice Number</dt>
              <dd>
                {payment.invoiceNumber}
              </dd>
            </div>

            <div>
              <dt>Amount</dt>
              <dd>
                {formatCurrency(
                  payment.amount,
                  payment.currencyCode ||
                    'INR'
                )}
              </dd>
            </div>

            <div>
              <dt>Status</dt>
              <dd>{payment.status}</dd>
            </div>

            <div>
              <dt>Payment Date</dt>
              <dd>
                {formatDate(
                  payment.paymentDate
                )}
              </dd>
            </div>

            <div>
              <dt>Payment Time</dt>
              <dd>
                {payment.paymentTime}
              </dd>
            </div>

            <div>
              <dt>Payment Mode</dt>
              <dd>
                {payment.paymentModeId}
              </dd>
            </div>

            <div>
              <dt>Sender Account</dt>
              <dd>
                {
                  payment.senderAccountNumber
                }
              </dd>
            </div>

            <div>
              <dt>Receiver Account</dt>
              <dd>
                {
                  payment.receiverAccountNumber
                }
              </dd>
            </div>

            <div>
              <dt>Description</dt>
              <dd>
                {payment.description}
              </dd>
            </div>
          </dl>

          <button
            className="btn btn-primary invoice-download-btn"
            onClick={downloadInvoice}
          >
            Download Invoice
          </button>
        </div>
      </div>

      <div
        ref={invoiceRef}
        className="invoice-template"
      >
        <div className="invoice-header">
          <h1>PAYMENT INVOICE</h1>
          <p>
            Payment Processing System
          </p>
        </div>

        <div className="invoice-row">
          <strong>
            Invoice Number
          </strong>
          <span>
            {payment.invoiceNumber}
          </span>
        </div>

        <div className="invoice-row">
          <strong>Payment ID</strong>
          <span>
            {payment.paymentId}
          </span>
        </div>

        <div className="invoice-row">
          <strong>Status</strong>
          <span>
            {payment.status}
          </span>
        </div>

        <div className="invoice-row">
          <strong>Amount</strong>
          <span>
            {formatCurrency(
              payment.amount,
              payment.currencyCode ||
                'INR'
            )}
          </span>
        </div>

        <div className="invoice-row">
          <strong>
            Payment Date
          </strong>
          <span>
            {formatDate(
              payment.paymentDate
            )}
          </span>
        </div>

        <div className="invoice-row">
          <strong>
            Payment Time
          </strong>
          <span>
            {payment.paymentTime}
          </span>
        </div>

        <div className="invoice-row">
          <strong>
            Sender Account
          </strong>
          <span>
            {
              payment.senderAccountNumber
            }
          </span>
        </div>

        <div className="invoice-row">
          <strong>
            Receiver Account
          </strong>
          <span>
            {
              payment.receiverAccountNumber
            }
          </span>
        </div>

        <div className="invoice-description">
          <strong>
            Description
          </strong>
          <p>
            {payment.description}
          </p>
        </div>

        <div className="invoice-footer">
          <p>
            This is a
            computer-generated
            invoice.
          </p>

          <p>
            Thank you for using our
            payment processing
            platform.
          </p>
        </div>
      </div>
    </div>
  )
}

export default PaymentDetailsPage