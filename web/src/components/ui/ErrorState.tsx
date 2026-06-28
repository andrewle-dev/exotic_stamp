import { AlertTriangle } from 'lucide-react'
import type { ReactNode } from 'react'
import { cn } from '../../lib/utils/cn'
import { getErrorMessage } from '../../lib/api/errors'

interface ErrorStateProps {
  title?: string
  message?: string
  error?: unknown
  action?: ReactNode
  className?: string
}

export function ErrorState({
  title = 'Something went wrong',
  message,
  error,
  action,
  className,
}: ErrorStateProps) {
  const resolvedMessage = message ?? (error !== undefined ? getErrorMessage(error) : 'Please try again.')

  return (
    <div
      className={cn(
        'flex min-h-40 flex-col items-center justify-center gap-3 px-6 py-12 text-center',
        className,
      )}
      role="alert"
    >
      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-accent text-accent-foreground">
        <AlertTriangle className="h-5 w-5" />
      </div>
      <div className="space-y-1">
        <h3 className="text-base font-medium text-foreground">{title}</h3>
        <p className="mx-auto max-w-md text-sm text-muted-foreground">{resolvedMessage}</p>
      </div>
      {action}
    </div>
  )
}
