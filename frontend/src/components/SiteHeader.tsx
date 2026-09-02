import { Link } from 'react-router-dom'
import { useAuth } from '../auth/auth-state'

export function SiteHeader() {
  const { isInitializing, logout, user } = useAuth()

  return (
    <header className="site-header">
      <Link className="brand" to="/" aria-label="Plavor home">
        PLAVOR
      </Link>
      <div className="header-actions">
        {isInitializing ? (
          <span className="header-muted">Account</span>
        ) : user ? (
          <>
            <Link to="/members/me">{user.name}</Link>
            {user.role === 'ADMIN' && <Link to="/admin/products">Admin</Link>}
            <Link to="/orders">Orders</Link>
            <button className="header-text-button" type="button" onClick={logout}>
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/signup">Join</Link>
          </>
        )}
        <Link to="/cart">Cart</Link>
      </div>
    </header>
  )
}
