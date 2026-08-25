import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { deleteCartItem, fetchCart, updateCartItem } from '../api/cart'
import { createOrder } from '../api/order'
import { useAuth } from '../auth/auth-state'
import { SiteHeader } from '../components/SiteHeader'
import type { Cart, CartItem } from '../types/cart'
import { currencyFormatter, formatImageUrl } from '../utils/catalog'

const DELIVERY_FEE = 3000
const PHONE_MAX_LENGTH = 11
const POSTAL_CODE_LENGTH = 5
const ORDER_FORM_FIELDS = [
  'receiverName',
  'receiverPhone',
  'postalCode',
  'address',
  'addressDetail',
  'deliveryMessage',
] as const

type OrderFormField = (typeof ORDER_FORM_FIELDS)[number]
type OrderForm = Record<OrderFormField, string>
type OrderFieldErrors = Partial<Record<OrderFormField, string>>
const ORDER_FORM_FIELD_SET = new Set<string>(ORDER_FORM_FIELDS)

export function CartPage() {
  const location = useLocation()
  const { accessToken, isInitializing, user } = useAuth()
  const [cart, setCart] = useState<Cart | null>(null)
  const [selectedItemIds, setSelectedItemIds] = useState<number[]>([])
  const [message, setMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [updatingItemId, setUpdatingItemId] = useState<number | null>(null)
  const [isOrdering, setIsOrdering] = useState(false)
  const [orderForm, setOrderForm] = useState<OrderForm>({
    receiverName: '',
    receiverPhone: '',
    postalCode: '',
    address: '',
    addressDetail: '',
    deliveryMessage: '',
  })
  const [orderFieldErrors, setOrderFieldErrors] = useState<OrderFieldErrors>({})

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
  const receiverName = orderForm.receiverName || user?.name || ''
  const receiverPhone =
    orderForm.receiverPhone || normalizeDigits(user?.phone ?? '', PHONE_MAX_LENGTH)

  async function handleQuantityChange(item: CartItem, nextQuantity: number) {
    if (!accessToken || nextQuantity < 1 || nextQuantity > item.stockQuantity) {
      return
    }

    setUpdatingItemId(item.id)
    setMessage('')
    setSuccessMessage('')

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
    setSuccessMessage('')

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
    setSuccessMessage('')

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

  function handleOrderFormChange(field: OrderFormField, value: string) {
    setOrderForm((current) => ({
      ...current,
      [field]: value,
    }))
    setOrderFieldErrors((current) => {
      if (!current[field]) {
        return current
      }

      const next = { ...current }
      delete next[field]
      return next
    })
  }

  async function handleCreateOrder(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!accessToken || selectedItemIds.length === 0) {
      return
    }

    setIsOrdering(true)
    setMessage('')
    setSuccessMessage('')
    setOrderFieldErrors({})

    try {
      const order = await createOrder(accessToken, {
        cartItemIds: selectedItemIds,
        receiverName,
        receiverPhone,
        postalCode: orderForm.postalCode,
        address: orderForm.address,
        addressDetail: orderForm.addressDetail.trim() || undefined,
        deliveryMessage: orderForm.deliveryMessage.trim() || undefined,
      })
      const data = await fetchCart(accessToken)

      setCart(data)
      setSelectedItemIds(data.items.map((item) => item.id))
      setSuccessMessage(`주문이 생성되었습니다. 주문번호 ${order.orderNumber}`)
    } catch (error) {
      const nextFieldErrors = readOrderFieldErrors(error)

      if (Object.keys(nextFieldErrors).length > 0) {
        setOrderFieldErrors(nextFieldErrors)
      }

      setMessage(readApiMessage(error, '주문을 생성하지 못했습니다.'))
    } finally {
      setIsOrdering(false)
    }
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
                <form className="cart-order-form" onSubmit={handleCreateOrder}>
                  <div className="cart-order-fields">
                    <label>
                      <span>수령자</span>
                      <input
                        aria-invalid={Boolean(orderFieldErrors.receiverName)}
                        autoComplete="name"
                        required
                        type="text"
                        value={receiverName}
                        placeholder="김동빈"
                        onChange={(event) =>
                          handleOrderFormChange('receiverName', event.target.value)
                        }
                      />
                      {orderFieldErrors.receiverName && (
                        <small className="cart-field-message">
                          {orderFieldErrors.receiverName}
                        </small>
                      )}
                    </label>

                    <label>
                      <span>연락처</span>
                      <input
                        aria-invalid={Boolean(orderFieldErrors.receiverPhone)}
                        autoComplete="tel"
                        inputMode="numeric"
                        maxLength={PHONE_MAX_LENGTH}
                        pattern="01[016789][0-9]{7,8}"
                        required
                        title="010으로 시작하는 10~11자리 숫자를 입력해주세요."
                        type="tel"
                        value={receiverPhone}
                        placeholder="01012345678"
                        onChange={(event) =>
                          handleOrderFormChange(
                            'receiverPhone',
                            normalizeDigits(event.target.value, PHONE_MAX_LENGTH),
                          )
                        }
                      />
                      {orderFieldErrors.receiverPhone && (
                        <small className="cart-field-message">
                          {orderFieldErrors.receiverPhone}
                        </small>
                      )}
                    </label>

                    <label>
                      <span>우편번호</span>
                      <input
                        aria-invalid={Boolean(orderFieldErrors.postalCode)}
                        autoComplete="postal-code"
                        inputMode="numeric"
                        maxLength={POSTAL_CODE_LENGTH}
                        pattern="[0-9]{5}"
                        required
                        title="5자리 숫자 우편번호를 입력해주세요."
                        type="text"
                        value={orderForm.postalCode}
                        placeholder="06236"
                        onChange={(event) =>
                          handleOrderFormChange(
                            'postalCode',
                            normalizeDigits(event.target.value, POSTAL_CODE_LENGTH),
                          )
                        }
                      />
                      {orderFieldErrors.postalCode && (
                        <small className="cart-field-message">
                          {orderFieldErrors.postalCode}
                        </small>
                      )}
                    </label>

                    <label>
                      <span>주소</span>
                      <input
                        aria-invalid={Boolean(orderFieldErrors.address)}
                        autoComplete="street-address"
                        required
                        type="text"
                        value={orderForm.address}
                        placeholder="서울특별시 강남구 테헤란로 123"
                        onChange={(event) =>
                          handleOrderFormChange('address', event.target.value)
                        }
                      />
                      {orderFieldErrors.address && (
                        <small className="cart-field-message">
                          {orderFieldErrors.address}
                        </small>
                      )}
                    </label>

                    <label>
                      <span>상세 주소</span>
                      <input
                        aria-invalid={Boolean(orderFieldErrors.addressDetail)}
                        type="text"
                        value={orderForm.addressDetail}
                        placeholder="선택 입력"
                        onChange={(event) =>
                          handleOrderFormChange('addressDetail', event.target.value)
                        }
                      />
                      {orderFieldErrors.addressDetail && (
                        <small className="cart-field-message">
                          {orderFieldErrors.addressDetail}
                        </small>
                      )}
                    </label>

                    <label>
                      <span>배송 요청사항</span>
                      <textarea
                        aria-invalid={Boolean(orderFieldErrors.deliveryMessage)}
                        rows={3}
                        value={orderForm.deliveryMessage}
                        placeholder="선택 입력"
                        onChange={(event) =>
                          handleOrderFormChange('deliveryMessage', event.target.value)
                        }
                      />
                      {orderFieldErrors.deliveryMessage && (
                        <small className="cart-field-message">
                          {orderFieldErrors.deliveryMessage}
                        </small>
                      )}
                    </label>
                  </div>

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
                  {successMessage && (
                    <p className="cart-message success">{successMessage}</p>
                  )}
                  <button disabled={selectedItems.length === 0 || isOrdering} type="submit">
                    {isOrdering
                      ? '주문 생성 중'
                      : `총 ${selectedQuantity}개 주문하기`}
                  </button>
                </form>
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

function normalizeDigits(value: string, maxLength: number) {
  return value.replace(/\D/g, '').slice(0, maxLength)
}

function readOrderFieldErrors(error: unknown) {
  if (!(error instanceof ApiError)) {
    return {}
  }

  return error.fieldErrors.reduce<OrderFieldErrors>((errors, fieldError) => {
    const fieldName = readOrderFormField(fieldError.field)

    if (fieldName && !errors[fieldName]) {
      errors[fieldName] = fieldError.message
    }

    return errors
  }, {})
}

function readOrderFormField(field: string) {
  const fieldName = field.split('.').at(-1) ?? field

  if (!ORDER_FORM_FIELD_SET.has(fieldName)) {
    return null
  }

  return fieldName as OrderFormField
}
