import { useEffect, useState, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { X } from 'lucide-react'
import { cn } from '../../lib/utils/cn'
import { Button } from './Button'
import { ApiErrorAlert } from '../feedback/ApiErrorAlert'
import { ConfirmDialog } from './ConfirmDialog'

type DrawerWidth = 'sm' | 'md' | 'lg'

interface FormDrawerProps {
  open: boolean
  title: string
  description?: string
  children: ReactNode
  onClose: () => void
  /** Associate the footer submit button with a form rendered inside (RHF). */
  formId?: string
  /** Used when no formId is provided; called by the Save button. */
  onSubmit?: () => void
  isSubmitting?: boolean
  /** When true, closing prompts an unsaved-changes confirmation. */
  isDirty?: boolean
  saveLabel?: string
  cancelLabel?: string
  /** Disable the save button (e.g. invalid form or permission). */
  saveDisabled?: boolean
  /** Error summary rendered above the footer. */
  error?: unknown
  /** Replace the default footer entirely. */
  footer?: ReactNode
  width?: DrawerWidth
}

const widthClass: Record<DrawerWidth, string> = {
  sm: 'max-w-sm',
  md: 'max-w-md',
  lg: 'max-w-xl',
}

export function FormDrawer({
  open,
  title,
  description,
  children,
  onClose,
  formId,
  onSubmit,
  isSubmitting = false,
  isDirty = false,
  saveLabel = 'Save',
  cancelLabel = 'Cancel',
  saveDisabled = false,
  error,
  footer,
  width = 'md',
}: FormDrawerProps) {
  const [confirmClose, setConfirmClose] = useState(false)
  const [prevOpen, setPrevOpen] = useState(open)

  if (open !== prevOpen) {
    setPrevOpen(open)
    if (!open) {
      setConfirmClose(false)
    }
  }

  function requestClose() {
    if (isSubmitting) {
      return
    }
    if (isDirty) {
      setConfirmClose(true)
      return
    }
    onClose()
  }

  useEffect(() => {
    if (!open) {
      return
    }
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        requestClose()
      }
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, isDirty, isSubmitting])

  if (!open) {
    return null
  }

  return createPortal(
    <div className="fixed inset-0 z-40 flex justify-end">
      <div className="absolute inset-0 bg-black/40" onClick={requestClose} aria-hidden="true" />

      <aside
        role="dialog"
        aria-modal="true"
        aria-labelledby="form-drawer-title"
        className={cn(
          'relative z-10 flex h-full w-full flex-col border-l border-border bg-card shadow-2xl shadow-black/10',
          widthClass[width],
        )}
      >
        <header className="flex shrink-0 items-start justify-between gap-4 border-b border-border px-6 py-5">
          <div className="min-w-0">
            <h2 id="form-drawer-title" className="text-lg font-semibold tracking-tight text-foreground">
              {title}
            </h2>
            {description ? (
              <p className="mt-1 text-sm leading-relaxed text-muted-foreground">{description}</p>
            ) : null}
          </div>
          <button
            type="button"
            onClick={requestClose}
            disabled={isSubmitting}
            className="rounded-lg p-1.5 text-muted-foreground transition-colors hover:bg-secondary hover:text-foreground disabled:opacity-50"
            aria-label="Close"
          >
            <X className="h-4 w-4" />
          </button>
        </header>

        <div className="flex-1 overflow-y-auto bg-secondary/30 px-6 py-5">{children}</div>

        {error ? (
          <div className="shrink-0 border-t border-border bg-card px-6 py-3">
            <ApiErrorAlert error={error} />
          </div>
        ) : null}

        <footer className="flex shrink-0 items-center justify-end gap-2 border-t border-border bg-card px-6 py-4">
          {footer ?? (
            <>
              <Button variant="secondary" size="md" onClick={requestClose} disabled={isSubmitting}>
                {cancelLabel}
              </Button>
              {formId ? (
                <Button type="submit" form={formId} size="md" disabled={isSubmitting || saveDisabled}>
                  {isSubmitting ? 'Saving…' : saveLabel}
                </Button>
              ) : (
                <Button size="md" onClick={onSubmit} disabled={isSubmitting || saveDisabled}>
                  {isSubmitting ? 'Saving…' : saveLabel}
                </Button>
              )}
            </>
          )}
        </footer>
      </aside>

      <ConfirmDialog
        open={confirmClose}
        variant="warning"
        title="Discard unsaved changes?"
        description="You have unsaved changes. Closing now will discard them."
        confirmLabel="Discard"
        cancelLabel="Keep editing"
        onConfirm={() => {
          setConfirmClose(false)
          onClose()
        }}
        onCancel={() => setConfirmClose(false)}
      />
    </div>,
    document.body,
  )
}
