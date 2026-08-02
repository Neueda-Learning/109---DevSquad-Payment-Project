import { AVAILABLE_TAGS } from '../../data/demoPayments'
import './TagChips.css'
import './PaymentFilters.css'

/**
 * Filter bar for the payment history page.
 * Filters by search text, vendor, tag/category and status.
 */
function PaymentFilters({ filters, onChange, vendors = [] }) {
  const handleField = (field) => (e) => onChange({ ...filters, [field]: e.target.value })

  const toggleTag = (tag) => {
    const current = filters.tags || []
    const next = current.includes(tag) ? current.filter((t) => t !== tag) : [...current, tag]
    onChange({ ...filters, tags: next })
  }

  const clearAll = () =>
    onChange({ search: '', vendor: '', status: '', tags: [] })

  const hasActiveFilters =
    filters.search || filters.vendor || filters.status || (filters.tags && filters.tags.length)

  return (
    <div className="filters">
      <div className="filters-row">
        <input
          type="search"
          className="input"
          placeholder="Search by vendor, reference or description…"
          value={filters.search}
          onChange={handleField('search')}
        />

        <select className="input" value={filters.vendor} onChange={handleField('vendor')}>
          <option value="">All vendors</option>
          {vendors.map((v) => (
            <option key={v.id} value={v.name}>
              {v.name}
            </option>
          ))}
        </select>

        <select className="input" value={filters.status} onChange={handleField('status')}>
          <option value="">All statuses</option>
          <option value="success">Successful</option>
          <option value="pending">Pending</option>
          <option value="processing">Processing</option>
          <option value="failed">Failed</option>
          <option value="refunded">Refunded</option>
        </select>

        {hasActiveFilters && (
          <button type="button" className="btn btn-ghost" onClick={clearAll}>
            Clear filters
          </button>
        )}
      </div>

      <div className="filters-tags">
        <span className="filters-tags-label">Tags:</span>
        {AVAILABLE_TAGS.map((tag) => (
          <button
            key={tag}
            type="button"
            className={
              'tag-chip tag-chip-button' +
              ((filters.tags || []).includes(tag) ? ' tag-chip-active' : '')
            }
            onClick={() => toggleTag(tag)}
          >
            {tag}
          </button>
        ))}
      </div>
    </div>
  )
}

export default PaymentFilters
