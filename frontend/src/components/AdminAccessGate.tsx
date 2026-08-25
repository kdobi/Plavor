import type { ReactNode } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/auth-state'

export function AdminAccessGate({ children }: { children: ReactNode }) {
  const location = useLocation()
  const { accessToken, isInitializing, user } = useAuth()

  if (isInitializing) {
    return <AdminAccessState eyebrow="Loading" title="권한을 확인하고 있습니다." />
  }

  if (!user || !accessToken) {
    return (
      <AdminAccessState eyebrow="Login required" title="로그인이 필요합니다.">
        <Link
          className="admin-access-link"
          to="/login"
          state={{ from: `${location.pathname}${location.search}` }}
        >
          로그인하기
        </Link>
      </AdminAccessState>
    )
  }

  if (user.role !== 'ADMIN') {
    return (
      <AdminAccessState
        eyebrow="Permission denied"
        title="관리자 권한이 필요합니다."
      >
        <Link className="admin-access-link" to="/">
          홈으로 돌아가기
        </Link>
      </AdminAccessState>
    )
  }

  return children
}

function AdminAccessState({
  children,
  eyebrow,
  title,
}: {
  children?: ReactNode
  eyebrow: string
  title: string
}) {
  return (
    <section className="admin-access-state">
      <p className="eyebrow">{eyebrow}</p>
      <h1>{title}</h1>
      {children}
    </section>
  )
}
