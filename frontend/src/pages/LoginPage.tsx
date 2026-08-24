import { useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { useAuth } from '../auth/auth-state'

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login, user } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const redirectTo =
    typeof location.state === 'object' &&
    location.state !== null &&
    'from' in location.state &&
    typeof location.state.from === 'string'
      ? location.state.from
      : '/'

  if (user) {
    return <Navigate to={redirectTo} replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setMessage('')
    setIsSubmitting(true)

    try {
      await login({ email, password })
      navigate(redirectTo, { replace: true })
    } catch (error) {
      if (error instanceof ApiError) {
        setMessage(error.message)
      } else {
        setMessage('로그인 중 문제가 발생했습니다.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <AuthShell
      eyebrow="Welcome back"
      title="로그인"
      description="가입한 이메일과 비밀번호로 Plavor에 다시 들어오세요."
    >
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          <span>이메일</span>
          <input
            autoComplete="email"
            inputMode="email"
            required
            type="email"
            value={email}
            placeholder="you@example.com"
            onChange={(event) => setEmail(event.target.value)}
          />
        </label>

        <label>
          <span>비밀번호</span>
          <input
            autoComplete="current-password"
            required
            type="password"
            value={password}
            placeholder="비밀번호"
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>

        {message && <p className="auth-message">{message}</p>}

        <button className="auth-primary-button" disabled={isSubmitting} type="submit">
          {isSubmitting ? '확인 중' : '로그인'}
        </button>
      </form>

      <div className="auth-divider">
        <span>또는</span>
      </div>

      <button className="auth-social-button kakao" type="button">
        <span>●</span>
        카카오로 계속하기
      </button>

      <p className="auth-switch">
        아직 계정이 없나요? <Link to="/signup">회원가입</Link>
      </p>
    </AuthShell>
  )
}

function AuthShell({
  eyebrow,
  title,
  description,
  children,
}: {
  eyebrow: string
  title: string
  description: string
  children: ReactNode
}) {
  return (
    <main className="auth-page">
      <section className="auth-art" aria-label="Plavor account">
        <Link className="auth-brand" to="/">
          PLAVOR
        </Link>
        <div className="auth-phone-frame">
          <div className="auth-status-row">
            <span>6:20</span>
            <span>LTE</span>
          </div>
          <div className="auth-illustration">
            <div className="auth-closet-line top" />
            <div className="auth-closet-line side" />
            <div className="auth-shirt" />
            <div className="auth-bag" />
          </div>
          <div className="auth-benefit">
            <strong>첫 구매 혜택</strong>
            <span>좋아하는 취향을 담아두고 빠르게 주문하세요.</span>
          </div>
        </div>
      </section>

      <section className="auth-panel" aria-labelledby="auth-title">
        <div className="auth-heading">
          <p className="eyebrow">{eyebrow}</p>
          <h1 id="auth-title">{title}</h1>
          <p>{description}</p>
        </div>
        {children}
      </section>
    </main>
  )
}
