import { createContext, useContext } from 'react'
import type {
  AuthTokenResponse,
  AuthUser,
  LoginRequest,
  SignupRequest,
} from '../types/auth'

export interface AuthContextValue {
  accessToken: string | null
  user: AuthUser | null
  isInitializing: boolean
  signup: (request: SignupRequest) => Promise<AuthUser>
  login: (request: LoginRequest) => Promise<AuthTokenResponse>
  loginWithKakao: (code: string) => Promise<AuthTokenResponse>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider.')
  }

  return context
}
