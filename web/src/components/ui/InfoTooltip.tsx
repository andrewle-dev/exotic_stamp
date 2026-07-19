import { Info } from 'lucide-react'
import { cn } from '../../lib/utils/cn'

export interface InfoTooltipProps {
  /** Concise guidance shown on hover/focus. */
  content: string
  /** Accessible name for the help control. */
  label?: string
  className?: string
  /** Preferred placement relative to the icon. */
  side?: 'top' | 'bottom'
  /** Horizontal alignment of the tooltip panel relative to the icon. */
  align?: 'start' | 'center' | 'end'
}

/**
 * Compact info icon that reveals guidance on hover or keyboard focus.
 * Prefer this over always-visible helper paragraphs for detailed rules.
 */
export function InfoTooltip({
  content,
  label = 'More information',
  className,
  side = 'top',
  align = 'center',
}: InfoTooltipProps) {
  return (
    <span className={cn('relative inline-flex shrink-0', className)}>
      <button
        type="button"
        className={cn(
          'peer inline-flex h-4 w-4 items-center justify-center rounded-full text-muted-foreground',
          'transition-colors hover:text-primary',
          'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/30',
        )}
        aria-label={label}
      >
        <Info className="h-3.5 w-3.5" aria-hidden="true" />
      </button>
      <span
        role="tooltip"
        className={cn(
          'pointer-events-none absolute z-50 w-max max-w-[16rem]',
          'rounded-md border border-border bg-card px-2.5 py-1.5 text-left text-xs leading-relaxed text-foreground shadow-md',
          'opacity-0 transition-opacity duration-150',
          'peer-hover:opacity-100 peer-focus-visible:opacity-100',
          side === 'top' ? 'bottom-full mb-2' : 'top-full mt-2',
          align === 'end' && 'right-0',
          align === 'start' && 'left-0',
          align === 'center' && 'left-1/2 -translate-x-1/2',
        )}
      >
        {content}
      </span>
    </span>
  )
}

/** Alias for field-label help affordances. */
export function FieldHelpTooltip(props: InfoTooltipProps) {
  return <InfoTooltip {...props} />
}

/** Alias matching common naming in admin forms. */
export function HelpIconTooltip(props: InfoTooltipProps) {
  return <InfoTooltip {...props} />
}
