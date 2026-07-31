import { NavLink, Link } from 'react-router-dom'
import './Navbar.css'

const links = [
  { to: '/', label: 'Home', end: true },
  { to: '/payments', label: 'Payments' },
  { to: '/payments/new', label: 'New Payment' },
  { to: '/scheduled', label: 'Scheduled' },
]

function Navbar() {
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
                'navbar-link' + (isActive ? ' navbar-link-active' : '')
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
        <Link to="/payments/new" className="btn btn-primary navbar-cta">
          + New Payment
        </Link>
      </div>
    </header>
  )
}

export default Navbar
