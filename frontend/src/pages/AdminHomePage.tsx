import { Link } from 'react-router-dom'
import { AdminAccessGate } from '../components/AdminAccessGate'
import { AdminBreadcrumb } from '../components/AdminNavigation'
import { SiteHeader } from '../components/SiteHeader'

export function AdminHomePage() {
  return (
    <div className="storefront">
      <SiteHeader />

      <main className="admin-page">
        <AdminAccessGate>
          <section className="admin-heading">
            <div>
              <AdminBreadcrumb current="홈" />
              <h1>관리자 홈</h1>
            </div>
          </section>

          <section className="admin-home-grid" aria-label="관리자 주요 메뉴">
            <Link className="admin-home-card" to="/admin/products">
              <span>Catalog</span>
              <strong>상품 목록</strong>
              <small>상품 등록, 수정, 판매 상태를 관리합니다.</small>
            </Link>

            <Link className="admin-home-card" to="/admin/orders">
              <span>Orders</span>
              <strong>주문 목록</strong>
              <small>주문 조회, 상세 확인, 배송 상태를 처리합니다.</small>
            </Link>
          </section>
        </AdminAccessGate>
      </main>
    </div>
  )
}
