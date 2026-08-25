import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { fetchOrder } from '../api/order'
import { useAuth } from '../auth/auth-state'
import { SiteHeader } from '../components/SiteHeader'
import type { Order } from '../types/order'
import { currencyFormatter } from '../utils/catalog'
import {
  formatOrderDate,
  formatOrderStatus,
  formatOrderTitle,
  sumOrderQuantity,
} from '../utils/order'

type OrderCompleteLocationState = {
  orderNumber?: string
}

export function OrderCompletePage() {
  const { orderId } = useParams()
  const location = useLocation()
  const { accessToken, isInitializing, user } = useAuth()
  const [order, setOrder] = useState<Order | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [message, setMessage] = useState('')
  const state = location.state as OrderCompleteLocationState | null

  useEffect(() => {
    if (!accessToken || !orderId) {
      return
    }

    const token = accessToken
    const currentOrderId = orderId
    const controller = new AbortController()

    async function loadOrder() {
      setIsLoading(true)
      setMessage('')

      try {
        const data = await fetchOrder(token, currentOrderId, controller.signal)

        setOrder(data)
      } catch (error) {
        if (!controller.signal.aborted) {
          setMessage(readApiMessage(error, '주문 정보를 불러오지 못했습니다.'))
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      }
    }

    loadOrder()

    return () => controller.abort()
  }, [accessToken, orderId])

  const loginState = useMemo(() => {
    return { from: `${location.pathname}${location.search}` }
  }, [location.pathname, location.search])

  return (
    <div className="storefront">
      <SiteHeader />

      <main className="order-complete-page">
        {isInitializing || isLoading ? (
          <section className="order-complete-panel skeleton-card" />
        ) : !user || !accessToken ? (
          <section className="cart-empty">
            <p className="eyebrow">Login required</p>
            <h2>로그인 후 주문 결과를 확인할 수 있습니다.</h2>
            <Link to="/login" state={loginState}>
              로그인하기
            </Link>
          </section>
        ) : order ? (
          <section className="order-complete-panel">
            <p className="eyebrow">Order received</p>
            <h1>주문이 접수되었습니다.</h1>
            <p className="order-complete-copy">
              주문번호 {order.orderNumber}
            </p>

            <div className="order-complete-summary">
              <div>
                <span>주문 상품</span>
                <strong>{formatOrderTitle(order)}</strong>
              </div>
              <div>
                <span>주문 상태</span>
                <strong>{formatOrderStatus(order.status)}</strong>
              </div>
              <div>
                <span>주문 시각</span>
                <strong>{formatOrderDate(order.orderedAt)}</strong>
              </div>
              <div>
                <span>상품 수량</span>
                <strong>{sumOrderQuantity(order)}개</strong>
              </div>
              <div>
                <span>상품 금액</span>
                <strong>{currencyFormatter.format(order.totalAmount)}원</strong>
              </div>
            </div>

            <div className="order-complete-actions">
              <Link to={`/orders/${order.id}`}>주문 상세 보기</Link>
              <Link to="/orders">주문 내역</Link>
              <Link to="/">계속 쇼핑하기</Link>
            </div>
          </section>
        ) : (
          <section className="cart-empty">
            <p className="eyebrow">Order received</p>
            <h2>{message || '주문 결과를 확인하지 못했습니다.'}</h2>
            {state?.orderNumber && <p>주문번호 {state.orderNumber}</p>}
            <Link to="/orders">주문 내역으로 이동</Link>
          </section>
        )}
      </main>
    </div>
  )
}

function readApiMessage(error: unknown, fallbackMessage: string) {
  if (error instanceof ApiError) {
    return error.message
  }

  return fallbackMessage
}
