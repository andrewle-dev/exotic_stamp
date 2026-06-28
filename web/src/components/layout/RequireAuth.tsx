import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../features/auth/hooks'
import { ROUTES } from '../../lib/constants/routes'
import { LoadingState } from '../ui/LoadingState'

export function RequireAuth({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isInitializing } = useAuth()
  const location = useLocation()

  if (isInitializing) {
    return (
      <div className="flex min-h-dvh items-center justify-center bg-background">
        <LoadingState message="Loading admin console…" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.login} replace state={{ from: location.pathname }} />
  }

  return children
}
