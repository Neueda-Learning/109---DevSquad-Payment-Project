import './TagChips.css'

/** Small read-only chip list showing a payment's tags/categories. */
function TagChips({ tags = [] }) {
  if (!tags.length) return null
  return (
    <span className="tag-chip-group">
      {tags.map((tag) => (
        <span key={tag} className="tag-chip">
          {tag}
        </span>
      ))}
    </span>
  )
}

export default TagChips
