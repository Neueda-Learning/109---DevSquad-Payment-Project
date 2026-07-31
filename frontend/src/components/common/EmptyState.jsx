import './EmptyState.css'

/** Generic centered placeholder shown for empty lists or loading states. */
function EmptyState({ title, message, action }) {
  return (
    <div className="empty-state">
      <h3>{title}</h3>
      {message && <p>{message}</p>}
      {action}
    </div>
  )
}

export default EmptyState
