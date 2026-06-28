import { useState } from 'react'
import { Check, Copy, Eye, EyeOff } from 'lucide-react'
import { cn } from '../../lib/utils/cn'
import { maskSecret } from '../../lib/formatting/masking'
import { useCopyToClipboard } from '../../lib/utils/useCopyToClipboard'

interface SecretFieldProps {
  value: string | null | undefined
  /** Allow toggling reveal. Set false in tables to keep values permanently masked. */
  revealable?: boolean
  /** Show a copy-to-clipboard button. */
  copyable?: boolean
  /** Permission-aware: disables reveal and copy when false. Defaults to true. */
  allowed?: boolean
  /** Custom masking function. Defaults to maskSecret. */
  maskFn?: (value: string | null | undefined) => string
  /** Render as a bordered field (detail view) vs inline (table cell). */
  variant?: 'field' | 'inline'
  className?: string
  /** Accessible label describing the secret, e.g. "QR code value". */
  ariaLabel?: string
}

/**
 * Displays a sensitive value masked by default.
 * In table usage, pass revealable={false} so the raw value is never exposed.
 */
export function SecretField({
  value,
  revealable = true,
  copyable = true,
  allowed = true,
  maskFn = (v) => maskSecret(v),
  variant = 'field',
  className,
  ariaLabel = 'sensitive value',
}: SecretFieldProps) {
  const [revealed, setRevealed] = useState(false)
  const { copied, copy } = useCopyToClipboard()

  const hasValue = value !== null && value !== undefined && value !== ''
  const canReveal = revealable && allowed && hasValue
  const isRevealed = canReveal && revealed
  const display = hasValue ? (isRevealed ? value : maskFn(value)) : '—'

  return (
    <div
      className={cn(
        'flex items-center gap-2',
        variant === 'field' &&
          'rounded-md border border-border bg-input-background px-3 py-2',
        className,
      )}
    >
      <span
        className={cn('min-w-0 flex-1 truncate font-mono text-sm text-foreground')}
        aria-label={isRevealed ? `${ariaLabel} revealed` : `${ariaLabel} hidden`}
      >
        {display}
      </span>

      {canReveal ? (
        <button
          type="button"
          onClick={() => setRevealed((prev) => !prev)}
          className="rounded p-1 text-muted-foreground hover:bg-secondary hover:text-foreground"
          aria-label={isRevealed ? `Hide ${ariaLabel}` : `Reveal ${ariaLabel}`}
        >
          {isRevealed ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
        </button>
      ) : null}

      {copyable ? (
        <button
          type="button"
          disabled={!allowed || !hasValue}
          onClick={() => {
            if (hasValue) {
              void copy(value)
            }
          }}
          className="rounded p-1 text-muted-foreground hover:bg-secondary hover:text-foreground disabled:cursor-not-allowed disabled:opacity-50"
          aria-label={`Copy ${ariaLabel}`}
        >
          {copied ? <Check className="h-4 w-4 text-emerald-600" /> : <Copy className="h-4 w-4" />}
        </button>
      ) : null}
    </div>
  )
}

/** Lightweight inline masked text, no controls. Safe default for dense tables. */
export function MaskedValue({
  value,
  maskFn = (v) => maskSecret(v),
  className,
}: {
  value: string | null | undefined
  maskFn?: (value: string | null | undefined) => string
  className?: string
}) {
  return <span className={cn('font-mono text-sm text-foreground', className)}>{maskFn(value)}</span>
}
