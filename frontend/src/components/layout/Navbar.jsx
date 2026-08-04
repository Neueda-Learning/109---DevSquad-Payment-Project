import { useState } from 'react'
import { NavLink, Link } from 'react-router-dom'
import './Navbar.css'

const links = [
  { to: '/', label: 'Home', end: true },
  { to: '/payments', label: 'Payments' },
  { to: '/payments/new', label: 'New Payment' },
  { to: '/scheduled', label: 'Scheduled' },
]

function Navbar({
  users = [],
  selectedUser,
  setSelectedUser,
}) {
  const [showUserMenu, setShowUserMenu] = useState(false)

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="navbar-brand">
          <span className="navbar-brand-mark">P</span>
          Paisa ye Paisa
        </Link>

        <nav className="navbar-links">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.end}
              className={({ isActive }) =>
                `navbar-link${isActive ? ' navbar-link-active' : ''}`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>

        <div className="navbar-right">
          <Link
            to="/payments/new"
            className="btn btn-primary navbar-cta"
          >
            + New Payment
          </Link>

          <div className="user-selector">
            <div
              className="user-avatar"
              onClick={() => setShowUserMenu(!showUserMenu)}
            >
              {selectedUser?.name?.charAt(0)?.toUpperCase() || 'U'}
            </div>

            {showUserMenu && (
              <div className="user-dropdown">
                <div className="user-dropdown-header">
                  {selectedUser?.name || 'Select User'}
                </div>

                {users.map((user) => (
                  <div
                    key={user.userId}
                    className={`user-dropdown-item ${
                      selectedUser?.userId === user.userId
                        ? 'active-user'
                        : ''
                    }`}
                    onClick={() => {
                      setSelectedUser(user)
                      setShowUserMenu(false)
                    }}
                  >
                    {user.name}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}

export default Navbar