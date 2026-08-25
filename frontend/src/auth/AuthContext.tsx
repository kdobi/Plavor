import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react'
import type { ReactNode } from 'react'
import {
  fetchMe,
  login,
  loginWithKakao,
  logoutSession,
  refreshAccessToken,
  signup,
} from '../api/auth'
import {
  clearStoredAccessToken,
  getStoredAccessToken,
  storeAccessToken,
} from './authStorage'
import { AuthContext } from './auth-state'
import type { AuthContextValue } from './auth-state'
import type {
  AuthTokenResponse,
  AuthUser,
  LoginRequest,
  SignupRequest,
} from '../types/auth'

const TOKEN_REFRESH_BUFFER_SECONDS = 60

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(() =>
    getStoredAccessToken(),
  )
  const [user, setUser] = useState<AuthUser | null>(null)
  const [isInitializing, setIsInitializing] = useState(true)

  const clearSession = useCallback(() => {
    clearStoredAccessToken()
    setAccessToken(null)
    setUser(null)
  }, [])

  const applyAuthResponse = useCallback((response: AuthTokenResponse) => {
    storeAccessToken(response.accessToken)
    setAccessToken(response.accessToken)
    setUser(response.user)
  }, [])

  useEffect(() => {
    let isActive = true
    const controller = new AbortController()

    async function restoreSession() {
      setIsInitializing(true)

      try {
        const storedAccessToken = getStoredAccessToken()
        if (storedAccessToken) {
          try {
            const me = await fetchMe(storedAccessToken, controller.signal)
            if (isActive) {
              setAccessToken(storedAccessToken)
              setUser(me)
            }
            return
          } catch {
            clearStoredAccessToken()
          }
        }

        const response = await refreshAccessToken(controller.signal)
        if (isActive) {
          applyAuthResponse(response)
        }
      } catch {
        if (isActive) {
          clearSession()
        }
      } finally {
        if (isActive) {
          setIsInitializing(false)
        }
      }
    }

    restoreSession()

    return () => {
      isActive = false
      controller.abort()
    }
  }, [applyAuthResponse, clearSession])

  useEffect(() => {
    if (!accessToken) {
      return
    }

    const refreshDelay = getAccessTokenRefreshDelay(accessToken)
    if (refreshDelay === null) {
      return
    }

    const timeoutId = window.setTimeout(async () => {
      try {
        const response = await refreshAccessToken()
        applyAuthResponse(response)
      } catch {
        clearSession()
      }
    }, refreshDelay)

    return () => window.clearTimeout(timeoutId)
  }, [accessToken, applyAuthResponse, clearSession])

  const handleSignup = useCallback(async (request: SignupRequest) => {
    return signup(request)
  }, [])

  const handleLogin = useCallback(async (request: LoginRequest) => {
    const response = await login(request)

    applyAuthResponse(response)

    return response
  }, [applyAuthResponse])

  const handleKakaoLogin = useCallback(async (code: string, state: string) => {
    const response = await loginWithKakao({ code, state })

    applyAuthResponse(response)

    return response
  }, [applyAuthResponse])

  const logout = useCallback(() => {
    void logoutSession().catch(() => undefined)
    clearSession()
  }, [clearSession])

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken,
      user,
      isInitializing,
      signup: handleSignup,
      login: handleLogin,
      loginWithKakao: handleKakaoLogin,
      logout,
    }),
    [
      accessToken,
      handleKakaoLogin,
      handleLogin,
      handleSignup,
      isInitializing,
      logout,
      user,
    ],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

function getAccessTokenRefreshDelay(accessToken: string) {
  try {
    const [, payload] = accessToken.split('.')
    if (!payload) {
      return null
    }

    const parsedPayload = JSON.parse(decodeBase64Url(payload)) as {
      exp?: unknown
    }
    if (typeof parsedPayload.exp !== 'number') {
      return null
    }

    const refreshAt =
      parsedPayload.exp * 1000 - TOKEN_REFRESH_BUFFER_SECONDS * 1000

    return Math.max(refreshAt - Date.now(), 0)
  } catch {
    return null
  }
}

function decodeBase64Url(value: string) {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')

  return window.atob(padded)
}
