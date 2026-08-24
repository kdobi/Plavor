import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { fetchCategories, fetchProducts } from '../api/catalog'
import { ProductCard } from '../components/ProductCard'
import { ProductSkeletonGrid } from '../components/ProductSkeletonGrid'
import { SiteHeader } from '../components/SiteHeader'
import {
  fallbackCategories,
  filterFallbackProducts,
} from '../data/fallbackCatalog'
import type { Category, ProductSummary } from '../types/catalog'

export function HomePage() {
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
        const data = await fetchCategories(controller.signal)
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

      try {
        const data = await fetchProducts(
          {
            categoryId: selectedCategoryId,
            keyword,
            page: 0,
            size: 12,
          },
          controller.signal,
        )

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
      <SiteHeader />

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
