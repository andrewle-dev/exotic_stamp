import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { Train } from 'lucide-react'
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
    <Card className="w-full max-w-md shadow-md">
      <CardContent className="space-y-6">
        <div className="flex flex-col items-center gap-2 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <Train className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-xl font-semibold text-foreground">Exotic Stamp</h1>
            <p className="text-sm text-muted-foreground">Admin Console Sign In</p>
          </div>
        </div>

        <form className="space-y-4" onSubmit={onSubmit} noValidate>
          <FormField label="Email or username" htmlFor="identifier" error={errors.identifier?.message}>
            <Input
              id="identifier"
              autoComplete="username"
              placeholder="admin@exoticstamp.vn"
              disabled={isSubmitting}
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
              {...register('password')}
            />
          </FormField>

          {submitError ? <ApiErrorAlert error={submitError} /> : null}

          <Button type="submit" size="md" className="w-full" disabled={isSubmitting}>
            {isSubmitting ? 'Signing in…' : 'Sign in'}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}
