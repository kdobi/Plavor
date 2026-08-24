import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { useAuth } from '../auth/auth-state'
import {
  clearStoredKakaoOAuthState,
  getStoredKakaoOAuthState,
} from '../auth/authStorage'

export function KakaoCallbackPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { loginWithKakao } = useAuth()
  const hasProcessed = useRef(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (hasProcessed.current) {
      return
    }

    hasProcessed.current = true

    async function completeKakaoLogin() {
      const kakaoError = searchParams.get('error')
      const code = searchParams.get('code')
      const returnedState = searchParams.get('state')
      const storedState = getStoredKakaoOAuthState()

      clearStoredKakaoOAuthState()

      if (kakaoError) {
        setMessage('카카오 로그인이 취소되었거나 승인되지 않았습니다.')
        return
      }

      if (!code) {
        setMessage('카카오 인증 코드를 찾지 못했습니다.')
        return
      }

      if (!storedState || storedState !== returnedState) {
        setMessage('카카오 로그인 요청이 만료되었습니다. 다시 시도해 주세요.')
        return
      }

      try {
        await loginWithKakao(code)
        navigate('/', { replace: true })
      } catch (error) {
        if (error instanceof ApiError) {
          setMessage(error.message)
        } else {
          setMessage('카카오 로그인 처리 중 문제가 발생했습니다.')
        }
      }
    }

    void completeKakaoLogin()
  }, [loginWithKakao, navigate, searchParams])

  return (
    <main className="auth-page signup-page">
      <section className="auth-panel auth-callback-panel" aria-labelledby="kakao-callback-title">
        <Link className="auth-brand compact" to="/">
          PLAVOR
        </Link>
        <div className="auth-heading">
          <p className="eyebrow">Kakao login</p>
          <h1 id="kakao-callback-title">로그인 확인 중</h1>
          <p>카카오에서 받은 인증 정보를 Plavor 계정으로 연결하고 있습니다.</p>
        </div>

        {message ? (
          <>
            <p className="auth-message">{message}</p>
            <Link className="auth-retry-link" to="/login">
              로그인 화면으로 돌아가기
            </Link>
          </>
        ) : (
          <div className="auth-status-card" role="status">
            <span className="auth-status-dot" />
            <span>잠시만 기다려 주세요.</span>
          </div>
        )}
      </section>
    </main>
  )
}
