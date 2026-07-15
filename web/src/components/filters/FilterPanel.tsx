import { useEffect, useId, useRef, type ReactNode } from 'react'
import { ListFilter } from 'lucide-react'
import { cn } from '../../lib/utils/cn'
import { FilterActionsRow } from './FilterActionsRow'

export interface FilterPanelProps {
  open: boolean
  onClose: () => void
  onApply: () => void
  onClear: () => void
  children: ReactNode
  title?: string
  subtitle?: string
  className?: string
  /** Optional id of the trigger button for aria-controls. */
  triggerId?: string
}

/**
 * Anchored dropdown panel for advanced filter fields.
 * Not shown in the page body until opened by the Filter button.
 */
export function FilterPanel({
  open,
  onClose,
  onApply,
  onClear,
  children,
  title = 'Filters',
  subtitle,
  className,
  triggerId,
}: FilterPanelProps) {
  const titleId = useId()
  const panelRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose()
      }
    }

    const onPointerDown = (event: MouseEvent) => {
      const target = event.target as Node
      if (panelRef.current?.contains(target)) return
      if (triggerId && document.getElementById(triggerId)?.contains(target)) return
      onClose()
    }

    document.addEventListener('keydown', onKeyDown)
    document.addEventListener('mousedown', onPointerDown)
    return () => {
      document.removeEventListener('keydown', onKeyDown)
      document.removeEventListener('mousedown', onPointerDown)
    }
  }, [open, onClose, triggerId])

  if (!open) {
    return null
  }

  return (
    <div
      ref={panelRef}
      role="dialog"
      aria-modal="false"
      aria-labelledby={titleId}
      className={cn(
        'absolute left-0 top-full z-40 mt-2 w-[min(100vw-2rem,22rem)] rounded-xl border border-border bg-card p-4 shadow-lg sm:w-[32rem] lg:w-[36rem]',
        className,
      )}
    >
      <div className="mb-3">
        <h3 id={titleId} className="text-sm font-semibold text-foreground">
          {title}
        </h3>
        {subtitle ? (
          <p className="mt-0.5 text-xs leading-relaxed text-muted-foreground">{subtitle}</p>
        ) : null}
      </div>

      <div className="space-y-4">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">{children}</div>
        <FilterActionsRow
          onReset={onClear}
          onApply={() => {
            onApply()
            onClose()
          }}
          resetLabel="Clear filters"
          applyLabel="Apply filters"
        />
      </div>
    </div>
  )
}

/** Compact Filter trigger button used by ListFilterToolbar. */
export function FilterPanelTrigger({
  id,
  open,
  onClick,
  activeCount = 0,
}: {
  id: string
  open: boolean
  onClick: () => void
  activeCount?: number
}) {
  return (
    <button
      id={id}
      type="button"
      onClick={onClick}
      aria-expanded={open}
      aria-haspopup="dialog"
      className={cn(
        'relative inline-flex h-10 shrink-0 items-center gap-2 rounded-lg border px-3.5 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/40',
        open || activeCount > 0
          ? 'border-primary/30 bg-primary/10 text-primary'
          : 'border-border bg-card text-foreground hover:bg-secondary',
      )}
    >
      <ListFilter className="h-4 w-4" aria-hidden />
      Filter
      {activeCount > 0 ? (
        <span className="inline-flex min-w-5 items-center justify-center rounded-full bg-primary px-1.5 py-0.5 text-[10px] font-semibold leading-none text-primary-foreground">
          {activeCount}
        </span>
      ) : null}
    </button>
  )
}
