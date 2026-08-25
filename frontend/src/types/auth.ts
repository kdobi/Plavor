export type UserRole = 'USER' | 'ADMIN'
export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'DELETED'

export interface AuthUser {
  id: number
  email: string
  name: string
  phone: string | null
  role: UserRole
  status: UserStatus
}

export interface SignupRequest {
  email: string
  password: string
  name: string
  phone?: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface KakaoLoginRequest {
  code: string
  state: string
}

export interface KakaoLoginUrlResponse {
  authorizationUrl: string
}

export interface AuthTokenResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  user: AuthUser
}

export interface ApiErrorResponse {
  code: string
  message: string
  errors: Array<{
    field: string
    message: string
  }>
}
