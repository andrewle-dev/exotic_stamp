import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { LoginForm } from '../components/LoginForm'
import { useAuth } from '../hooks'
import { ROUTES } from '../../../lib/constants/routes'
import { LoadingState } from '../../../components/ui/LoadingState'

export function LoginPage() {
  const { isAuthenticated, isInitializing } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const redirectTo = (location.state as { from?: string } | null)?.from ?? ROUTES.dashboard

  if (isInitializing) {
    return (
      <div className="flex min-h-dvh items-center justify-center bg-background">
        <LoadingState message="Checking session…" />
      </div>
    )
  }

  if (isAuthenticated) {
    return <Navigate to={redirectTo} replace />
  }

  return (
    <div className="flex min-h-dvh items-center justify-center bg-background px-4">
      <LoginForm onSuccess={() => navigate(redirectTo, { replace: true })} />
    </div>
  )
}
