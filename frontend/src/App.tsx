import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type Category = {
  id: number
  name: string
  slug: string
  displayOrder: number
}

type ProductImage = {
  id: number
  imageUrl: string
  altText: string | null
  displayOrder: number
  thumbnail: boolean
}

type ProductStatus = 'ACTIVE' | 'SOLD_OUT' | 'HIDDEN'

type ProductSummary = {
  id: number
  categoryId: number
  categoryName: string
  categorySlug: string
  name: string
  price: number
  stockQuantity: number
  status: ProductStatus
  thumbnailImage: ProductImage | null
}

type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

const fallbackCategories: Category[] = [
  { id: 1, name: 'Tops', slug: 'tops', displayOrder: 1 },
  { id: 2, name: 'Outerwear', slug: 'outerwear', displayOrder: 2 },
  { id: 3, name: 'Bags', slug: 'bags', displayOrder: 3 },
]

const fallbackProducts: ProductSummary[] = [
  {
    id: 3,
    categoryId: 2,
    categoryName: 'Outerwear',
    categorySlug: 'outerwear',
    name: 'Daily Denim Jacket',
    price: 89000,
    stockQuantity: 0,
    status: 'SOLD_OUT',
    thumbnailImage: {
      id: 3,
      imageUrl: 'https://images.unsplash.com/photo-1543076447-215ad9ba6923',
      altText: 'Daily Denim Jacket',
      displayOrder: 1,
      thumbnail: true,
    },
  },
  {
    id: 2,
    categoryId: 2,
    categoryName: 'Outerwear',
    categorySlug: 'outerwear',
    name: 'Relaxed Zip Hoodie',
    price: 69000,
    stockQuantity: 45,
    status: 'ACTIVE',
    thumbnailImage: {
      id: 2,
      imageUrl: 'https://images.unsplash.com/photo-1556821840-3a63f95609a7',
      altText: 'Relaxed Zip Hoodie',
      displayOrder: 1,
      thumbnail: true,
    },
  },
  {
    id: 1,
    categoryId: 1,
    categoryName: 'Tops',
    categorySlug: 'tops',
    name: 'Minimal Cotton T-Shirt',
    price: 29000,
    stockQuantity: 120,
    status: 'ACTIVE',
    thumbnailImage: {
      id: 1,
      imageUrl: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab',
      altText: 'Minimal Cotton T-Shirt',
      displayOrder: 1,
      thumbnail: true,
    },
  },
]

const currencyFormatter = new Intl.NumberFormat('ko-KR')

