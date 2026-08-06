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
  const [directionFilter, setDirectionFilter] = useState('ALL')
  const [searchTerm, setSearchTerm] = useState('')
  const [activeTab, setActiveTab] = useState('single')

  useEffect(() => {
    if (!selectedUser) {
      setPayments([])
      setTags([])
      setLoading(false)
      return
    }

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

  const getBatchId = (payment) => payment.batchId ?? payment.batch_id ?? null

  const userAccountSet = useMemo(
    () => new Set((selectedUser?.accounts || []).map((account) => String(account))),
    [selectedUser],
  )

  const getPaymentDirection = (payment) => {
    const senderAccount = String(payment.senderAccountNumber ?? '')
    const receiverAccount = String(payment.receiverAccountNumber ?? '')

    if (userAccountSet.has(senderAccount)) return 'DEBIT'
    if (userAccountSet.has(receiverAccount)) return 'CREDIT'
    return null
  }

  const getPaymentTimestamp = (payment) => {
    const datePart = payment.paymentDate || payment.createdAt || payment.created_at
    const timePart = payment.paymentTime || '00:00:00'

    if (!datePart) return 0

    const normalizedDate = String(datePart).split('T')[0]
    const normalizedTime = String(timePart).trim() || '00:00:00'
    const safeTime = normalizedTime.length === 5 ? `${normalizedTime}:00` : normalizedTime
    const timestamp = new Date(`${normalizedDate}T${safeTime}`).getTime()
    return Number.isNaN(timestamp) ? 0 : timestamp
  }

  const displayedPayments = useMemo(() => {
    return payments
      .filter((payment) => {
      const matchesStatus = statusFilter === 'ALL' || payment.status === statusFilter

      const paymentDirection = getPaymentDirection(payment)
      const matchesDirection =
        directionFilter === 'ALL' ||
        (directionFilter === 'DEBITS' && paymentDirection === 'DEBIT') ||
        (directionFilter === 'CREDITS' && paymentDirection === 'CREDIT')

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

      return matchesStatus && matchesDirection && matchesTag && matchesSearch
      })
      .sort((a, b) => getPaymentTimestamp(b) - getPaymentTimestamp(a))
  }, [payments, statusFilter, directionFilter, tagFilter, searchTerm, userAccountSet])

  const singlePayments = useMemo(
    () => displayedPayments.filter((payment) => !getBatchId(payment)),
    [displayedPayments],
  )

  const groupedPayments = useMemo(() => {
    const groups = displayedPayments.reduce((acc, payment) => {
      const batchId = getBatchId(payment)
      if (!batchId) return acc

      const groupKey = String(batchId)
      if (!acc[groupKey]) {
        acc[groupKey] = []
      }

      acc[groupKey].push(payment)
      return acc
    }, {})

    return Object.entries(groups).sort(
      ([, firstGroup], [, secondGroup]) =>
        getPaymentTimestamp(secondGroup[0]) - getPaymentTimestamp(firstGroup[0]),
    )
  }, [displayedPayments])

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
        <>
          <div className="history-tabs">
            <button
              type="button"
              className={`history-tab ${activeTab === 'single' ? 'active' : ''}`}
              onClick={() => setActiveTab('single')}
            >
              Single Payments ({singlePayments.length})
            </button>

            <button
              type="button"
              className={`history-tab ${activeTab === 'group' ? 'active' : ''}`}
              onClick={() => setActiveTab('group')}
            >
              Group Payments ({groupedPayments.length})
            </button>
          </div>

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

            <select value={directionFilter} onChange={(e) => setDirectionFilter(e.target.value)}>
              <option value="ALL">Both</option>
              <option value="DEBITS">Debits</option>
              <option value="CREDITS">Credits</option>
            </select>

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
        </>
      )}

      {loading ? (
        <Spinner label="Loading payments..." />
      ) : displayedPayments.length > 0 ? (
        activeTab === 'single' ? (
          singlePayments.length > 0 ? (
            <PaymentList payments={singlePayments} selectedUser={selectedUser} />
          ) : (
            <EmptyState
              title="No single payments found"
              message="No single payments match the selected filters."
            />
          )
        ) : groupedPayments.length > 0 ? (
          <div className="batch-groups">
            {groupedPayments.map(([batchId, batchPayments]) => (
              <section key={batchId} className="batch-group-card">
                <div className="batch-group-header">
                  <div>
                    <h2>Batch #{batchId}</h2>
                    <p>{batchPayments.length} payment(s) in this group</p>
                  </div>
                </div>

                <PaymentList payments={batchPayments} selectedUser={selectedUser} />
              </section>
            ))}
          </div>
        ) : (
          <EmptyState
            title="No group payments found"
            message="No batch payments match the selected filters."
          />
        )
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