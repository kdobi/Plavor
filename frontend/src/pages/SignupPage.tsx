import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/auth'
import { useAuth } from '../auth/auth-state'

export function SignupPage() {
  const navigate = useNavigate()
  const { signup, login, user } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [message, setMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const passwordMismatch = useMemo(() => {
    return passwordConfirm.length > 0 && password !== passwordConfirm
  }, [password, passwordConfirm])

  if (user) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setMessage('')

    if (password !== passwordConfirm) {
      setMessage('비밀번호가 서로 다릅니다.')
      return
    }

    setIsSubmitting(true)

    try {
      await signup({
        email,
        password,
        name,
        phone: phone.trim() || undefined,
      })
      await login({ email, password })
      navigate('/', { replace: true })
    } catch (error) {
      if (error instanceof ApiError) {
        const firstFieldError = error.fieldErrors[0]?.message
        setMessage(firstFieldError ?? error.message)
      } else {
        setMessage('회원가입 중 문제가 발생했습니다.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="auth-page signup-page">
      <section className="auth-panel" aria-labelledby="signup-title">
        <Link className="auth-brand compact" to="/">
          PLAVOR
        </Link>
        <div className="auth-heading">
          <p className="eyebrow">Create account</p>
          <h1 id="signup-title">회원가입</h1>
          <p>이메일 계정으로 Plavor의 쇼핑 흐름을 시작하세요.</p>
        </div>

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
            <span>이름</span>
            <input
              autoComplete="name"
              required
              type="text"
              value={name}
              placeholder="김동빈"
              onChange={(event) => setName(event.target.value)}
            />
          </label>

          <label>
            <span>휴대폰 번호</span>
            <input
              autoComplete="tel"
              inputMode="tel"
              type="tel"
              value={phone}
              placeholder="선택 입력"
              onChange={(event) => setPhone(event.target.value)}
            />
          </label>

          <label>
            <span>비밀번호</span>
            <input
              autoComplete="new-password"
              minLength={8}
              required
              type="password"
              value={password}
              placeholder="8자 이상"
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>

          <label>
            <span>비밀번호 확인</span>
            <input
              autoComplete="new-password"
              minLength={8}
              required
              type="password"
              value={passwordConfirm}
              placeholder="한 번 더 입력"
              onChange={(event) => setPasswordConfirm(event.target.value)}
            />
          </label>

          {passwordMismatch && (
            <p className="auth-message">비밀번호가 서로 다릅니다.</p>
          )}
          {message && <p className="auth-message">{message}</p>}

          <button
            className="auth-primary-button"
            disabled={isSubmitting || passwordMismatch}
            type="submit"
          >
            {isSubmitting ? '가입 중' : '회원가입'}
          </button>
        </form>

        <p className="auth-terms">
          가입하면 Plavor의 서비스 이용약관과 개인정보 처리방침에 동의한 것으로
          간주됩니다.
        </p>

        <p className="auth-switch">
          이미 계정이 있나요? <Link to="/login">로그인</Link>
        </p>
      </section>
    </main>
  )
}
