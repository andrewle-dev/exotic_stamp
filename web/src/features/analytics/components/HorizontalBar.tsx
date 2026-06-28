import { cn } from '../../../lib/utils/cn'

interface HorizontalBarProps {
  label: string
  value: number
  max: number
  valueLabel?: string
  className?: string
  barClassName?: string
}

export function HorizontalBar({
  label,
  value,
  max,
  valueLabel,
  className,
  barClassName,
}: HorizontalBarProps) {
  const widthPercent = max > 0 ? Math.min(100, Math.round((value / max) * 100)) : 0

  return (
    <div className={cn('space-y-1', className)}>
      <div className="flex items-center justify-between gap-3 text-sm">
        <span className="truncate text-foreground">{label}</span>
        <span className="shrink-0 tabular-nums text-muted-foreground">
          {valueLabel ?? value.toLocaleString()}
        </span>
      </div>
      <div className="h-2 overflow-hidden rounded-full bg-secondary">
        <div
          className={cn('h-full rounded-full bg-primary transition-all', barClassName)}
          style={{ width: `${widthPercent}%` }}
          role="presentation"
        />
      </div>
    </div>
  )
}
