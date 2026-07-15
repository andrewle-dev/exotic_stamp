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
      <div className="flex h-full min-h-0 items-center justify-center bg-background">
        <LoadingState message="Checking session…" />
      </div>
    )
  }

  if (isAuthenticated) {
    return <Navigate to={redirectTo} replace />
  }

  return (
    <div className="relative min-h-screen overflow-hidden bg-slate-50">
      <div className="pointer-events-none absolute -left-32 top-0 h-72 w-72 rounded-full bg-sky-400/20 blur-3xl" />
      <div className="pointer-events-none absolute right-0 top-24 h-72 w-72 rounded-full bg-indigo-400/10 blur-3xl" />
      <div className="pointer-events-none absolute inset-y-0 right-1/2 mr-40 hidden w-72 bg-[radial-gradient(circle_at_top_left,rgba(59,130,246,0.12),transparent_40%)] lg:block" />
      <div className="relative flex min-h-screen items-center justify-center px-4 py-16">
        <LoginForm onSuccess={() => navigate(redirectTo, { replace: true })} />
      </div>
    </div>
  )
}