function App() {
  const [categories, setCategories] = useState<Category[]>(fallbackCategories)
  const [products, setProducts] = useState<ProductSummary[]>([])
  const [totalProducts, setTotalProducts] = useState(0)
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(
    null,
  )
  const [searchDraft, setSearchDraft] = useState('')
  const [keyword, setKeyword] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isUsingFallback, setIsUsingFallback] = useState(false)

  useEffect(() => {
    const controller = new AbortController()

    async function loadCategories() {
      try {
        const response = await fetch('/api/categories', {
          signal: controller.signal,
        })

        if (!response.ok) {
          throw new Error('Failed to load categories')
        }

        const data = (await response.json()) as Category[]
        setCategories(data)
      } catch {
        if (!controller.signal.aborted) {
          setCategories(fallbackCategories)
          setIsUsingFallback(true)
        }
      }
    }

    loadCategories()

    return () => controller.abort()
  }, [])

  useEffect(() => {
    const controller = new AbortController()

    async function loadProducts() {
      setIsLoading(true)

      const params = new URLSearchParams({ page: '0', size: '12' })

      if (selectedCategoryId !== null) {
        params.set('categoryId', String(selectedCategoryId))
      }

      if (keyword.trim()) {
        params.set('keyword', keyword.trim())
      }

      try {
        const response = await fetch(`/api/products?${params.toString()}`, {
          signal: controller.signal,
        })

        if (!response.ok) {
          throw new Error('Failed to load products')
        }

        const data = (await response.json()) as PageResponse<ProductSummary>
        setProducts(data.content)
        setTotalProducts(data.totalElements)
        setIsUsingFallback(false)
      } catch {
        if (!controller.signal.aborted) {
          const filteredProducts = filterFallbackProducts(
            selectedCategoryId,
            keyword,
          )

          setProducts(filteredProducts)
          setTotalProducts(filteredProducts.length)
          setIsUsingFallback(true)
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      }
    }

    loadProducts()

    return () => controller.abort()
  }, [keyword, selectedCategoryId])

  const selectedCategoryName = useMemo(() => {
    if (selectedCategoryId === null) {
      return '추천'
    }

    return (
      categories.find((category) => category.id === selectedCategoryId)?.name ??
      '카테고리'
    )
  }, [categories, selectedCategoryId])

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setKeyword(searchDraft)
  }

  return (
    <div className="storefront">
      <header className="site-header">
        <a className="brand" href="/" aria-label="Plavor home">
          PLAVOR
        </a>
        <nav className="top-nav" aria-label="Primary navigation">
          <a href="#products">Shop</a>
          <a href="#new">New</a>
          <a href="#journal">Journal</a>
        </nav>
        <div className="header-actions">
          <a href="#saved">Saved</a>
          <a href="#cart">Cart</a>
        </div>
      </header>

      <main>
        <section className="intro-band" aria-labelledby="home-title">
          <div>
            <p className="eyebrow">New season edit</p>
            <h1 id="home-title">담백하게 고른 데일리웨어</h1>
          </div>
          <p className="intro-copy">
            오래 입기 좋은 소재와 실루엣 중심으로 고른 Plavor의 첫 상품 셀렉션.
          </p>
        </section>

        <section className="category-strip" aria-label="Product categories">
          <button
            className={selectedCategoryId === null ? 'active' : ''}
            type="button"
            onClick={() => setSelectedCategoryId(null)}
          >
            추천
          </button>
          {categories.map((category) => (
            <button
              className={selectedCategoryId === category.id ? 'active' : ''}
              key={category.id}
              type="button"
              onClick={() => setSelectedCategoryId(category.id)}
            >
              {category.name}
            </button>
          ))}
        </section>

        <section className="product-section" id="products">
          <div className="section-toolbar">
            <div>
              <p className="eyebrow">{selectedCategoryName}</p>
              <h2>상품</h2>
            </div>
            <form className="search-form" onSubmit={handleSearch}>
              <label htmlFor="product-search">검색</label>
              <input
                id="product-search"
                type="search"
                value={searchDraft}
                placeholder="hoodie, denim"
                onChange={(event) => setSearchDraft(event.target.value)}
              />
              <button type="submit">Search</button>
            </form>
          </div>

          <div className="result-meta">
            <span>{totalProducts} items</span>
            {isUsingFallback && <span>Preview data</span>}
          </div>

          {isLoading ? (
            <ProductSkeletonGrid />
          ) : products.length > 0 ? (
            <div className="product-grid">
              {products.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <p>조건에 맞는 상품이 없습니다.</p>
            </div>
          )}
        </section>
      </main>
    </div>
  )
}

function ProductCard({ product }: { product: ProductSummary }) {
  return (
    <article className="product-card">
      <a className="product-link" href={`/products/${product.id}`}>
        <div className="image-wrap">
          {product.thumbnailImage ? (
            <img
              src={formatImageUrl(product.thumbnailImage.imageUrl)}
              alt={product.thumbnailImage.altText ?? product.name}
              loading="lazy"
            />
          ) : (
            <div className="image-fallback">{product.categoryName}</div>
          )}
          {product.status === 'SOLD_OUT' && (
            <span className="status-badge">Sold out</span>
          )}
        </div>
        <div className="product-info">
          <div>
            <p className="category-name">{product.categoryName}</p>
            <h3>{product.name}</h3>
          </div>
          <div className="price-row">
            <strong>{currencyFormatter.format(product.price)}원</strong>
            <span>{product.stockQuantity > 0 ? '구매 가능' : '재입고 대기'}</span>
          </div>
        </div>
      </a>
    </article>
  )
}

function ProductSkeletonGrid() {
  return (
    <div className="product-grid" aria-label="상품을 불러오는 중">
      {Array.from({ length: 6 }, (_, index) => (
        <div className="product-card skeleton-card" key={index}>
          <div className="skeleton-image" />
          <div className="skeleton-line wide" />
          <div className="skeleton-line" />
        </div>
      ))}
    </div>
  )
}

function filterFallbackProducts(categoryId: number | null, keyword: string) {
  const normalizedKeyword = keyword.trim().toLowerCase()

  return fallbackProducts.filter((product) => {
    const matchesCategory =
      categoryId === null || product.categoryId === categoryId
    const matchesKeyword =
      !normalizedKeyword ||
      product.name.toLowerCase().includes(normalizedKeyword)

    return matchesCategory && matchesKeyword
  })
}

function formatImageUrl(imageUrl: string) {
  const params = 'auto=format&fit=crop&w=720&q=86'
  return imageUrl.includes('?') ? `${imageUrl}&${params}` : `${imageUrl}?${params}`
}

export default App
