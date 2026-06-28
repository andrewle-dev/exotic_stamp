import { AlertTriangle } from 'lucide-react'
import type { OperationalWarning } from '../../../types/analytics'
import { EmptyState } from '../../../components/ui/EmptyState'
import { cn } from '../../../lib/utils/cn'

interface OperationalWarningsListProps {
  warnings: OperationalWarning[]
  voucherSampleNote?: string
}

export function OperationalWarningsList({
  warnings,
  voucherSampleNote,
}: OperationalWarningsListProps) {
  if (warnings.length === 0) {
    return (
      <EmptyState
        title="No operational warnings"
        description="All checked resources look healthy based on available data."
      />
    )
  }

  return (
    <div className="space-y-3">
      {voucherSampleNote ? (
        <p className="text-xs text-muted-foreground">{voucherSampleNote}</p>
      ) : null}
      <ul className="space-y-2">
        {warnings.map((warning, index) => (
          <li
            key={`${warning.category}-${index}-${warning.message}`}
            className={cn(
              'flex items-start gap-2 rounded-md border px-3 py-2 text-sm',
              warning.severity === 'danger'
                ? 'border-red-200 bg-accent/40 text-accent-foreground'
                : 'border-amber-200 bg-amber-50 text-amber-900',
            )}
          >
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
            <div>
              <p className="text-[11px] font-semibold uppercase tracking-wide opacity-80">
                {warning.category}
              </p>
              <p>{warning.message}</p>
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}
