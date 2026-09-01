import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import {
  fetchAdminOrders,
  updateAdminOrderStatus,
} from '../api/admin'
import { ApiError } from '../api/auth'
import { useAuth } from '../auth/auth-state'
import { AdminAccessGate } from '../components/AdminAccessGate'
import {
  AdminBreadcrumb,
  AdminNavigation,
} from '../components/AdminNavigation'
import { SiteHeader } from '../components/SiteHeader'
import type { AdminOrder } from '../types/admin'
import type { OrderStatus } from '../types/order'
import { currencyFormatter, formatImageUrl } from '../utils/catalog'
import {
  formatOrderDate,
  formatOrderTitle,
  formatOrderStatus,
  sumOrderQuantity,
} from '../utils/order'

const ADMIN_PAGE_SIZE = 12
const ORDER_STATUS_OPTIONS: Array<{ label: string; value: OrderStatus }> = [
  { label: '주문 접수', value: 'CREATED' },
  { label: '결제 완료', value: 'PAID' },
  { label: '배송 준비', value: 'PREPARING' },
  { label: '배송 중', value: 'SHIPPED' },
  { label: '배송 완료', value: 'DELIVERED' },
  { label: '주문 취소', value: 'CANCELED' },
]

export function AdminOrdersPage() {
  const { accessToken, user } = useAuth()
  const [orders, setOrders] = useState<AdminOrder[]>([])
  const [totalOrders, setTotalOrders] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [page, setPage] = useState(0)
  const [selectedStatus, setSelectedStatus] = useState<OrderStatus | null>(null)
  const [keywordDraft, setKeywordDraft] = useState('')
  const [keyword, setKeyword] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [updatingOrderId, setUpdatingOrderId] = useState<number | null>(null)

  const canLoad = Boolean(accessToken && user?.role === 'ADMIN')

  useEffect(() => {
    if (!accessToken || !canLoad) {
      return
    }

    const token = accessToken
    const controller = new AbortController()

    async function loadOrders() {
      setIsLoading(true)
      setMessage('')

      try {
        const data = await fetchAdminOrders(
          token,
          {
            status: selectedStatus,
            keyword,
            page,
            size: ADMIN_PAGE_SIZE,
          },
          controller.signal,
        )

        setOrders(data.content)
        setTotalOrders(data.totalElements)
        setTotalPages(data.totalPages)
      } catch (error) {
        if (!controller.signal.aborted) {
          setMessage(readApiMessage(error, '주문 목록을 불러오지 못했습니다.'))
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      }
    }

    loadOrders()

    return () => controller.abort()
  }, [accessToken, canLoad, keyword, page, selectedStatus])

  const activeFilters = useMemo(() => {
    return [selectedStatus !== null ? '상태' : null, keyword.trim() ? '검색어' : null]
      .filter(Boolean).length
  }, [keyword, selectedStatus])

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setKeyword(keywordDraft)
    setPage(0)
  }

  function handleStatusFilterChange(value: string) {
    setSelectedStatus(value ? (value as OrderStatus) : null)
    setPage(0)
  }

  async function handleOrderStatusChange(order: AdminOrder, status: OrderStatus) {
    if (!accessToken || order.status === status) {
      return
    }

    setUpdatingOrderId(order.id)
    setMessage('')

    try {
      const updatedOrder = await updateAdminOrderStatus(accessToken, order.id, {
        status,
      })

      setOrders((current) =>
        current.map((currentOrder) =>
          currentOrder.id === updatedOrder.id ? updatedOrder : currentOrder,
        ),
      )
    } catch (error) {
      setMessage(readApiMessage(error, '주문 상태를 변경하지 못했습니다.'))
    } finally {
      setUpdatingOrderId(null)
    }
  }

  return (
    <div className="storefront">
      <SiteHeader />

      <main className="admin-page">
        <AdminAccessGate>
          <section className="admin-heading">
            <div>
              <AdminBreadcrumb current="주문 목록" />
              <h1>주문 목록</h1>
            </div>
          </section>

          <AdminNavigation active="orders" />

          <section className="admin-toolbar compact" aria-label="주문 검색 조건">
            <form className="admin-search-form" onSubmit={handleSearch}>
              <label htmlFor="admin-order-search">검색</label>
              <input
                id="admin-order-search"
                type="search"
                value={keywordDraft}
                placeholder="주문번호, 수령자, 이메일"
                onChange={(event) => setKeywordDraft(event.target.value)}
              />
              <button type="submit">검색</button>
            </form>

            <label className="admin-select-field">
              <span>상태</span>
              <select
                value={selectedStatus ?? ''}
                onChange={(event) => handleStatusFilterChange(event.target.value)}
              >
                <option value="">전체</option>
                {ORDER_STATUS_OPTIONS.map((status) => (
                  <option key={status.value} value={status.value}>
                    {status.label}
                  </option>
                ))}
              </select>
            </label>
          </section>

          <section className="admin-list-panel">
            <div className="admin-list-meta">
              <span>{totalOrders} orders</span>
              <span>{activeFilters > 0 ? `${activeFilters} filters` : 'All orders'}</span>
            </div>

            {message && <p className="admin-message">{message}</p>}

            {isLoading ? (
              <div className="admin-table-skeleton" />
            ) : orders.length > 0 ? (
              <>
                <div className="admin-order-list">
                  {orders.map((order) => (
                    <article className="admin-order-card" key={order.id}>
                      <div className="admin-order-thumbs" aria-hidden="true">
                        {order.items.slice(0, 3).map((item) => (
                          <span className="admin-order-thumb" key={item.id}>
                            {item.thumbnailImageUrl ? (
                              <img
                                src={formatImageUrl(item.thumbnailImageUrl, 160)}
                                alt=""
                              />
                            ) : (
                              'PLAVOR'
                            )}
                          </span>
                        ))}
                      </div>

                      <div className="admin-order-main">
                        <p className="eyebrow">{formatOrderDate(order.orderedAt)}</p>
                        <h2>{formatOrderTitle(order)}</h2>
                        <span>주문번호 {order.orderNumber}</span>
                        <small>
                          {order.memberName} · {order.memberEmail}
                        </small>
                      </div>

                      <div className="admin-order-shipping">
                        <span>배송지</span>
                        <strong>{order.receiverName}</strong>
                        <small>
                          ({order.postalCode}) {order.address}
                        </small>
                      </div>

                      <div className="admin-order-actions">
                        <label className="admin-status-control">
                          <span>주문 상태</span>
                          <select
                            className={`admin-status-select ${order.status.toLowerCase()}`}
                            disabled={
                              updatingOrderId === order.id ||
                              order.availableNextStatuses.length === 0
                            }
                            value={order.status}
                            onChange={(event) =>
                              handleOrderStatusChange(
                                order,
                                event.target.value as OrderStatus,
                              )
                            }
                          >
                            {getSelectableStatusOptions(order).map((status) => (
                              <option key={status.value} value={status.value}>
                                {status.label}
                              </option>
                            ))}
                          </select>
                          <small className="admin-field-hint">
                            {order.availableNextStatuses.length > 0
                              ? `${formatOrderStatus(order.status)}에서 변경 가능`
                              : '최종 상태'}
                          </small>
                        </label>
                        <strong>{currencyFormatter.format(order.totalAmount)}원</strong>
                        <span>총 {sumOrderQuantity(order)}개</span>
                        <Link to={`/admin/orders/${order.id}`}>상세 보기</Link>
                      </div>
                    </article>
                  ))}
                </div>

                <div className="admin-pagination">
                  <button
                    type="button"
                    disabled={page === 0}
                    onClick={() => setPage((current) => Math.max(current - 1, 0))}
                  >
                    이전
                  </button>
                  <span>
                    {totalPages === 0 ? 0 : page + 1} / {totalPages}
                  </span>
                  <button
                    type="button"
                    disabled={totalPages === 0 || page + 1 >= totalPages}
                    onClick={() => setPage((current) => current + 1)}
                  >
                    다음
                  </button>
                </div>
              </>
            ) : (
              <div className="admin-empty">
                <p className="eyebrow">No orders</p>
                <h2>조건에 맞는 주문이 없습니다.</h2>
                <Link to="/">홈으로 이동</Link>
              </div>
            )}
          </section>
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

function getSelectableStatusOptions(order: AdminOrder) {
  const selectableStatuses = new Set<OrderStatus>([
    order.status,
    ...order.availableNextStatuses,
  ])

  return ORDER_STATUS_OPTIONS.filter((status) =>
    selectableStatuses.has(status.value),
  )
}
