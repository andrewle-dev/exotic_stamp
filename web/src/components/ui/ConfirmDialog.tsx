import { useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { AlertTriangle, Info } from 'lucide-react'
import { cn } from '../../lib/utils/cn'
import { Button } from './Button'
import { Input } from './FormField'

export type ConfirmVariant = 'danger' | 'warning' | 'default'

interface ConfirmDialogProps {
  open: boolean
  title: string
  description?: React.ReactNode
  variant?: ConfirmVariant
  confirmLabel?: string
  cancelLabel?: string
  /** When set, the user must type this exact text to enable confirm (e.g. station code). */
  requireText?: string
  /** Label shown above the confirmation input. */
  requireTextLabel?: React.ReactNode
  loading?: boolean
  /** Externally disable confirm (e.g. permission). */
  disabled?: boolean
  onConfirm: () => void
  onCancel: () => void
}

const variantConfig: Record<
  ConfirmVariant,
  { icon: typeof AlertTriangle; iconWrap: string; confirmVariant: 'primary' | 'danger' }
> = {
  danger: {
    icon: AlertTriangle,
    iconWrap: 'bg-accent text-accent-foreground',
    confirmVariant: 'danger',
  },
  warning: {
    icon: AlertTriangle,
    iconWrap: 'bg-amber-50 text-amber-600',
    confirmVariant: 'primary',
  },
  default: {
    icon: Info,
    iconWrap: 'bg-secondary text-primary',
    confirmVariant: 'primary',
  },
}

export function ConfirmDialog({
  open,
  title,
  description,
  variant = 'default',
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  requireText,
  requireTextLabel,
  loading = false,
  disabled = false,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  const [typed, setTyped] = useState('')
  const [prevOpen, setPrevOpen] = useState(open)

  if (open !== prevOpen) {
    setPrevOpen(open)
    if (!open) {
      setTyped('')
    }
  }

  useEffect(() => {
    if (!open) {
      return
    }
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !loading) {
        onCancel()
      }
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [open, loading, onCancel])

  if (!open) {
    return null
  }

  const config = variantConfig[variant]
  const Icon = config.icon
  const textSatisfied = requireText ? typed.trim() === requireText.trim() : true
  const confirmDisabled = disabled || loading || !textSatisfied

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div
        className="absolute inset-0 bg-black/40"
        onClick={() => {
          if (!loading) {
            onCancel()
          }
        }}
        aria-hidden="true"
      />

      <div
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirm-dialog-title"
        className="relative z-10 w-full max-w-md rounded-lg border border-border bg-card shadow-lg"
      >
        <div className="flex gap-3 p-6">
          <div className={cn('flex h-10 w-10 shrink-0 items-center justify-center rounded-full', config.iconWrap)}>
            <Icon className="h-5 w-5" />
          </div>
          <div className="min-w-0 flex-1 space-y-2">
            <h2 id="confirm-dialog-title" className="text-base font-semibold text-foreground">
              {title}
            </h2>
            {description ? (
              <div className="text-sm text-muted-foreground">{description}</div>
            ) : null}

            {requireText ? (
              <div className="space-y-1.5 pt-1">
                <label htmlFor="confirm-require-text" className="text-sm font-medium text-foreground">
                  {requireTextLabel ?? (
                    <>
                      Type <span className="font-mono text-foreground">{requireText}</span> to confirm
                    </>
                  )}
                </label>
                <Input
                  id="confirm-require-text"
                  value={typed}
                  autoComplete="off"
                  disabled={loading}
                  onChange={(event) => setTyped(event.target.value)}
                />
              </div>
            ) : null}
          </div>
        </div>

        <div className="flex justify-end gap-2 border-t border-border px-6 py-4">
          <Button variant="secondary" size="md" onClick={onCancel} disabled={loading}>
            {cancelLabel}
          </Button>
          <Button
            variant={config.confirmVariant}
            size="md"
            onClick={onConfirm}
            disabled={confirmDisabled}
          >
            {loading ? 'Working…' : confirmLabel}
          </Button>
        </div>
      </div>
    </div>,
    document.body,
  )
}
