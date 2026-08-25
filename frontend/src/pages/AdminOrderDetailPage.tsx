import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  fetchAdminOrder,
  updateAdminOrderStatus,
} from '../api/admin'
import { ApiError } from '../api/auth'
import { useAuth } from '../auth/auth-state'
import { AdminAccessGate } from '../components/AdminAccessGate'
import { SiteHeader } from '../components/SiteHeader'
import type { AdminOrder } from '../types/admin'
import type { OrderStatus } from '../types/order'
import { currencyFormatter, formatImageUrl } from '../utils/catalog'
import {
  formatOrderDate,
  formatOrderStatus,
  formatOrderTitle,
  sumOrderQuantity,
} from '../utils/order'

const ORDER_STATUS_OPTIONS: Array<{ label: string; value: OrderStatus }> = [
  { label: '주문 접수', value: 'CREATED' },
  { label: '결제 완료', value: 'PAID' },
  { label: '배송 준비', value: 'PREPARING' },
  { label: '배송 중', value: 'SHIPPED' },
  { label: '배송 완료', value: 'DELIVERED' },
  { label: '주문 취소', value: 'CANCELED' },
]

export function AdminOrderDetailPage() {
  const { orderId } = useParams()
  const { accessToken, user } = useAuth()
  const [order, setOrder] = useState<AdminOrder | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isUpdating, setIsUpdating] = useState(false)
  const [message, setMessage] = useState('')

  const canLoad = Boolean(accessToken && user?.role === 'ADMIN' && orderId)

  useEffect(() => {
    if (!accessToken || !orderId || !canLoad) {
      return
    }

    const token = accessToken
    const currentOrderId = orderId
    const controller = new AbortController()

    async function loadOrder() {
      setIsLoading(true)
      setMessage('')

      try {
        const data = await fetchAdminOrder(token, currentOrderId, controller.signal)

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
  }, [accessToken, canLoad, orderId])

  async function handleStatusChange(status: OrderStatus) {
    if (!accessToken || !order || order.status === status) {
      return
    }

    setIsUpdating(true)
    setMessage('')

    try {
      const updatedOrder = await updateAdminOrderStatus(accessToken, order.id, {
        status,
      })

      setOrder(updatedOrder)
    } catch (error) {
      setMessage(readApiMessage(error, '주문 상태를 변경하지 못했습니다.'))
    } finally {
      setIsUpdating(false)
    }
  }

  return (
    <div className="storefront">
      <SiteHeader />

      <main className="admin-page">
        <AdminAccessGate>
          <section className="admin-heading">
            <div>
              <p className="eyebrow">Admin Order Detail</p>
              <h1>주문 상세 관리</h1>
            </div>
            <Link className="admin-secondary-link" to="/admin/orders">
              목록으로
            </Link>
          </section>

          {isLoading ? (
            <section className="order-detail-layout">
              <article className="order-detail-card skeleton-card" />
              <aside className="order-side-card skeleton-card" />
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

                <div className="order-detail-overview">
                  <div>
                    <span>고객</span>
                    <strong>{order.memberName}</strong>
                  </div>
                  <div>
                    <span>주문 상품</span>
                    <strong>{sumOrderQuantity(order)}개</strong>
                  </div>
                  <div>
                    <span>상품 금액</span>
                    <strong>{currencyFormatter.format(order.totalAmount)}원</strong>
                  </div>
                  <div>
                    <span>최근 수정</span>
                    <strong>{formatOrderDate(order.updatedAt)}</strong>
                  </div>
                </div>

                <div className="order-items">
                  {order.items.map((item) => (
                    <div className="order-item-row" key={item.id}>
                      <Link
                        className="order-item-thumb"
                        to={`/products/${item.productId}`}
                      >
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
                        <Link to={`/products/${item.productId}`}>
                          {item.productName}
                        </Link>
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

              <aside className="order-side-card admin-order-side-card">
                <h2>처리 상태</h2>
                <label className="admin-field">
                  <span>주문 상태</span>
                  <select
                    disabled={isUpdating}
                    value={order.status}
                    onChange={(event) =>
                      handleStatusChange(event.target.value as OrderStatus)
                    }
                  >
                    {ORDER_STATUS_OPTIONS.map((status) => (
                      <option key={status.value} value={status.value}>
                        {status.label}
                      </option>
                    ))}
                  </select>
                </label>
                {message && <p className="admin-message">{message}</p>}

                <h2>고객 정보</h2>
                <dl>
                  <div>
                    <dt>회원명</dt>
                    <dd>{order.memberName}</dd>
                  </div>
                  <div>
                    <dt>이메일</dt>
                    <dd>{order.memberEmail}</dd>
                  </div>
                  <div>
                    <dt>회원 ID</dt>
                    <dd>{order.memberId}</dd>
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
            <section className="admin-empty">
              <p className="eyebrow">Not found</p>
              <h2>{message || '주문을 찾을 수 없습니다.'}</h2>
              <Link to="/admin/orders">주문 목록으로 이동</Link>
            </section>
          )}
        </AdminAccessGate>
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
