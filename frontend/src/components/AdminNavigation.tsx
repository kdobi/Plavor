import { Link } from 'react-router-dom'

type AdminSection = 'products' | 'orders'

export function AdminNavigation({ active }: { active: AdminSection }) {
  return (
    <nav className="admin-subnav" aria-label="관리자 메뉴">
      <Link className={active === 'products' ? 'active' : ''} to="/admin/products">
        상품 목록
      </Link>
      <Link className={active === 'orders' ? 'active' : ''} to="/admin/orders">
        주문 목록
      </Link>
    </nav>
  )
}

export function AdminBreadcrumb({ current }: { current: string }) {
  return (
    <nav className="admin-breadcrumb" aria-label="현재 위치">
      <Link to="/admin">관리자</Link>
      <span>/</span>
      <strong>{current}</strong>
    </nav>
  )
}
