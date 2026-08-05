import { useEffect, useState } from 'react'
import './ThemeToggle.css'

const STORAGE_KEY = 'theme'

function getInitialIsDark() {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored) return stored === 'dark'
  return true
}

/** Day/night switch used to toggle the app's light/dark theme. */
function ThemeToggle() {
  const [isDark, setIsDark] = useState(getInitialIsDark)

  useEffect(() => {
    document.documentElement.setAttribute(
      'data-theme',
      isDark ? 'dark' : 'light'
    )
    localStorage.setItem(STORAGE_KEY, isDark ? 'dark' : 'light')
  }, [isDark])

  return (
    <label className="theme-switch" aria-label="Toggle Theme">
      <input
        type="checkbox"
        checked={isDark}
        onChange={(e) => setIsDark(e.target.checked)}
      />
      <div className="switch-bg">
        <div className="sky-stars">
          <div className="star star-1" />
          <div className="star star-2" />
          <div className="star star-3" />
          <div className="star star-4" />
        </div>
        <div className="sky-clouds">
          <div className="cloud cloud-1" />
          <div className="cloud cloud-2" />
        </div>
        <div className="sky-vault">
          <div className="sun" />
          <div className="moon">
            <div className="craters">
              <div className="crater crater-1" />
              <div className="crater crater-2" />
              <div className="crater crater-3" />
            </div>
          </div>
        </div>
        <div className="landscape">
          <div className="mountain mountain-1" />
          <div className="mountain mountain-2" />
          <div className="terrain" />
          <div className="tree tree-1" />
          <div className="tree tree-2" />
          <div className="tree tree-3" />
        </div>
      </div>
    </label>
  )
}

export default ThemeToggle
