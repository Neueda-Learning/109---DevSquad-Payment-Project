import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <div className="page empty-state">
      <h1>404</h1>
      <p>The page you're looking for doesn't exist.</p>
      <Link to="/" className="btn btn-primary">
        Back to home
      </Link>
    </div>
  )
}

export default NotFoundPage
