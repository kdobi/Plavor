import { ApiError } from './auth'
import type { ApiErrorResponse } from '../types/auth'
import type { Order, OrderCreateRequest } from '../types/order'

export async function fetchOrders(accessToken: string, signal?: AbortSignal) {
  const response = await fetch('/api/orders', {
    headers: authHeaders(accessToken),
    signal,
  })

  return parseResponse<Order[]>(response)
}

export async function fetchOrder(
  accessToken: string,
  orderId: string,
  signal?: AbortSignal,
) {
  const response = await fetch(`/api/orders/${orderId}`, {
    headers: authHeaders(accessToken),
    signal,
  })

  return parseResponse<Order>(response)
}

export async function createOrder(
  accessToken: string,
  request: OrderCreateRequest,
) {
  const response = await fetch('/api/orders', {
    method: 'POST',
    headers: {
      ...authHeaders(accessToken),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  return parseResponse<Order>(response)
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
