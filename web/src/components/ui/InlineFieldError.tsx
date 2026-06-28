import { cn } from '../../lib/utils/cn'

interface InlineFieldErrorProps {
  message?: string
  id?: string
  className?: string
}

export function InlineFieldError({ message, id, className }: InlineFieldErrorProps) {
  if (!message) {
    return null
  }

  return (
    <p id={id} className={cn('text-sm text-destructive', className)} role="alert">
      {message}
    </p>
  )
}
