import { useEffect, useMemo, useState } from 'react'
import './PaymentsHistoryPage.css'
import PaymentList from '../components/payments/PaymentList'
import EmptyState from '../components/common/EmptyState'
import Spinner from '../components/common/Spinner'

function PaymentsHistoryPage({ selectedUser }) {
  const [payments, setPayments] = useState([])
  const [loading, setLoading] = useState(true)
  const [tags, setTags] = useState([])
  const [tagFilter, setTagFilter] = useState('ALL') // changed

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [searchTerm, setSearchTerm] = useState('')

  useEffect(() => {
    if (!selectedUser) return

    async function loadPayments() {
      try {
        setLoading(true)

        // payments
        const response = await fetch('http://localhost:8080/api/v1/payments/all')
        const allPayments = await response.json()

        // tags
        const tagsResponse = await fetch('http://localhost:8080/api/v1/tags/all')
        const tagsData = await tagsResponse.json()
        setTags(Array.isArray(tagsData) ? tagsData : [])

        const userAccounts = selectedUser.accounts || []
        const filteredPayments = (Array.isArray(allPayments) ? allPayments : []).filter(
          (payment) =>
            userAccounts.includes(payment.senderAccountNumber) ||
            userAccounts.includes(payment.receiverAccountNumber)
        )

        setPayments(filteredPayments)
      } catch (error) {
        console.error('Failed to fetch payments', error)
      } finally {
        setLoading(false)
      }
    }

    loadPayments()
  }, [selectedUser])

  const displayedPayments = useMemo(() => {
    return payments.filter((payment) => {
      const matchesStatus = statusFilter === 'ALL' || payment.status === statusFilter

      const matchesTag =
        tagFilter === 'ALL' ||
        payment.tag === tagFilter ||
        payment.tagName === tagFilter ||
        payment.tags?.includes?.(tagFilter)

      const search = searchTerm.toLowerCase()
      const matchesSearch =
        !search ||
        payment.invoiceNumber?.toLowerCase().includes(search) ||
        payment.description?.toLowerCase().includes(search)

      return matchesStatus && matchesTag && matchesSearch
    })
  }, [payments, statusFilter, tagFilter, searchTerm])

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Payment History</h1>
          <p className="page-subtitle">
            Showing payments for <strong>{selectedUser?.name}</strong>
          </p>
        </div>
      </div>

      {!loading && payments.length > 0 && (
        <div className="payment-filters">
          <input
            type="text"
            placeholder="Search invoice or description..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />

          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="ALL">All Status</option>
            <option value="COMPLETED">Completed</option>
            <option value="PENDING">Pending</option>
            <option value="FAILED">Failed</option>
          </select>

          {/* Replaced type filter with tag filter */}
          <select value={tagFilter} onChange={(e) => setTagFilter(e.target.value)}>
            <option value="ALL">All Tags</option>
            {tags.map((tag) => {
              const id = tag.tagId ?? tag.tag_id
              const name = tag.tagName ?? tag.tag_name
              return (
                <option key={id} value={name}>
                  {name}
                </option>
              )
            })}
          </select>
        </div>
      )}

      {loading ? (
        <Spinner label="Loading payments..." />
      ) : displayedPayments.length > 0 ? (
        <PaymentList payments={displayedPayments} selectedUser={selectedUser} />
      ) : (
        <EmptyState
          title="No payments found"
          message="No payments match the selected filters."
        />
      )}
    </div>
  )
}

export default PaymentsHistoryPage