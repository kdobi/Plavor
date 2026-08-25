import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { addCartItem } from '../api/cart'
import { useAuth } from '../auth/auth-state'
import { fetchProductDetail } from '../api/catalog'
import { SiteHeader } from '../components/SiteHeader'
import { findFallbackProductDetail } from '../data/fallbackCatalog'
import type { ProductDetail } from '../types/catalog'
import { currencyFormatter, formatImageUrl } from '../utils/catalog'

export function ProductDetailPage() {
  const { productId } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const { accessToken, user } = useAuth()
  const [product, setProduct] = useState<ProductDetail | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isNotFound, setIsNotFound] = useState(false)
  const [isUsingFallback, setIsUsingFallback] = useState(false)
  const [quantity, setQuantity] = useState(1)
  const [cartMessage, setCartMessage] = useState('')
  const [isAddingCart, setIsAddingCart] = useState(false)

  useEffect(() => {
    const controller = new AbortController()

    async function loadProductDetail() {
      if (!productId) {
        setIsNotFound(true)
        setIsLoading(false)
        return
      }

      setIsLoading(true)
      setIsNotFound(false)

      try {
        const data = await fetchProductDetail(productId, controller.signal)

        if (!data) {
          setProduct(null)
          setIsNotFound(true)
          return
        }

        setProduct(data)
        setIsUsingFallback(false)
      } catch {
        if (!controller.signal.aborted) {
          const fallbackProduct = findFallbackProductDetail(productId)

          setProduct(fallbackProduct)
          setIsNotFound(!fallbackProduct)
          setIsUsingFallback(Boolean(fallbackProduct))
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      }
    }

    loadProductDetail()

    return () => controller.abort()
  }, [productId])

  const primaryImage = useMemo(() => {
    return (
      product?.images.find((image) => image.thumbnail) ??
      product?.images[0] ??
      null
    )
  }, [product])

  async function handleAddCart() {
    if (!product) {
      return
    }

    if (!user || !accessToken) {
      navigate('/login', {
        state: { from: `${location.pathname}${location.search}` },
      })
      return
    }

    setIsAddingCart(true)
    setCartMessage('')

    try {
      await addCartItem(accessToken, {
        productId: product.id,
        quantity,
      })
      navigate('/cart')
    } catch (error) {
      if (error instanceof ApiError) {
        setCartMessage(error.message)
      } else {
        setCartMessage('장바구니에 담지 못했습니다.')
      }
    } finally {
      setIsAddingCart(false)
    }
  }

  return (
    <div className="storefront">
      <SiteHeader />

      <main className="detail-page">
        <Link className="back-link" to="/">
          Back to shop
        </Link>

        {isLoading ? (
          <ProductDetailSkeleton />
        ) : isNotFound || !product ? (
          <section className="detail-empty">
            <p className="eyebrow">Not found</p>
            <h1>상품을 찾을 수 없습니다.</h1>
            <Link to="/">홈으로 돌아가기</Link>
          </section>
        ) : (
          <section className="detail-layout">
            <div className="detail-media">
              {primaryImage ? (
                <img
                  src={formatImageUrl(primaryImage.imageUrl, 1080)}
                  alt={primaryImage.altText ?? product.name}
                />
              ) : (
                <div className="detail-image-fallback">
                  {product.category.name}
                </div>
              )}
            </div>

            <aside className="detail-panel">
              <div className="detail-heading">
                <div>
                  <p className="eyebrow">{product.category.name}</p>
                  <h1>{product.name}</h1>
                </div>
                {isUsingFallback && <span className="preview-chip">Preview data</span>}
              </div>

              <div className="detail-price">
                {currencyFormatter.format(product.price)}원
              </div>

              <div className="detail-meta">
                <div>
                  <span>상태</span>
                  <strong>
                    {product.status === 'SOLD_OUT' ? '품절' : '판매중'}
                  </strong>
                </div>
                <div>
                  <span>재고</span>
                  <strong>{product.stockQuantity}개</strong>
                </div>
              </div>

              <p className="detail-description">
                {product.description ?? '상품 설명이 준비 중입니다.'}
              </p>

              <div className="detail-purchase-row">
                <span>수량</span>
                <div className="detail-quantity-control">
                  <button
                    aria-label="수량 줄이기"
                    disabled={quantity <= 1}
                    type="button"
                    onClick={() => setQuantity((current) => current - 1)}
                  >
                    -
                  </button>
                  <strong>{quantity}</strong>
                  <button
                    aria-label="수량 늘리기"
                    disabled={quantity >= product.stockQuantity}
                    type="button"
                    onClick={() => setQuantity((current) => current + 1)}
                  >
                    +
                  </button>
                </div>
              </div>

              {cartMessage && <p className="detail-message">{cartMessage}</p>}

              <div className="detail-actions">
                <button
                  type="button"
                  disabled={product.status === 'SOLD_OUT' || isAddingCart}
                  onClick={handleAddCart}
                >
                  {product.status === 'SOLD_OUT'
                    ? '재입고 알림 받기'
                    : isAddingCart
                      ? '담는 중'
                      : '장바구니 담기'}
                </button>
                <button type="button" className="secondary-action">
                  관심상품
                </button>
              </div>
            </aside>
          </section>
        )}
      </main>
    </div>
  )
}

function ProductDetailSkeleton() {
  return (
    <section className="detail-layout">
      <div className="detail-media detail-skeleton" />
      <aside className="detail-panel">
        <div className="skeleton-line wide" />
        <div className="skeleton-line wide" />
        <div className="skeleton-line" />
      </aside>
    </section>
  )
}
