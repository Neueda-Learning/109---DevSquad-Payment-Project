import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import ScheduledPaymentsTabs from '../components/payments/ScheduledPaymentsTabs'
import NormalScheduledPayments from '../components/payments/NormalScheduledPayments'
import BatchScheduledPayments from '../components/payments/BatchScheduledPayments'
import {
  fetchNormalScheduledPayments,
  fetchBatchScheduledPayments,
  fetchBatchScheduleDetails,
  cancelScheduledPayment,
} from '../api/paymentApi'
import '../components/payments/PaymentList.css'

function ScheduledPaymentsPage() {
  const [activeTab, setActiveTab] = useState('normal')

  const [normalSchedules, setNormalSchedules] = useState([])
  const [normalLoading, setNormalLoading] = useState(false)
  const [normalError, setNormalError] = useState('')
  const [cancellingId, setCancellingId] = useState(null)

  const [batchSchedules, setBatchSchedules] = useState([])
  const [batchLoading, setBatchLoading] = useState(false)
  const [batchError, setBatchError] = useState('')
  const [batchLoaded, setBatchLoaded] = useState(false)

  const [expandedBatchId, setExpandedBatchId] = useState(null)
  const [batchDetailsById, setBatchDetailsById] = useState({})
  const [batchDetailsLoadingById, setBatchDetailsLoadingById] = useState({})
  const [batchDetailsErrorById, setBatchDetailsErrorById] = useState({})

  useEffect(() => {
    let active = true

    async function load() {
      setNormalLoading(true)
      setNormalError('')

      try {
        const data = await fetchNormalScheduledPayments()
        if (!active) return
        setNormalSchedules(data)
      } catch (error) {
        if (!active) return
        setNormalError(error?.message || 'Failed to load normal scheduled payments.')
      } finally {
        if (active) {
          setNormalLoading(false)
        }
      }
    }

    load()

    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    let active = true

    if (activeTab !== 'batch' || batchLoaded) {
      return () => {
        active = false
      }
    }

    async function loadBatch() {
      setBatchLoading(true)
      setBatchError('')

      try {
        const data = await fetchBatchScheduledPayments()
        if (!active) return
        setBatchSchedules(data)
        setBatchLoaded(true)
      } catch (error) {
        if (!active) return
        setBatchError(error?.message || 'Failed to load batch scheduled payments.')
      } finally {
        if (active) {
          setBatchLoading(false)
        }
      }
    }

    loadBatch()

    return () => {
      active = false
    }
  }, [activeTab, batchLoaded])

  const handleCancel = async (id) => {
    setCancellingId(id)

    try {
      await cancelScheduledPayment(id)
      setNormalSchedules((prev) => prev.filter((schedule) => schedule.scheduleId !== id))
    } catch (error) {
      setNormalError(error?.message || 'Failed to cancel scheduled payment.')
    } finally {
      setCancellingId(null)
    }
  }

  const handleToggleBatch = async (batchId) => {
    if (expandedBatchId === batchId) {
      setExpandedBatchId(null)
      return
    }

    setExpandedBatchId(batchId)

    if (batchDetailsById[batchId] || batchDetailsLoadingById[batchId]) {
      return
    }

    setBatchDetailsLoadingById((prev) => ({ ...prev, [batchId]: true }))
    setBatchDetailsErrorById((prev) => ({ ...prev, [batchId]: '' }))

    try {
      const details = await fetchBatchScheduleDetails(batchId)
      setBatchDetailsById((prev) => ({ ...prev, [batchId]: details }))
    } catch (error) {
      setBatchDetailsErrorById((prev) => ({
        ...prev,
        [batchId]: error?.message || 'Failed to load batch schedule details.',
      }))
    } finally {
      setBatchDetailsLoadingById((prev) => ({ ...prev, [batchId]: false }))
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Scheduled payments</h1>
          <p className="page-subtitle">View normal and batch scheduled payments separately.</p>
        </div>
        <Link to="/scheduled/new" className="btn btn-primary">
          + Schedule Payment
        </Link>
      </div>

      <ScheduledPaymentsTabs activeTab={activeTab} onChange={setActiveTab} />

      {activeTab === 'normal' ? (
        <NormalScheduledPayments
          loading={normalLoading}
          error={normalError}
          schedules={normalSchedules}
          cancellingId={cancellingId}
          onCancel={handleCancel}
        />
      ) : (
        <BatchScheduledPayments
          loading={batchLoading}
          error={batchError}
          batches={batchSchedules}
          expandedBatchId={expandedBatchId}
          detailsByBatchId={batchDetailsById}
          detailsLoadingByBatchId={batchDetailsLoadingById}
          detailsErrorByBatchId={batchDetailsErrorById}
          onToggleExpand={handleToggleBatch}
        />
      )}
    </div>
  )
}

export default ScheduledPaymentsPage
