import { ShieldOff } from 'lucide-react'
import type { ReactNode } from 'react'
import { cn } from '../../lib/utils/cn'

interface PermissionDeniedStateProps {
  title?: string
  description?: string
  action?: ReactNode
  className?: string
}

export function PermissionDeniedState({
  title = 'Access denied',
  description = 'You do not have permission to view this resource. Contact an administrator if you believe this is a mistake.',
  action,
  className,
}: PermissionDeniedStateProps) {
  return (
    <div
      className={cn(
        'flex min-h-40 flex-col items-center justify-center gap-3 px-6 py-12 text-center',
        className,
      )}
      role="alert"
    >
      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-secondary text-muted-foreground">
        <ShieldOff className="h-5 w-5" />
      </div>
      <div className="space-y-1">
        <h3 className="text-base font-medium text-foreground">{title}</h3>
        <p className="mx-auto max-w-md text-sm text-muted-foreground">{description}</p>
      </div>
      {action}
    </div>
  )
}
