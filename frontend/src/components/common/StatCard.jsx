import './StatCard.css'

/** Dashboard summary tile. */
function StatCard({ label, value, sublabel, tone = 'neutral' }) {
  return (
    <div className={`stat-card stat-card-${tone}`}>
      <p className="stat-card-label">{label}</p>
      <p className="stat-card-value">{value}</p>
      {sublabel && <p className="stat-card-sublabel">{sublabel}</p>}
    </div>
  )
}

export default StatCard
