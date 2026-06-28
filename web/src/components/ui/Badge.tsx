import { cn } from '../../lib/utils/cn'

const statusClasses: Record<string, string> = {
  ACTIVE: 'border-emerald-200 bg-emerald-50 text-emerald-700',
  DRAFT: 'border-border bg-secondary text-muted-foreground',
  INACTIVE: 'border-orange-200 bg-orange-50 text-orange-600',
  ARCHIVED: 'border-slate-200 bg-slate-100 text-slate-500',
  READY: 'border-emerald-200 bg-emerald-50 text-emerald-700',
  NOT_READY: 'border-red-200 bg-accent text-accent-foreground',
}

interface BadgeProps {
  status: string
  label?: string
  dot?: boolean
  className?: string
}

export function Badge({ status, label, dot, className }: BadgeProps) {
  const cls = statusClasses[status] ?? 'border-border bg-secondary text-muted-foreground'
  const text = label ?? status.replace(/_/g, ' ')

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 rounded border px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide whitespace-nowrap',
        cls,
        className,
      )}
    >
      {dot ? (
        <span
          className={cn(
            'h-1.5 w-1.5 rounded-full',
            cls.includes('emerald')
              ? 'bg-emerald-500'
              : cls.includes('orange')
                ? 'bg-orange-500'
                : cls.includes('red') || cls.includes('accent')
                  ? 'bg-destructive'
                  : 'bg-muted-foreground',
          )}
        />
      ) : null}
      {text}
    </span>
  )
}
