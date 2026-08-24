import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react'
import type { ReactNode } from 'react'
import { fetchMe, login, loginWithKakao, signup } from '../api/auth'
import {
  clearStoredAccessToken,
  getStoredAccessToken,
  storeAccessToken,
} from './authStorage'
import { AuthContext } from './auth-state'
import type { AuthContextValue } from './auth-state'
import type { AuthUser, LoginRequest, SignupRequest } from '../types/auth'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(() =>
    getStoredAccessToken(),
  )
  const [user, setUser] = useState<AuthUser | null>(null)
  const [isInitializing, setIsInitializing] = useState(Boolean(accessToken))

  useEffect(() => {
    if (!accessToken) {
      return
    }

    const token = accessToken
    const controller = new AbortController()

    async function restoreSession() {
      setIsInitializing(true)

      try {
        const me = await fetchMe(token, controller.signal)
        setUser(me)
      } catch {
        clearStoredAccessToken()
        setAccessToken(null)
        setUser(null)
      } finally {
        if (!controller.signal.aborted) {
          setIsInitializing(false)
        }
      }
    }

    restoreSession()

    return () => controller.abort()
  }, [accessToken])

  const handleSignup = useCallback(async (request: SignupRequest) => {
    return signup(request)
  }, [])

  const handleLogin = useCallback(async (request: LoginRequest) => {
    const response = await login(request)

    storeAccessToken(response.accessToken)
    setAccessToken(response.accessToken)
    setUser(response.user)

    return response
  }, [])

  const handleKakaoLogin = useCallback(async (code: string) => {
    const response = await loginWithKakao({ code })

    storeAccessToken(response.accessToken)
    setAccessToken(response.accessToken)
    setUser(response.user)

    return response
  }, [])

  const logout = useCallback(() => {
    clearStoredAccessToken()
    setAccessToken(null)
    setUser(null)
  }, [])

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
