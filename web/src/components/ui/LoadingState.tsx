import { Loader2 } from 'lucide-react'
import { cn } from '../../lib/utils/cn'

interface LoadingStateProps {
  message?: string
  className?: string
}

export function LoadingState({ message = 'Loading…', className }: LoadingStateProps) {
  return (
    <div
      className={cn(
        'flex min-h-40 flex-col items-center justify-center gap-2 text-sm text-muted-foreground',
        className,
      )}
      role="status"
      aria-live="polite"
    >
      <Loader2 className="h-5 w-5 animate-spin" />
      <span>{message}</span>
    </div>
  )
}
