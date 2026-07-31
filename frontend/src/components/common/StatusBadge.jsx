import { getStatusLabel, getStatusTone } from '../../utils/format'
import './StatusBadge.css'

/** Small colored pill representing a payment/schedule status. */
function StatusBadge({ status }) {
  const tone = getStatusTone(status)
  return <span className={`status-badge status-badge-${tone}`}>{getStatusLabel(status)}</span>
}

export default StatusBadge
