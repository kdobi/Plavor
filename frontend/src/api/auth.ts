import type {
  ApiErrorResponse,
  AuthTokenResponse,
  AuthUser,
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
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  return parseResponse<AuthTokenResponse>(response)
}

export async function fetchMe(accessToken: string, signal?: AbortSignal) {
  const response = await fetch('/api/members/me', {
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
