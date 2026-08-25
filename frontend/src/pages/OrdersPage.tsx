import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { fetchOrders } from '../api/order'
import { useAuth } from '../auth/auth-state'
import { SiteHeader } from '../components/SiteHeader'
import type { Order } from '../types/order'
import { currencyFormatter, formatImageUrl } from '../utils/catalog'
import {
  formatOrderDate,
  formatOrderStatus,
  formatOrderTitle,
  sumOrderQuantity,
} from '../utils/order'

export function OrdersPage() {
  const location = useLocation()
  const { accessToken, isInitializing, user } = useAuth()
  const [orders, setOrders] = useState<Order[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (!accessToken) {
      return
    }

    const token = accessToken
    const controller = new AbortController()

    async function loadOrders() {
      setIsLoading(true)
      setMessage('')

      try {
        const data = await fetchOrders(token, controller.signal)

        setOrders(data)
      } catch (error) {
        if (!controller.signal.aborted) {
          setMessage(readApiMessage(error, '주문 내역을 불러오지 못했습니다.'))
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      }
    }

    loadOrders()

    return () => controller.abort()
  }, [accessToken])

  const loginState = useMemo(() => {
    return { from: `${location.pathname}${location.search}` }
  }, [location.pathname, location.search])

  return (
    <div className="storefront">
      <SiteHeader />

      <main className="orders-page">
        <section className="orders-heading">
          <div>
            <p className="eyebrow">Orders</p>
            <h1>주문 내역</h1>
          </div>
          <Link to="/">계속 쇼핑하기</Link>
        </section>

        {isInitializing || isLoading ? (
          <OrdersSkeleton />
        ) : !user || !accessToken ? (
          <section className="cart-empty">
            <p className="eyebrow">Login required</p>
            <h2>로그인 후 주문 내역을 확인할 수 있습니다.</h2>
            <Link to="/login" state={loginState}>
              로그인하기
            </Link>
          </section>
        ) : orders.length > 0 ? (
          <section className="orders-list" aria-label="주문 목록">
            {orders.map((order) => (
              <article className="order-list-card" key={order.id}>
                <div className="order-list-thumbs" aria-hidden="true">
                  {order.items.slice(0, 3).map((item) => (
                    <div className="order-list-thumb" key={item.id}>
                      {item.thumbnailImageUrl ? (
                        <img
                          src={formatImageUrl(item.thumbnailImageUrl, 180)}
                          alt=""
                        />
                      ) : (
                        <span>PLAVOR</span>
                      )}
                    </div>
                  ))}
                </div>

                <div className="order-list-main">
                  <p className="eyebrow">{formatOrderDate(order.orderedAt)}</p>
                  <h2>{formatOrderTitle(order)}</h2>
                  <span>주문번호 {order.orderNumber}</span>
                  <ul className="order-list-items">
                    {order.items.slice(0, 2).map((item) => (
                      <li key={item.id}>
                        <span>{item.productName}</span>
                        <strong>{item.quantity}개</strong>
                      </li>
                    ))}
                    {order.items.length > 2 && (
                      <li>
                        <span>그 외 상품</span>
                        <strong>{order.items.length - 2}건</strong>
                      </li>
                    )}
                  </ul>
                </div>

                <div className="order-list-shipping">
                  <span>배송지</span>
                  <strong>{order.receiverName}</strong>
                  <small>
                    ({order.postalCode}) {order.address}
                  </small>
                </div>

                <div className="order-list-meta">
                  <span className="order-status-chip">
                    {formatOrderStatus(order.status)}
                  </span>
                  <strong>{currencyFormatter.format(order.totalAmount)}원</strong>
                  <span className="order-list-price-label">상품 금액</span>
                  <small>총 {sumOrderQuantity(order)}개</small>
                  <Link to={`/orders/${order.id}`}>상세 보기</Link>
                </div>
              </article>
            ))}
          </section>
        ) : (
          <section className="cart-empty">
            <p className="eyebrow">No orders</p>
            <h2>아직 주문 내역이 없습니다.</h2>
            <Link to="/">상품 보러가기</Link>
          </section>
        )}

        {message && <p className="orders-message">{message}</p>}
      </main>
    </div>
  )
}

function OrdersSkeleton() {
  return (
    <section className="orders-list">
      <div className="order-list-card skeleton-card" />
      <div className="order-list-card skeleton-card" />
    </section>
  )
}

function readApiMessage(error: unknown, fallbackMessage: string) {
  if (error instanceof ApiError) {
    return error.message
  }

  return fallbackMessage
}
