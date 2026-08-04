import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import StatCard from '../components/common/StatCard'
import PaymentList from '../components/payments/PaymentList'
import Spinner from '../components/common/Spinner'
import { formatCurrency } from '../utils/currency'
import './HomePage.css'

function HomePage({ selectedUser }) {
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!selectedUser) return

    async function loadDashboard() {
      try {
        setLoading(true)

        const response = await fetch(
          'http://localhost:8080/api/v1/payments/all'
        )

        const allPayments =
          await response.json()

        const userAccounts =
          selectedUser.accounts || []

        const userPayments =
          allPayments.filter(
            (payment) =>
              userAccounts.includes(
                payment.senderAccountNumber
              ) ||
              userAccounts.includes(
                payment.receiverAccountNumber
              )
          )

        const totalPaid = userPayments
          .filter(
            (payment) =>
              payment.status ===
              'COMPLETED'
          )
          .reduce(
            (sum, payment) =>
              sum + payment.amount,
            0
          )

        const totalFailed =
          userPayments.filter(
            (payment) =>
              payment.status === 'FAILED'
          ).length

        const totalPending =
          userPayments.filter(
            (payment) =>
              payment.status ===
                'PENDING' ||
              payment.status ===
                'PROCESSING'
          ).length

        const upcomingScheduled = 0

        const recentPayments =
          [...userPayments]
            .sort(
              (a, b) =>
                new Date(
                  b.paymentDate
                ) -
                new Date(
                  a.paymentDate
                )
            )
            .slice(0, 5)

        setSummary({
          totalPaid,
          totalFailed,
          totalPending,
          upcomingScheduled,
          recentPayments,
        })
      } catch (error) {
        console.error(
          'Failed to load dashboard',
          error
        )
      } finally {
        setLoading(false)
      }
    }

    loadDashboard()
  }, [selectedUser])

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>
            Welcome{' '}
            {selectedUser?.name || ''}
          </h1>

          <p className="page-subtitle">
            Here's an overview of your
            payment activity.
          </p>
        </div>

        <div className="page-header-actions">
          <Link
            to="/payments/new"
            className="btn btn-primary"
          >
            + New Payment
          </Link>

          <Link
            to="/scheduled/new"
            className="btn btn-secondary"
          >
            Schedule Payment
          </Link>
        </div>
      </div>

      {loading || !summary ? (
        <Spinner label="Loading dashboard..." />
      ) : (
        <>
          <div className="stat-grid">
            <StatCard
              label="Total Paid"
              value={formatCurrency(
                summary.totalPaid,
                'INR'
              )}
              sublabel="Completed transactions"
              tone="success"
            />

            <StatCard
              label="Failed Payments"
              value={summary.totalFailed}
              sublabel="Need attention"
              tone="danger"
            />

            <StatCard
              label="Pending"
              value={summary.totalPending}
              sublabel="In progress"
              tone="warning"
            />

            <StatCard
              label="Scheduled"
              value={
                summary.upcomingScheduled
              }
              sublabel="Upcoming payments"
              tone="info"
            />
          </div>

          <div className="section-header">
            <h2>Recent Payments</h2>

            <Link
              to="/payments"
              className="link"
            >
              View all
            </Link>
          </div>

          <PaymentList
            payments={
              summary.recentPayments
            }
            selectedUser={selectedUser}
          />
        </>
      )}
    </div>
  )
}

export default HomePage