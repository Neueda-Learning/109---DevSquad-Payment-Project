import './Spinner.css'

/** Simple inline loading spinner + label used while demo API calls resolve. */
function Spinner({ label = 'Loading…' }) {
  return (
    <div className="spinner-wrap">
      <span className="spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  )
}

export default Spinner
