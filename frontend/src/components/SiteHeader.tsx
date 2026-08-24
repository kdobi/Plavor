import { Link } from 'react-router-dom'

export function SiteHeader() {
  return (
    <header className="site-header">
      <Link className="brand" to="/" aria-label="Plavor home">
        PLAVOR
      </Link>
      <nav className="top-nav" aria-label="Primary navigation">
        <Link to="/#products">Shop</Link>
        <Link to="/#new">New</Link>
        <Link to="/#journal">Journal</Link>
      </nav>
      <div className="header-actions">
        <Link to="/saved">Saved</Link>
        <Link to="/cart">Cart</Link>
      </div>
    </header>
  )
}
