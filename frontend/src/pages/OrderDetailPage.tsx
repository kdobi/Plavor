import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { fetchOrder } from '../api/order'
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

export function OrderDetailPage() {
  const { orderId } = useParams()
  const location = useLocation()
  const { accessToken, isInitializing, user } = useAuth()
  const [order, setOrder] = useState<Order | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [message, setMessage] = useState('')

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

      <main className="orders-page">
        <section className="orders-heading">
          <div>
            <p className="eyebrow">Order detail</p>
            <h1>주문 상세</h1>
          </div>
          <Link to="/orders">주문 내역</Link>
        </section>

        {isInitializing || isLoading ? (
          <OrderDetailSkeleton />
        ) : !user || !accessToken ? (
          <section className="cart-empty">
            <p className="eyebrow">Login required</p>
            <h2>로그인 후 주문 상세를 확인할 수 있습니다.</h2>
            <Link to="/login" state={loginState}>
              로그인하기
            </Link>
          </section>
        ) : order ? (
          <section className="order-detail-layout">
            <article className="order-detail-card">
              <div className="order-detail-head">
                <div>
                  <p className="eyebrow">{formatOrderDate(order.orderedAt)}</p>
                  <h2>{formatOrderTitle(order)}</h2>
                  <span>주문번호 {order.orderNumber}</span>
                </div>
                <span className="order-status-chip">
                  {formatOrderStatus(order.status)}
                </span>
              </div>

              <div className="order-progress" aria-label="주문 진행 상태">
                <div className="active">
                  <span />
                  <strong>주문 접수</strong>
                </div>
                <div>
                  <span />
                  <strong>상품 준비</strong>
                </div>
                <div>
                  <span />
                  <strong>배송 준비</strong>
                </div>
              </div>

              <div className="order-detail-overview">
                <div>
                  <span>주문일시</span>
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
                <div>
                  <span>수령자</span>
                  <strong>{order.receiverName}</strong>
                </div>
              </div>

              <div className="order-items">
                {order.items.map((item) => (
                  <div className="order-item-row" key={item.id}>
                    <Link className="order-item-thumb" to={`/products/${item.productId}`}>
                      {item.thumbnailImageUrl ? (
                        <img
                          src={formatImageUrl(item.thumbnailImageUrl, 220)}
                          alt={item.productName}
                        />
                      ) : (
                        'PLAVOR'
                      )}
                    </Link>
                    <div className="order-item-info">
                      <Link to={`/products/${item.productId}`}>{item.productName}</Link>
                      <span>{currencyFormatter.format(item.unitPrice)}원</span>
                    </div>
                    <div className="order-item-price">
                      <small>{item.quantity}개</small>
                      <strong>{currencyFormatter.format(item.totalPrice)}원</strong>
                    </div>
                  </div>
                ))}
              </div>
            </article>

            <aside className="order-side-card">
              <h2>주문 정보</h2>
              <dl>
                <div>
                  <dt>주문 상태</dt>
                  <dd>{formatOrderStatus(order.status)}</dd>
                </div>
                <div>
                  <dt>상품 수량</dt>
                  <dd>{sumOrderQuantity(order)}개</dd>
                </div>
                <div>
                  <dt>상품 금액</dt>
                  <dd>{currencyFormatter.format(order.totalAmount)}원</dd>
                </div>
              </dl>

              <h2>배송 정보</h2>
              <dl>
                <div>
                  <dt>수령자</dt>
                  <dd>{order.receiverName}</dd>
                </div>
                <div>
                  <dt>연락처</dt>
                  <dd>{order.receiverPhone}</dd>
                </div>
                <div>
                  <dt>주소</dt>
                  <dd>
                    ({order.postalCode}) {order.address}
                    {order.addressDetail ? ` ${order.addressDetail}` : ''}
                  </dd>
                </div>
                {order.deliveryMessage && (
                  <div>
                    <dt>요청사항</dt>
                    <dd>{order.deliveryMessage}</dd>
                  </div>
                )}
              </dl>
            </aside>
          </section>
        ) : (
          <section className="cart-empty">
            <p className="eyebrow">Not found</p>
            <h2>{message || '주문을 찾을 수 없습니다.'}</h2>
            <Link to="/orders">주문 내역으로 이동</Link>
          </section>
        )}
      </main>
    </div>
  )
}

function OrderDetailSkeleton() {
  return (
    <section className="order-detail-layout">
      <article className="order-detail-card skeleton-card" />
      <aside className="order-side-card skeleton-card" />
    </section>
  )
}

function readApiMessage(error: unknown, fallbackMessage: string) {
  if (error instanceof ApiError) {
    return error.message
  }

  return fallbackMessage
}
