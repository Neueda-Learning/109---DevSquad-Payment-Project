import { useEffect, useState } from 'react'
import PaymentFilters from '../components/payments/PaymentFilters'
import PaymentList from '../components/payments/PaymentList'
import EmptyState from '../components/common/EmptyState'
import Spinner from '../components/common/Spinner'
import { fetchPayments, fetchVendors } from '../api/paymentApi'

const INITIAL_FILTERS = { search: '', vendor: '', status: '', tags: [] }

function PaymentsHistoryPage() {
  const [filters, setFilters] = useState(INITIAL_FILTERS)
  const [payments, setPayments] = useState([])
  const [vendors, setVendors] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchVendors().then(setVendors)
  }, [])

  useEffect(() => {
    let active = true
    async function load() {
      setLoading(true)
      const data = await fetchPayments(filters)
      if (active) {
        setPayments(data)
        setLoading(false)
      }
    }
    load()
    return () => {
      active = false
    }
  }, [filters])

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Payment history</h1>
          <p className="page-subtitle">Filter past payments by vendor, tag or status.</p>
        </div>
      </div>

      <PaymentFilters filters={filters} onChange={setFilters} vendors={vendors} />

      {loading ? (
        <Spinner label="Loading payments…" />
      ) : payments.length ? (
        <PaymentList payments={payments} />
      ) : (
        <EmptyState
          title="No payments found"
          message="Try adjusting your filters or search terms."
        />
      )}
    </div>
  )
}

export default PaymentsHistoryPage
