import { useId, useState, type ReactNode } from 'react'
import { ChevronDown, ListFilter } from 'lucide-react'
import { cn } from '../../lib/utils/cn'
import { FilterActionsRow } from './FilterActionsRow'

interface AdvancedFiltersPanelProps {
  subtitle: string
  children: ReactNode
  onApply: () => void
  onReset: () => void
  title?: string
  collapsible?: boolean
  defaultExpanded?: boolean
  className?: string
}

/**
 * @deprecated Prefer `ListFilterToolbar` + `FilterPanel`.
 * Always-open card layout — no longer used by dashboard list pages.
 */
export function AdvancedFiltersPanel({
  subtitle,
  children,
  onApply,
  onReset,
  title = 'Advanced filters',
  collapsible = false,
  defaultExpanded = true,
  className,
}: AdvancedFiltersPanelProps) {
  const titleId = useId()
  const [expanded, setExpanded] = useState(defaultExpanded)

  return (
    <section
      className={cn(
        'rounded-2xl border border-border bg-card p-5 shadow-sm',
        className,
      )}
      aria-labelledby={titleId}
    >
      <div className="mb-4 flex items-start justify-between gap-3">
        <div className="flex min-w-0 items-start gap-3">
          <span className="mt-0.5 inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
            <ListFilter className="h-4 w-4" aria-hidden />
          </span>
          <div className="min-w-0">
            <h3 id={titleId} className="text-sm font-semibold text-foreground">
              {title}
            </h3>
            <p className="mt-0.5 text-xs leading-relaxed text-muted-foreground">{subtitle}</p>
          </div>
        </div>
        {collapsible ? (
          <button
            type="button"
            onClick={() => setExpanded((prev) => !prev)}
            className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-lg text-muted-foreground transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/40"
            aria-expanded={expanded}
            aria-label={expanded ? 'Collapse filters' : 'Expand filters'}
          >
            <ChevronDown
              className={cn('h-4 w-4 transition-transform', expanded ? 'rotate-180' : '')}
            />
          </button>
        ) : null}
      </div>

      {expanded ? (
        <div className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">{children}</div>
          <FilterActionsRow
            onReset={onReset}
            onApply={onApply}
            resetLabel="Clear filters"
            applyLabel="Apply filters"
          />

        </div>
      ) : null}
    </section>
  )
}
