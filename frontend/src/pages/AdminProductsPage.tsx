import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import {
  fetchAdminProducts,
  updateAdminProductStatus,
} from '../api/admin'
import { ApiError } from '../api/auth'
import { fetchCategories } from '../api/catalog'
import { useAuth } from '../auth/auth-state'
import { AdminAccessGate } from '../components/AdminAccessGate'
import { SiteHeader } from '../components/SiteHeader'
import type { AdminProduct } from '../types/admin'
import type { Category, ProductStatus } from '../types/catalog'
import { currencyFormatter, formatImageUrl } from '../utils/catalog'

const ADMIN_PAGE_SIZE = 12
const PRODUCT_STATUS_OPTIONS: Array<{
  label: string
  value: ProductStatus
}> = [
  { label: '판매중', value: 'ACTIVE' },
  { label: '품절', value: 'SOLD_OUT' },
  { label: '숨김', value: 'HIDDEN' },
]

export function AdminProductsPage() {
  const { accessToken, user } = useAuth()
  const [categories, setCategories] = useState<Category[]>([])
  const [products, setProducts] = useState<AdminProduct[]>([])
  const [totalProducts, setTotalProducts] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [page, setPage] = useState(0)
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(
    null,
  )
  const [selectedStatus, setSelectedStatus] = useState<ProductStatus | null>(
    null,
  )
  const [keywordDraft, setKeywordDraft] = useState('')
  const [keyword, setKeyword] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [updatingProductId, setUpdatingProductId] = useState<number | null>(null)

  const canLoad = Boolean(accessToken && user?.role === 'ADMIN')

  useEffect(() => {
    if (!canLoad) {
      return
    }

    const controller = new AbortController()

    async function loadCategories() {
      try {
        const data = await fetchCategories(controller.signal)
        setCategories(data)
      } catch {
        if (!controller.signal.aborted) {
          setMessage('카테고리를 불러오지 못했습니다.')
        }
      }
    }

    loadCategories()

    return () => controller.abort()
  }, [canLoad])

  useEffect(() => {
    if (!accessToken || !canLoad) {
      return
    }

    const token = accessToken
    const controller = new AbortController()

    async function loadProducts() {
      setIsLoading(true)
      setMessage('')

      try {
        const data = await fetchAdminProducts(
          token,
          {
            categoryId: selectedCategoryId,
            status: selectedStatus,
            keyword,
            page,
            size: ADMIN_PAGE_SIZE,
          },
          controller.signal,
        )

        setProducts(data.content)
        setTotalProducts(data.totalElements)
        setTotalPages(data.totalPages)
      } catch (error) {
        if (!controller.signal.aborted) {
          setMessage(readApiMessage(error, '상품 목록을 불러오지 못했습니다.'))
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      }
    }

    loadProducts()

    return () => controller.abort()
  }, [
    accessToken,
    canLoad,
    keyword,
    page,
    selectedCategoryId,
    selectedStatus,
  ])

  const activeFilters = useMemo(() => {
    return [
      selectedCategoryId !== null ? '카테고리' : null,
      selectedStatus !== null ? '상태' : null,
      keyword.trim() ? '검색어' : null,
    ].filter(Boolean).length
  }, [keyword, selectedCategoryId, selectedStatus])

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setKeyword(keywordDraft)
    setPage(0)
  }

  function handleCategoryChange(value: string) {
    setSelectedCategoryId(value ? Number(value) : null)
    setPage(0)
  }

  function handleStatusFilterChange(value: string) {
    setSelectedStatus(value ? (value as ProductStatus) : null)
    setPage(0)
  }

  async function handleProductStatusChange(
    product: AdminProduct,
    status: ProductStatus,
  ) {
    if (!accessToken || product.status === status) {
      return
    }

    setUpdatingProductId(product.id)
    setMessage('')

    try {
      const updatedProduct = await updateAdminProductStatus(accessToken, product.id, {
        status,
      })

      setProducts((current) =>
        current.map((currentProduct) =>
          currentProduct.id === updatedProduct.id ? updatedProduct : currentProduct,
        ),
      )
    } catch (error) {
      setMessage(readApiMessage(error, '상품 상태를 변경하지 못했습니다.'))
    } finally {
      setUpdatingProductId(null)
    }
  }

  return (
    <div className="storefront">
      <SiteHeader />

      <main className="admin-page">
        <AdminAccessGate>
          <section className="admin-heading">
            <div>
              <p className="eyebrow">Admin Catalog</p>
              <h1>상품 관리</h1>
            </div>
            <Link className="admin-primary-link" to="/admin/products/new">
              상품 등록
            </Link>
          </section>

          <section className="admin-toolbar" aria-label="상품 검색 조건">
            <form className="admin-search-form" onSubmit={handleSearch}>
              <label htmlFor="admin-product-search">검색</label>
              <input
                id="admin-product-search"
                type="search"
                value={keywordDraft}
                placeholder="상품명으로 검색"
                onChange={(event) => setKeywordDraft(event.target.value)}
              />
              <button type="submit">검색</button>
            </form>

            <label className="admin-select-field">
              <span>카테고리</span>
              <select
                value={selectedCategoryId ?? ''}
                onChange={(event) => handleCategoryChange(event.target.value)}
              >
                <option value="">전체</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </label>

            <label className="admin-select-field">
              <span>상태</span>
              <select
                value={selectedStatus ?? ''}
                onChange={(event) => handleStatusFilterChange(event.target.value)}
              >
                <option value="">전체</option>
                {PRODUCT_STATUS_OPTIONS.map((status) => (
                  <option key={status.value} value={status.value}>
                    {status.label}
                  </option>
                ))}
              </select>
            </label>
          </section>

          <section className="admin-list-panel">
            <div className="admin-list-meta">
              <span>{totalProducts} items</span>
              <span>{activeFilters > 0 ? `${activeFilters} filters` : 'All products'}</span>
            </div>

            {message && <p className="admin-message">{message}</p>}

            {isLoading ? (
              <div className="admin-table-skeleton" />
            ) : products.length > 0 ? (
              <>
                <div className="admin-table-wrap">
                  <table className="admin-product-table">
                    <thead>
                      <tr>
                        <th>상품</th>
                        <th>카테고리</th>
                        <th>가격</th>
                        <th>재고</th>
                        <th>상태</th>
                        <th>수정일</th>
                        <th>관리</th>
                      </tr>
                    </thead>
                    <tbody>
                      {products.map((product) => (
                        <tr key={product.id}>
                          <td>
                            <div className="admin-product-cell">
                              <ProductThumb product={product} />
                              <div>
                                <strong>{product.name}</strong>
                                <span>ID {product.id}</span>
                              </div>
                            </div>
                          </td>
                          <td>{product.category.name}</td>
                          <td>{currencyFormatter.format(product.price)}원</td>
                          <td>{product.stockQuantity}개</td>
                          <td>
                            <select
                              className={`admin-status-select ${product.status.toLowerCase()}`}
                              disabled={updatingProductId === product.id}
                              value={product.status}
                              onChange={(event) =>
                                handleProductStatusChange(
                                  product,
                                  event.target.value as ProductStatus,
                                )
                              }
                            >
                              {PRODUCT_STATUS_OPTIONS.map((status) => (
                                <option key={status.value} value={status.value}>
                                  {status.label}
                                </option>
                              ))}
                            </select>
                          </td>
                          <td>{formatDate(product.updatedAt)}</td>
                          <td>
                            <Link to={`/admin/products/${product.id}/edit`}>
                              수정
                            </Link>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
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
                <p className="eyebrow">No products</p>
                <h2>조건에 맞는 상품이 없습니다.</h2>
                <Link to="/admin/products/new">첫 상품 등록하기</Link>
              </div>
            )}
          </section>
        </AdminAccessGate>
      </main>
    </div>
  )
}

function ProductThumb({ product }: { product: AdminProduct }) {
  const image =
    product.images.find((productImage) => productImage.thumbnail) ??
    product.images[0] ??
    null

  if (!image) {
    return <span className="admin-product-thumb">PLAVOR</span>
  }

  return (
    <span className="admin-product-thumb">
      <img src={formatImageUrl(image.imageUrl, 160)} alt="" />
    </span>
  )
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function readApiMessage(error: unknown, fallbackMessage: string) {
  if (error instanceof ApiError) {
    return error.message
  }

  return fallbackMessage
}
