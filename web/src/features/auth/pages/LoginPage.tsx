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
    <div className="relative min-h-screen overflow-hidden bg-[linear-gradient(135deg,#f8fbff_0%,#eef4ff_45%,#f8fafc_100%)]">
      <div className="pointer-events-none absolute left-[-8rem] top-[-6rem] h-72 w-72 rounded-full bg-sky-400/25 blur-3xl" />
      <div className="pointer-events-none absolute right-[-6rem] top-16 h-80 w-80 rounded-full bg-indigo-400/15 blur-3xl" />
      <div className="pointer-events-none absolute bottom-[-4rem] left-1/3 h-64 w-64 rounded-full bg-cyan-300/15 blur-3xl" />
      <div className="pointer-events-none absolute inset-y-0 right-[15%] hidden w-72 bg-[radial-gradient(circle_at_top_left,rgba(59,130,246,0.16),transparent_60%)] lg:block" />

      <div className="relative flex min-h-screen items-center justify-center px-4 py-12 sm:px-6 lg:px-8">
        <div className="w-full max-w-6xl rounded-[2rem] border border-white/70 bg-white/70 p-3 shadow-[0_30px_90px_rgba(15,23,42,0.10)] backdrop-blur-xl sm:p-4 lg:p-5">
          <div className="grid overflow-hidden rounded-[1.6rem] bg-white shadow-[0_20px_55px_rgba(15,23,42,0.08)] lg:grid-cols-[1.1fr_0.9fr]">
            <div className="hidden bg-[linear-gradient(135deg,#0f172a_0%,#1e3a8a_50%,#2563eb_100%)] p-12 lg:flex lg:flex-col lg:justify-between">
              <div className="space-y-5">
                <div className="inline-flex items-center rounded-full border border-white/20 bg-white/10 px-3 py-1 text-sm font-medium text-white/90">
                  Exotic Stamp Admin
                </div>
                <div className="space-y-4">
                  <h2 className="max-w-sm text-3xl font-semibold leading-tight text-white">
                    Manage campaigns, stations, and rewards from a single place.
                  </h2>
                  <p className="max-w-md text-sm leading-7 text-slate-200">
                    Powerful tools for operations teams to launch and monitor every stamp experience.
                  </p>
                </div>
              </div>
              <div className="rounded-2xl border border-white/20 bg-white/10 p-4 text-sm text-slate-100">
                Trusted by retail and metro teams to run delightful, connected promotions.
              </div>
            </div>

            <div className="flex items-center justify-center bg-slate-50/80 p-6 sm:p-10 lg:p-12">
              <LoginForm onSuccess={() => navigate(redirectTo, { replace: true })} />
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
