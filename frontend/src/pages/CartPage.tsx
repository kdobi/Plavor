import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { deleteCartItem, fetchCart, updateCartItem } from '../api/cart'
import { useAuth } from '../auth/auth-state'
import { SiteHeader } from '../components/SiteHeader'
import type { Cart, CartItem } from '../types/cart'
import { currencyFormatter, formatImageUrl } from '../utils/catalog'

const DELIVERY_FEE = 3000

export function CartPage() {
  const location = useLocation()
  const { accessToken, isInitializing, user } = useAuth()
  const [cart, setCart] = useState<Cart | null>(null)
  const [selectedItemIds, setSelectedItemIds] = useState<number[]>([])
  const [message, setMessage] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [updatingItemId, setUpdatingItemId] = useState<number | null>(null)

  useEffect(() => {
    if (!accessToken) {
      return
    }

    const token = accessToken
    const controller = new AbortController()

    async function loadCart() {
      setIsLoading(true)
      setMessage('')

      try {
        const data = await fetchCart(token, controller.signal)

        setCart(data)
        setSelectedItemIds(data.items.map((item) => item.id))
      } catch (error) {
        if (!controller.signal.aborted) {
          setMessage(readApiMessage(error, '장바구니를 불러오지 못했습니다.'))
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      }
    }

    loadCart()

    return () => controller.abort()
  }, [accessToken])

  const loginState = useMemo(() => {
    return { from: `${location.pathname}${location.search}` }
  }, [location.pathname, location.search])

  const selectedItems = useMemo(() => {
    if (!cart) {
      return []
    }

    return cart.items.filter((item) => selectedItemIds.includes(item.id))
  }, [cart, selectedItemIds])

  const selectedAmount = useMemo(() => {
    return selectedItems.reduce((total, item) => total + item.totalPrice, 0)
  }, [selectedItems])

  const selectedQuantity = useMemo(() => {
    return selectedItems.reduce((total, item) => total + item.quantity, 0)
  }, [selectedItems])

  const selectedDeliveryFee = selectedItems.length > 0 ? DELIVERY_FEE : 0
  const selectedPaymentAmount = selectedAmount + selectedDeliveryFee
  const isAllSelected =
    Boolean(cart?.items.length) && selectedItemIds.length === cart?.items.length

  async function handleQuantityChange(item: CartItem, nextQuantity: number) {
    if (!accessToken || nextQuantity < 1 || nextQuantity > item.stockQuantity) {
      return
    }

    setUpdatingItemId(item.id)
    setMessage('')

    try {
      const data = await updateCartItem(accessToken, item.id, {
        quantity: nextQuantity,
      })

      setCart(data)
      setSelectedItemIds((current) =>
        data.items
          .filter((nextItem) => current.includes(nextItem.id))
          .map((nextItem) => nextItem.id),
      )
    } catch (error) {
      setMessage(readApiMessage(error, '수량을 변경하지 못했습니다.'))
    } finally {
      setUpdatingItemId(null)
    }
  }

  async function handleDelete(itemId: number) {
    if (!accessToken) {
      return
    }

    setUpdatingItemId(itemId)
    setMessage('')

    try {
      await deleteCartItem(accessToken, itemId)
      const data = await fetchCart(accessToken)

      setCart(data)
      setSelectedItemIds((current) => current.filter((id) => id !== itemId))
    } catch (error) {
      setMessage(readApiMessage(error, '상품을 삭제하지 못했습니다.'))
    } finally {
      setUpdatingItemId(null)
    }
  }

  async function handleDeleteSelected() {
    if (!accessToken || selectedItemIds.length === 0) {
      return
    }

    setUpdatingItemId(-1)
    setMessage('')

    try {
      for (const itemId of selectedItemIds) {
        await deleteCartItem(accessToken, itemId)
      }

      const data = await fetchCart(accessToken)

      setCart(data)
      setSelectedItemIds([])
    } catch (error) {
      setMessage(readApiMessage(error, '선택한 상품을 삭제하지 못했습니다.'))
    } finally {
      setUpdatingItemId(null)
    }
  }

  function handleToggleAll() {
    if (!cart) {
      return
    }

    setSelectedItemIds((current) =>
      current.length === cart.items.length ? [] : cart.items.map((item) => item.id),
    )
  }

  function handleToggleItem(itemId: number) {
    setSelectedItemIds((current) =>
      current.includes(itemId)
        ? current.filter((id) => id !== itemId)
        : [...current, itemId],
    )
  }

  return (
    <div className="storefront">
      <SiteHeader />

      <main className="cart-page">
        <section className="cart-heading">
          <div>
            <p className="eyebrow">Shopping bag</p>
            <h1>장바구니</h1>
          </div>
          <Link to="/">계속 쇼핑하기</Link>
        </section>

        {accessToken && (isInitializing || isLoading) ? (
          <CartSkeleton />
        ) : !user || !accessToken ? (
          <section className="cart-empty">
            <p className="eyebrow">Login required</p>
            <h2>로그인 후 장바구니를 확인할 수 있습니다.</h2>
            <Link to="/login" state={loginState}>
              로그인하기
            </Link>
          </section>
        ) : cart && cart.items.length > 0 ? (
          <>
            <section className="cart-overview">
              <div className="cart-delivery-tabs" aria-label="배송 유형">
                <button className="active" type="button">
                  <strong>{cart.items.length}</strong>
                  <span>PLAVOR 배송</span>
                </button>
                <button type="button">
                  <strong>0</strong>
                  <span>브랜드 배송</span>
                </button>
              </div>

              <div className="cart-benefits" aria-label="결제 혜택">
                <div>
                  <span>네이버페이</span>
                  <strong>최대 3만 포인트 적립</strong>
                  <button type="button">더보기</button>
                </div>
                <div>
                  <span>BC카드</span>
                  <strong>3% 청구할인</strong>
                  <button type="button">더보기</button>
                </div>
              </div>
            </section>

            <section className="cart-layout">
              <div className="cart-main-column">
                <div className="cart-select-row">
                  <button
                    className={isAllSelected ? 'selected' : ''}
                    type="button"
                    onClick={handleToggleAll}
                  >
                    <span aria-hidden="true">{isAllSelected ? '✓' : ''}</span>
                    전체 선택
                  </button>
                  <button
                    disabled={selectedItemIds.length === 0 || updatingItemId === -1}
                    type="button"
                    onClick={handleDeleteSelected}
                  >
                    선택 삭제
                  </button>
                </div>

                <div className="cart-items" aria-label="장바구니 상품">
                  {cart.items.map((item) => (
                    <CartItemRow
                      item={item}
                      isSelected={selectedItemIds.includes(item.id)}
                      isUpdating={updatingItemId === item.id}
                      key={item.id}
                      onDelete={handleDelete}
                      onQuantityChange={handleQuantityChange}
                      onToggle={handleToggleItem}
                    />
                  ))}
                </div>
              </div>

              <aside className="cart-summary" aria-label="주문 요약">
                <h2>주문 요약</h2>
                <dl>
                  <div>
                    <dt>선택 상품</dt>
                    <dd>{selectedQuantity}개</dd>
                  </div>
                  <div>
                    <dt>상품 금액</dt>
                    <dd>{currencyFormatter.format(selectedAmount)}원</dd>
                  </div>
                  <div>
                    <dt>배송비</dt>
                    <dd>
                      {selectedItems.length > 0
                        ? `${currencyFormatter.format(selectedDeliveryFee)}원`
                        : '-'}
                    </dd>
                  </div>
                  <div className="cart-total-row">
                    <dt>예상 결제금액</dt>
                    <dd>{currencyFormatter.format(selectedPaymentAmount)}원</dd>
                  </div>
                </dl>
                {message && <p className="cart-message">{message}</p>}
                <button disabled={selectedItems.length === 0} type="button">
                  총 {selectedQuantity}개 주문하기
                </button>
              </aside>
            </section>
          </>
        ) : (
          <section className="cart-empty">
            <p className="eyebrow">Empty cart</p>
            <h2>아직 담긴 상품이 없습니다.</h2>
            <Link to="/">상품 보러가기</Link>
            {message && <p className="cart-message">{message}</p>}
          </section>
        )}
      </main>
    </div>
  )
}

function CartItemRow({
  item,
  isSelected,
  isUpdating,
  onDelete,
  onQuantityChange,
  onToggle,
}: {
  item: CartItem
  isSelected: boolean
  isUpdating: boolean
  onDelete: (itemId: number) => void
  onQuantityChange: (item: CartItem, nextQuantity: number) => void
  onToggle: (itemId: number) => void
}) {
  const canDecrease = item.quantity > 1 && !isUpdating
  const canIncrease = item.quantity < item.stockQuantity && !isUpdating

  return (
    <article className="cart-item">
      <div className="cart-item-select">
        <button
          aria-label={`${item.productName} 선택`}
          className={isSelected ? 'cart-check-button selected' : 'cart-check-button'}
          type="button"
          onClick={() => onToggle(item.id)}
        >
          <span aria-hidden="true">{isSelected ? '✓' : ''}</span>
        </button>
      </div>

      <div className="cart-item-product">
        <Link className="cart-item-image" to={`/products/${item.productId}`}>
          {item.thumbnailImageUrl ? (
            <img
              src={formatImageUrl(item.thumbnailImageUrl, 420)}
              alt={item.productName}
            />
          ) : (
            <span>PLAVOR</span>
          )}
        </Link>

        <div className="cart-item-info">
          <Link to={`/products/${item.productId}`}>{item.productName}</Link>
          <span>PLAVOR delivery</span>
          <small>재고 {item.stockQuantity}개</small>
        </div>
      </div>

      <div className="cart-item-actions">
        <div className="quantity-control" aria-label={`${item.productName} 수량`}>
          <button
            aria-label="수량 줄이기"
            disabled={!canDecrease}
            type="button"
            onClick={() => onQuantityChange(item, item.quantity - 1)}
          >
            -
          </button>
          <strong>{item.quantity}</strong>
          <button
            aria-label="수량 늘리기"
            disabled={!canIncrease}
            type="button"
            onClick={() => onQuantityChange(item, item.quantity + 1)}
          >
            +
          </button>
        </div>
      </div>

      <div className="cart-item-price">
        <span>상품금액</span>
        <strong>{currencyFormatter.format(item.totalPrice)}원</strong>
      </div>

      <button
        className="cart-remove-button"
        disabled={isUpdating}
        type="button"
        onClick={() => onDelete(item.id)}
      >
        삭제
      </button>
    </article>
  )
}

function CartSkeleton() {
  return (
    <section className="cart-layout">
      <div className="cart-items">
        <div className="cart-skeleton-row" />
        <div className="cart-skeleton-row" />
      </div>
      <aside className="cart-summary">
        <div className="skeleton-line wide" />
        <div className="skeleton-line" />
        <div className="skeleton-line wide" />
      </aside>
    </section>
  )
}

function readApiMessage(error: unknown, fallbackMessage: string) {
  if (error instanceof ApiError) {
    return error.message
  }

  return fallbackMessage
}
