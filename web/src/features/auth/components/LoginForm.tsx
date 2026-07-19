import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { ArrowLeft, Eye, EyeOff } from 'lucide-react'
import {
  forgotPasswordSchema,
  loginSchema,
  resetPasswordSchema,
  type ForgotPasswordFormValues,
  type LoginFormValues,
  type ResetPasswordFormValues,
} from '../schemas'
import { useAuth } from '../hooks'
import { requestPasswordReset, resetPassword as resetPasswordApi } from '../api'
import { Button } from '../../../components/ui/Button'
import { FormField, Input } from '../../../components/ui/FormField'
import { ApiErrorAlert } from '../../../components/feedback/ApiErrorAlert'
import { Card, CardContent } from '../../../components/ui/Card'

interface LoginFormProps {
  onSuccess?: () => void
}

type AuthMode = 'login' | 'forgot' | 'reset'

export function LoginForm({ onSuccess }: LoginFormProps) {
  const { login } = useAuth()
  const [submitError, setSubmitError] = useState<unknown>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [mode, setMode] = useState<AuthMode>('login')
  const [showPassword, setShowPassword] = useState(false)
  const [showNewPassword, setShowNewPassword] = useState(false)

  const loginForm = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      identifier: '',
      password: '',
    },
  })

  const forgotForm = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: {
      email: '',
    },
  })

  const resetForm = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      email: '',
      otp: '',
      newPassword: '',
      confirmPassword: '',
    },
  })

  const onLoginSubmit = loginForm.handleSubmit(async (values) => {
    setSubmitError(null)
    setSuccessMessage(null)
    try {
      await login({
        identifier: values.identifier,
        password: values.password,
      })
      onSuccess?.()
    } catch (error) {
      setSubmitError(error)
    }
  })

  const onForgotSubmit = forgotForm.handleSubmit(async (values) => {
    setSubmitError(null)
    setSuccessMessage(null)
    try {
      await requestPasswordReset({ email: values.email })
      resetForm.reset({ email: values.email, otp: '', newPassword: '', confirmPassword: '' })
      setSuccessMessage('If the account exists, a 6-digit code has been sent to your email. Enter it below to continue.')
      setMode('reset')
    } catch (error) {
      setSubmitError(error)
    }
  })

  const onResetSubmit = resetForm.handleSubmit(async (values) => {
    setSubmitError(null)
    setSuccessMessage(null)
    try {
      await resetPasswordApi({
        email: values.email,
        otp: values.otp,
        newPassword: values.newPassword,
      })
      loginForm.reset({ identifier: '', password: '' })
      forgotForm.reset({ email: '' })
      resetForm.reset({ email: '', otp: '', newPassword: '', confirmPassword: '' })
      setSuccessMessage('Password updated successfully. You can now sign in with your new password.')
      setMode('login')
    } catch (error) {
      setSubmitError(error)
    }
  })

  const headingText = mode === 'forgot' ? 'Reset your password' : mode === 'reset' ? 'Create a new password' : 'Welcome back'
  const subheadingText =
    mode === 'forgot'
      ? 'Enter your email to receive a reset code.'
      : mode === 'reset'
        ? 'Enter the 6-digit code sent to your inbox.'
        : 'Sign in to the Exotic Stamp admin console'

  return (
    <Card className="w-full max-w-md overflow-hidden rounded-[2rem] border border-slate-200/80 bg-white shadow-[0_20px_60px_rgba(15,23,42,0.08)]">
      <CardContent className="space-y-7 px-7 py-8 sm:px-8 sm:py-9">
        <div className="flex flex-col items-center gap-4 text-center">
          <div className="flex h-20 w-20 items-center justify-center rounded-[1.75rem] bg-slate-100 shadow-sm">
            <img
              src="/assets/logo/ExoticStamp_logo2.png"
              alt="Exotic Stamp logo"
              className="h-12 w-12 rounded-[1.45rem] object-contain"
            />
          </div>
          <div className="space-y-2">
            <h1 className="text-2xl font-semibold text-slate-900">{headingText}</h1>
            <p className="text-sm text-slate-500">{subheadingText}</p>
          </div>
        </div>

        {mode === 'login' ? (
          <form className="space-y-5" onSubmit={onLoginSubmit} noValidate>
            <FormField label="Email or username" htmlFor="identifier" error={loginForm.formState.errors.identifier?.message}>
              <Input
                id="identifier"
                autoComplete="username"
                placeholder="Enter your email or username"
                disabled={loginForm.formState.isSubmitting}
                className="h-11 bg-slate-50"
                {...loginForm.register('identifier')}
              />
            </FormField>

            <FormField label="Password" htmlFor="password" error={loginForm.formState.errors.password?.message}>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  placeholder="Enter your password"
                  disabled={loginForm.formState.isSubmitting}
                  className="h-11 bg-slate-50 pr-11"
                  {...loginForm.register('password')}
                />
                <button
                  type="button"
                  aria-label="Toggle password visibility"
                  className="absolute inset-y-0 right-3 flex items-center text-slate-400 transition hover:text-slate-600"
                  onClick={() => setShowPassword((value) => !value)}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </FormField>

            {submitError ? <ApiErrorAlert error={submitError} /> : null}
            {successMessage ? (
              <div className="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
                {successMessage}
              </div>
            ) : null}

            <Button
              type="submit"
              size="md"
              className="w-full rounded-2xl bg-primary text-white shadow-lg shadow-sky-500/10 hover:bg-primary/90"
              disabled={loginForm.formState.isSubmitting}
            >
              {loginForm.formState.isSubmitting ? 'Signing in…' : 'Sign in'}
            </Button>
          </form>
        ) : null}

        {mode === 'forgot' ? (
          <form className="space-y-5" onSubmit={onForgotSubmit} noValidate>
            <FormField label="Email" htmlFor="forgot-email" error={forgotForm.formState.errors.email?.message}>
              <Input
                id="forgot-email"
                type="email"
                autoComplete="email"
                placeholder="Enter your email"
                disabled={forgotForm.formState.isSubmitting}
                className="h-11 bg-slate-50"
                {...forgotForm.register('email')}
              />
            </FormField>

            {submitError ? <ApiErrorAlert error={submitError} /> : null}
            {successMessage ? (
              <div className="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
                {successMessage}
              </div>
            ) : null}

            <div className="flex flex-col gap-3 sm:flex-row">
              <Button
                type="button"
                variant="secondary"
                size="md"
                className="w-full"
                onClick={() => {
                  setMode('login')
                  setSubmitError(null)
                  setSuccessMessage(null)
                }}
              >
                <ArrowLeft className="h-4 w-4" /> Back
              </Button>
              <Button type="submit" size="md" className="w-full rounded-2xl bg-primary text-white hover:bg-primary/90" disabled={forgotForm.formState.isSubmitting}>
                {forgotForm.formState.isSubmitting ? 'Sending…' : 'Send reset code'}
              </Button>
            </div>
          </form>
        ) : null}

        {mode === 'reset' ? (
          <form className="space-y-5" onSubmit={onResetSubmit} noValidate>
            <FormField label="Email" htmlFor="reset-email" error={resetForm.formState.errors.email?.message}>
              <Input
                id="reset-email"
                type="email"
                autoComplete="email"
                placeholder="Enter your email"
                disabled={resetForm.formState.isSubmitting}
                className="h-11 bg-slate-50"
                {...resetForm.register('email')}
              />
            </FormField>

            <FormField label="OTP" htmlFor="otp" error={resetForm.formState.errors.otp?.message}>
              <Input
                id="otp"
                inputMode="numeric"
                autoComplete="one-time-code"
                placeholder="Enter 6-digit code"
                disabled={resetForm.formState.isSubmitting}
                className="h-11 bg-slate-50"
                {...resetForm.register('otp')}
              />
            </FormField>

            <FormField label="New password" htmlFor="newPassword" error={resetForm.formState.errors.newPassword?.message}>
              <div className="relative">
                <Input
                  id="newPassword"
                  type={showNewPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="Enter a new password"
                  disabled={resetForm.formState.isSubmitting}
                  className="h-11 bg-slate-50 pr-11"
                  {...resetForm.register('newPassword')}
                />
                <button
                  type="button"
                  aria-label="Toggle password visibility"
                  className="absolute inset-y-0 right-3 flex items-center text-slate-400 transition hover:text-slate-600"
                  onClick={() => setShowNewPassword((value) => !value)}
                >
                  {showNewPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </FormField>

            <FormField label="Confirm password" htmlFor="confirmPassword" error={resetForm.formState.errors.confirmPassword?.message}>
              <Input
                id="confirmPassword"
                type={showNewPassword ? 'text' : 'password'}
                autoComplete="new-password"
                placeholder="Confirm your password"
                disabled={resetForm.formState.isSubmitting}
                className="h-11 bg-slate-50"
                {...resetForm.register('confirmPassword')}
              />
            </FormField>

            {submitError ? <ApiErrorAlert error={submitError} /> : null}
            {successMessage ? (
              <div className="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
                {successMessage}
              </div>
            ) : null}

            <div className="flex flex-col gap-3 sm:flex-row">
              <Button
                type="button"
                variant="secondary"
                size="md"
                className="w-full"
                onClick={() => {
                  setMode('forgot')
                  setSubmitError(null)
                  setSuccessMessage(null)
                }}
              >
                <ArrowLeft className="h-4 w-4" /> Back
              </Button>
              <Button type="submit" size="md" className="w-full rounded-2xl bg-primary text-white hover:bg-primary/90" disabled={resetForm.formState.isSubmitting}>
                {resetForm.formState.isSubmitting ? 'Updating…' : 'Update password'}
              </Button>
            </div>
          </form>
        ) : null}

        {mode === 'login' ? (
          <div className="text-center">
            <button
              type="button"
              className="text-sm font-medium text-primary transition hover:underline"
              onClick={() => {
                setMode('forgot')
                setSubmitError(null)
                setSuccessMessage(null)
              }}
            >
              Forgot password?
            </button>
          </div>
        ) : null}
      </CardContent>
    </Card>
  )
}
