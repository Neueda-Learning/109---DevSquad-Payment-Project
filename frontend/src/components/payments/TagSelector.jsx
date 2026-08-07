import './TagSelector.css'

/**
 * TagSelector — renders a list of checkboxes for selecting payment tags.
 *
 * Props:
 *   availableTags  — string[]  — list of tag names fetched from the backend
 *   selectedTags   — string[]  — currently selected tag names
 *   onChange       — (tags: string[]) => void — called whenever selection changes
 *   loading        — boolean (optional)
 */
function TagSelector({ availableTags = [], selectedTags = [], onChange, loading = false }) {
  const toggle = (tag) => {
    if (selectedTags.includes(tag)) {
      onChange(selectedTags.filter((t) => t !== tag))
    } else {
      onChange([...selectedTags, tag])
    }
  }

  return (
    <div className="tag-selector">
      <span className="tag-selector__label">Select Tags</span>

      {loading && <span className="tag-selector__loading">Loading tags…</span>}

      {!loading && availableTags.length === 0 && (
        <span className="tag-selector__empty">No tags available.</span>
      )}

      {!loading && availableTags.length > 0 && (
        <div className="tag-selector__options">
          {availableTags.map((tag) => (
            <label key={tag} className="tag-selector__option">
              <input
                type="checkbox"
                checked={selectedTags.includes(tag)}
                onChange={() => toggle(tag)}
              />
              <span>{tag}</span>
            </label>
          ))}
        </div>
      )}

      {selectedTags.length > 0 && (
        <div className="tag-selector__selected">
          {selectedTags.map((tag) => (
            <span key={tag} className="tag-chip">
              {tag}
              <button
                type="button"
                className="tag-chip__remove"
                onClick={() => toggle(tag)}
                aria-label={`Remove ${tag}`}
              >
                ×
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}

export default TagSelector

