import { MapPin } from 'lucide-react'
import type { StationResponse } from '../../../types/stations'
import { cn } from '../../../lib/utils/cn'

interface StationTableCellProps {
  station: StationResponse
}

export function StationTableCell({ station }: StationTableCellProps) {
  return (
    <div className="flex items-center gap-3">
      {station.imageUrl ? (
        <img
          src={station.imageUrl}
          alt=""
          className="h-10 w-10 shrink-0 rounded-md border border-border object-cover"
        />
      ) : (
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-md border border-border bg-secondary text-muted-foreground">
          <MapPin className="h-4 w-4" />
        </div>
      )}
      <div className="min-w-0">
        <p className="truncate font-medium text-foreground">{station.name}</p>
        <p className="font-mono text-xs text-muted-foreground">{station.code}</p>
      </div>
    </div>
  )
}

interface LineBadgeProps {
  lineCode?: string
  lineName?: string
  className?: string
}

export function LineBadge({ lineCode, lineName, className }: LineBadgeProps) {
  const label = lineCode ?? lineName ?? '—'
  return (
    <span
      className={cn(
        'inline-flex rounded border border-emerald-200 bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700',
        className,
      )}
    >
      {label}
    </span>
  )
}
