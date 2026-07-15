import { cn } from '../../../lib/utils/cn'

interface ColumnResizeHandleProps {
  columnId: string
  label: string
  isActive: boolean
  onResizeStart: (columnId: string, clientX: number) => void
  onNudge: (columnId: string, delta: number) => void
}

/**
 * Subtle Excel-like resize grip on the right edge of a header cell.
 * Isolated from header click targets (sorting) via stopPropagation.
 */
export function ColumnResizeHandle({
  columnId,
  label,
  isActive,
  onResizeStart,
  onNudge,
}: ColumnResizeHandleProps) {
  return (
    <button
      type="button"
      tabIndex={0}
      aria-label={`Resize ${label} column`}
      aria-orientation="vertical"
      className={cn(
        'absolute inset-y-0 right-0 z-10 w-3 translate-x-1/2 cursor-col-resize touch-none border-0 bg-transparent p-0',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/40',
        'after:absolute after:inset-y-1.5 after:left-1/2 after:w-px after:-translate-x-1/2 after:rounded-full after:bg-border after:transition-colors',
        'hover:after:bg-foreground/35',
        isActive && 'after:bg-foreground/55 after:w-0.5',
      )}
      onPointerDown={(event) => {
        event.preventDefault()
        event.stopPropagation()
        onResizeStart(columnId, event.clientX)
      }}
      onClick={(event) => {
        // Prevent parent header click (e.g. future sort toggles).
        event.preventDefault()
        event.stopPropagation()
      }}
      onKeyDown={(event) => {
        if (event.key === 'ArrowLeft') {
          event.preventDefault()
          onNudge(columnId, -10)
        } else if (event.key === 'ArrowRight') {
          event.preventDefault()
          onNudge(columnId, 10)
        } else if (event.key === 'Home') {
          event.preventDefault()
          onNudge(columnId, -1000)
        }
      }}
    />
  )
}
