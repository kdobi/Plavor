import { ApiError } from './auth'
import type { ApiErrorResponse } from '../types/auth'
import type {
  AdminOrder,
  AdminOrderPage,
  AdminOrderStatusUpdateRequest,
  AdminProduct,
  AdminProductPage,
  AdminProductRequest,
  AdminProductStatusUpdateRequest,
} from '../types/admin'
import type { ProductStatus } from '../types/catalog'
import type { OrderStatus } from '../types/order'

export async function fetchAdminProducts(
  accessToken: string,
  params: {
    categoryId: number | null
    status: ProductStatus | null
    keyword: string
    page?: number
    size?: number
  },
  signal?: AbortSignal,
) {
  const searchParams = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 20),
  })

  if (params.categoryId !== null) {
    searchParams.set('categoryId', String(params.categoryId))
  }

  if (params.status !== null) {
    searchParams.set('status', params.status)
  }

  if (params.keyword.trim()) {
    searchParams.set('keyword', params.keyword.trim())
  }

  const response = await fetch(`/api/admin/products?${searchParams.toString()}`, {
    headers: authHeaders(accessToken),
    signal,
  })

  return parseResponse<AdminProductPage>(response)
}

export async function fetchAdminProduct(
  accessToken: string,
  productId: string,
  signal?: AbortSignal,
) {
  const response = await fetch(`/api/admin/products/${productId}`, {
    headers: authHeaders(accessToken),
    signal,
  })

  return parseResponse<AdminProduct>(response)
}

export async function createAdminProduct(
  accessToken: string,
  request: AdminProductRequest,
) {
  const response = await fetch('/api/admin/products', {
    method: 'POST',
    headers: {
      ...authHeaders(accessToken),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  return parseResponse<AdminProduct>(response)
}

export async function updateAdminProduct(
  accessToken: string,
  productId: string,
  request: AdminProductRequest,
) {
  const response = await fetch(`/api/admin/products/${productId}`, {
    method: 'PUT',
    headers: {
      ...authHeaders(accessToken),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  return parseResponse<AdminProduct>(response)
}

export async function updateAdminProductStatus(
  accessToken: string,
  productId: number,
  request: AdminProductStatusUpdateRequest,
) {
  const response = await fetch(`/api/admin/products/${productId}/status`, {
    method: 'PATCH',
    headers: {
      ...authHeaders(accessToken),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  return parseResponse<AdminProduct>(response)
}

export async function fetchAdminOrders(
  accessToken: string,
  params: {
    status: OrderStatus | null
    keyword: string
    page?: number
    size?: number
  },
  signal?: AbortSignal,
) {
  const searchParams = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 20),
  })

  if (params.status !== null) {
    searchParams.set('status', params.status)
  }

  if (params.keyword.trim()) {
    searchParams.set('keyword', params.keyword.trim())
  }

  const response = await fetch(`/api/admin/orders?${searchParams.toString()}`, {
    headers: authHeaders(accessToken),
    signal,
  })

  return parseResponse<AdminOrderPage>(response)
}

export async function fetchAdminOrder(
  accessToken: string,
  orderId: string,
  signal?: AbortSignal,
) {
  const response = await fetch(`/api/admin/orders/${orderId}`, {
    headers: authHeaders(accessToken),
    signal,
  })

  return parseResponse<AdminOrder>(response)
}

export async function updateAdminOrderStatus(
  accessToken: string,
  orderId: number,
  request: AdminOrderStatusUpdateRequest,
) {
  const response = await fetch(`/api/admin/orders/${orderId}/status`, {
    method: 'PATCH',
    headers: {
      ...authHeaders(accessToken),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  return parseResponse<AdminOrder>(response)
}

function authHeaders(accessToken: string) {
  return {
    Authorization: `Bearer ${accessToken}`,
  }
}

async function parseResponse<T>(response: Response): Promise<T> {
  if (response.ok) {
    return (await response.json()) as T
  }

  const error = (await response.json().catch(() => null)) as
    | ApiErrorResponse
    | null

  if (!error) {
    throw new ApiError('요청을 처리하지 못했습니다.', response.status)
  }

  throw new ApiError(
    error.message || '요청을 처리하지 못했습니다.',
    response.status,
    error.code,
    error.errors,
  )
}
