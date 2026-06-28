import type { LucideIcon } from 'lucide-react'
import { Card, CardContent } from '../../../components/ui/Card'
import { cn } from '../../../lib/utils/cn'

interface MetricCardProps {
  label: string
  value: string
  hint?: string
  icon?: LucideIcon
  accent?: 'default' | 'success' | 'warning' | 'danger'
}

const accentClasses: Record<NonNullable<MetricCardProps['accent']>, string> = {
  default: 'border-l-primary',
  success: 'border-l-emerald-500',
  warning: 'border-l-amber-500',
  danger: 'border-l-destructive',
}

export function MetricCard({ label, value, hint, icon: Icon, accent = 'default' }: MetricCardProps) {
  return (
    <Card className={cn('border-l-4', accentClasses[accent])}>
      <CardContent className="flex items-start justify-between gap-3 p-4">
        <div className="min-w-0 space-y-1">
          <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            {label}
          </p>
          <p className="text-2xl font-semibold text-foreground">{value}</p>
          {hint ? <p className="text-xs text-muted-foreground">{hint}</p> : null}
        </div>
        {Icon ? (
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-md bg-secondary text-muted-foreground">
            <Icon className="h-4 w-4" />
          </div>
        ) : null}
      </CardContent>
    </Card>
  )
}
