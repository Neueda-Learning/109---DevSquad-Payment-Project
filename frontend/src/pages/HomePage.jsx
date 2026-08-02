import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import StatCard from '../components/common/StatCard'
import PaymentList from '../components/payments/PaymentList'
import Spinner from '../components/common/Spinner'
import { fetchDashboardSummary } from '../api/paymentApi'
import { formatCurrency } from '../utils/currency'
import './HomePage.css'

function HomePage() {
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    fetchDashboardSummary().then((data) => {
      if (active) {
        setSummary(data)
        setLoading(false)
      }
    })
    return () => {
      active = false
    }
  }, [])

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Welcome back</h1>
          <p className="page-subtitle">Here's an overview of your payment activity.</p>
        </div>
        <div className="page-header-actions">
          <Link to="/payments/new" className="btn btn-primary">
            + New Payment
          </Link>
          <Link to="/scheduled/new" className="btn btn-secondary">
            Schedule Payment
          </Link>
        </div>
      </div>

      {loading || !summary ? (
        <Spinner label="Loading dashboard…" />
      ) : (
        <>
          <div className="stat-grid">
            <StatCard
              label="Total paid"
              value={formatCurrency(summary.totalPaid, 'USD')}
              sublabel="Across all successful payments"
              tone="success"
            />
            <StatCard
              label="Failed payments"
              value={summary.totalFailed}
              sublabel="Need attention"
              tone="danger"
            />
            <StatCard
              label="Pending / processing"
              value={summary.totalPending}
              sublabel="In progress"
              tone="warning"
            />
            <StatCard
              label="Upcoming scheduled"
              value={summary.upcomingScheduled}
              sublabel="Scheduled payments"
              tone="info"
            />
          </div>

          <div className="section-header">
            <h2>Recent payments</h2>
            <Link to="/payments" className="link">
              View all
            </Link>
          </div>
          <PaymentList payments={summary.recentPayments} />
        </>
      )}
    </div>
  )
}

export default HomePage
