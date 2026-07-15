import { Button } from '../ui/Button'

interface FilterActionsRowProps {
  onReset: () => void
  onApply: () => void
  resetLabel?: string
  applyLabel?: string
  disableApply?: boolean
}

/**
 * Footer actions for the advanced filters panel.
 * Clear (secondary) and Apply (primary) sit side-by-side, right-aligned.
 */
export function FilterActionsRow({
  onReset,
  onApply,
  resetLabel = 'Clear filters',
  applyLabel = 'Apply filters',
  disableApply = false,
}: FilterActionsRowProps) {
  return (
    <div className="flex flex-row flex-wrap items-center justify-end gap-2 border-t border-border pt-4 sm:flex-nowrap sm:gap-3">
      <Button
        type="button"
        variant="secondary"
        size="md"
        onClick={onReset}
        className="h-9 shrink-0 whitespace-nowrap hover:border-destructive/20 hover:bg-destructive/10 hover:text-destructive"
      >
        {resetLabel}
      </Button>
      <Button
        type="button"
        variant="primary"
        size="md"
        onClick={onApply}
        disabled={disableApply}
        className="h-9 shrink-0 whitespace-nowrap"
      >
        {applyLabel}
      </Button>
    </div>
  )
}
