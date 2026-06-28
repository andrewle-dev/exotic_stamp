import { AlertCircle, ShieldAlert } from 'lucide-react'
import { getErrorMessage, isForbiddenError } from '../../lib/api/errors'
import { cn } from '../../lib/utils/cn'

interface ApiErrorAlertProps {
  error: unknown
  title?: string
  className?: string
}

export function ApiErrorAlert({ error, title, className }: ApiErrorAlertProps) {
  if (error === null || error === undefined) {
    return null
  }

  const forbidden = isForbiddenError(error)
  const Icon = forbidden ? ShieldAlert : AlertCircle

  return (
    <div
      className={cn(
        'flex items-start gap-2 rounded-md border border-red-200 bg-accent px-3 py-2 text-sm text-accent-foreground',
        className,
      )}
      role="alert"
    >
      <Icon className="mt-0.5 h-4 w-4 shrink-0" />
      <div>
        {title ? <p className="font-medium">{title}</p> : null}
        <p>{getErrorMessage(error)}</p>
      </div>
    </div>
  )
}
