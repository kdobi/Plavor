import type {
  Category,
  PageResponse,
  ProductDetail,
  ProductSummary,
} from '../types/catalog'

export async function fetchCategories(signal?: AbortSignal) {
  const response = await fetch('/api/categories', { signal })

  if (!response.ok) {
    throw new Error('Failed to load categories')
  }

  return (await response.json()) as Category[]
}

export async function fetchProducts(
  params: {
    categoryId: number | null
    keyword: string
    page?: number
    size?: number
  },
  signal?: AbortSignal,
) {
  const searchParams = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 12),
  })

  if (params.categoryId !== null) {
    searchParams.set('categoryId', String(params.categoryId))
  }

  if (params.keyword.trim()) {
    searchParams.set('keyword', params.keyword.trim())
  }

  const response = await fetch(`/api/products?${searchParams.toString()}`, {
    signal,
  })

  if (!response.ok) {
    throw new Error('Failed to load products')
  }

  return (await response.json()) as PageResponse<ProductSummary>
}

export async function fetchProductDetail(
  productId: string,
  signal?: AbortSignal,
) {
  const response = await fetch(`/api/products/${productId}`, { signal })

  if (response.status === 404) {
    return null
  }

  if (!response.ok) {
    throw new Error('Failed to load product detail')
  }

  return (await response.json()) as ProductDetail
}
