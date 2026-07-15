import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { loginSchema, type LoginFormValues } from '../schemas'
import { useAuth } from '../hooks'
import { Button } from '../../../components/ui/Button'
import { FormField, Input } from '../../../components/ui/FormField'
import { ApiErrorAlert } from '../../../components/feedback/ApiErrorAlert'
import { Card, CardContent } from '../../../components/ui/Card'

interface LoginFormProps {
  onSuccess?: () => void
}

export function LoginForm({ onSuccess }: LoginFormProps) {
  const { login } = useAuth()
  const [submitError, setSubmitError] = useState<unknown>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      identifier: '',
      password: '',
    },
  })

  const onSubmit = handleSubmit(async (values) => {
    setSubmitError(null)
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

  return (
    <Card className="w-full max-w-md overflow-hidden rounded-[2rem] border border-slate-200/70 bg-white/95 shadow-[0_32px_80px_rgba(15,23,42,0.12)]">
      <CardContent className="space-y-8 px-8 py-10 sm:px-10 sm:py-12">
        <div className="flex flex-col items-center gap-4 text-center">
          <div className="flex h-20 w-20 items-center justify-center rounded-[1.75rem] bg-slate-100 shadow-sm">
            <img
              src="/assets/logo/ExoticStamp_logo2.png"
              alt="Exotic Stamp logo"
              className="h-12 w-12 rounded-[1.5rem] object-contain"
            />
          </div>
          <div className="space-y-2">
            <h1 className="text-3xl font-semibold text-slate-900">Exotic Stamp</h1>
            <p className="text-sm text-slate-500">Admin Console Sign In</p>
          </div>
        </div>

        <form className="space-y-5" onSubmit={onSubmit} noValidate>
          <FormField label="Email or username" htmlFor="identifier" error={errors.identifier?.message}>
            <Input
              id="identifier"
              autoComplete="username"
              placeholder="admin@exoticstamp.vn"
              disabled={isSubmitting}
              className="bg-slate-50"
              {...register('identifier')}
            />
          </FormField>

          <FormField label="Password" htmlFor="password" error={errors.password?.message}>
            <Input
              id="password"
              type="password"
              autoComplete="current-password"
              placeholder="••••••••"
              disabled={isSubmitting}
              className="bg-slate-50"
              {...register('password')}
            />
          </FormField>

          {submitError ? <ApiErrorAlert error={submitError} /> : null}

          <Button
            type="submit"
            size="md"
            className="w-full rounded-2xl bg-primary text-white shadow-lg shadow-sky-500/10 hover:bg-primary/90"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Signing in…' : 'Sign in'}
          </Button>
        </form>

        <div className="space-y-4">
          <div className="relative">
            <div className="absolute inset-x-8 top-1/2 h-px bg-slate-200" />
            <div className="relative flex justify-center text-xs uppercase tracking-[0.28em] text-slate-400">
              or
            </div>
          </div>
          <div className="text-center">
            <a href="#" className="text-sm font-medium text-primary hover:underline">
              Forgot password?
            </a>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
