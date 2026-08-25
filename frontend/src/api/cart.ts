import { ApiError } from './auth'
import type {
  Cart,
  CartItemAddRequest,
  CartItemUpdateRequest,
} from '../types/cart'
import type { ApiErrorResponse } from '../types/auth'

export async function fetchCart(accessToken: string, signal?: AbortSignal) {
  const response = await fetch('/api/cart', {
    headers: authHeaders(accessToken),
    signal,
  })

  return parseResponse<Cart>(response)
}

export async function addCartItem(
  accessToken: string,
  request: CartItemAddRequest,
) {
  const response = await fetch('/api/cart/items', {
    method: 'POST',
    headers: {
      ...authHeaders(accessToken),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  return parseResponse<Cart>(response)
}

export async function updateCartItem(
  accessToken: string,
  itemId: number,
  request: CartItemUpdateRequest,
) {
  const response = await fetch(`/api/cart/items/${itemId}`, {
    method: 'PATCH',
    headers: {
      ...authHeaders(accessToken),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  return parseResponse<Cart>(response)
}

export async function deleteCartItem(accessToken: string, itemId: number) {
  const response = await fetch(`/api/cart/items/${itemId}`, {
    method: 'DELETE',
    headers: authHeaders(accessToken),
  })

  if (!response.ok) {
    await throwApiError(response)
  }
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

  return throwApiError(response)
}

async function throwApiError(response: Response): Promise<never> {
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
