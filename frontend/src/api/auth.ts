import type {
  ApiErrorResponse,
  AuthTokenResponse,
  AuthUser,
  KakaoLoginRequest,
  KakaoLoginUrlResponse,
  LoginRequest,
  SignupRequest,
} from '../types/auth'

export class ApiError extends Error {
  status: number
  code?: string
  fieldErrors: ApiErrorResponse['errors']

  constructor(
    message: string,
    status: number,
    code?: string,
    fieldErrors: ApiErrorResponse['errors'] = [],
  ) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.fieldErrors = fieldErrors
  }
}

export async function signup(request: SignupRequest) {
  const response = await fetch('/api/auth/signup', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  return parseResponse<AuthUser>(response)
}

export async function login(request: LoginRequest) {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  return parseResponse<AuthTokenResponse>(response)
}

export async function fetchKakaoLoginUrl() {
  const response = await fetch('/api/auth/kakao/login-url', {
    credentials: 'same-origin',
  })

  return parseResponse<KakaoLoginUrlResponse>(response)
}

export async function loginWithKakao(request: KakaoLoginRequest) {
  const response = await fetch('/api/auth/kakao/login', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  return parseResponse<AuthTokenResponse>(response)
}

export async function refreshAccessToken(signal?: AbortSignal) {
  const response = await fetch('/api/auth/refresh', {
    method: 'POST',
    credentials: 'same-origin',
    signal,
  })

  return parseResponse<AuthTokenResponse>(response)
}

export async function logoutSession() {
  const response = await fetch('/api/auth/logout', {
    method: 'POST',
    credentials: 'same-origin',
  })

  if (!response.ok) {
    await parseResponse<never>(response)
  }
}

export async function fetchMe(accessToken: string, signal?: AbortSignal) {
  const response = await fetch('/api/members/me', {
    credentials: 'same-origin',
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
    signal,
  })

  return parseResponse<AuthUser>(response)
}

async function parseResponse<T>(response: Response) {
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
