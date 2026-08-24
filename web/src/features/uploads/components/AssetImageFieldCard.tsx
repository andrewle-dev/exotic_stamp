import { useCallback, useRef, useState, type ChangeEvent, type DragEvent } from 'react'
import { Check, ImageIcon, Loader2, Upload, X } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { ImageWithFallback } from '../../../components/ui/ImageWithFallback'
import { InlineFieldError } from '../../../components/ui/InlineFieldError'
import { getErrorMessage } from '../../../lib/api/errors'
import { cn } from '../../../lib/utils/cn'
import type { AssetUploadPurpose } from '../../../types/uploads'
import { useUploadPublicAsset } from '../hooks'

export type AssetImageObjectFit = 'contain' | 'cover'
export type AssetImagePreviewSize = 'md' | 'lg'
export type AssetImagePreviewAspect = 'square' | 'wide'

export interface AssetImageFieldCardProps {
  id: string
  /** Primary title, e.g. "Main stamp artwork" */
  title: string
  value: string
  onChange: (url: string) => void
  error?: string
  required?: boolean
  /** Contextual helper under the title */
  hint?: string
  /** Alias used by form drawers; same display as hint. */
  help?: string
  /** Upload purpose sent to the public asset API. */
  purpose?: AssetUploadPurpose
  /** Entity ID used to build a purpose-specific storage key. */
  entityId?: string
  /** When true and a file was uploaded this session, show unsaved upload hint. */
  formDirty?: boolean
  /**
   * Show a raw URL text input. Defaults to false — URLs stay in form state only.
   * Prefer upload + preview for admin workflows.
   */
  showUrlInput?: boolean
  /** Square preview edge size (ignored when previewAspect is wide). */
  previewSize?: AssetImagePreviewSize
  /** Square brand mark vs landscape promo banner. Defaults to square. */
  previewAspect?: AssetImagePreviewAspect
  /** How the preview fits inside the frame. */
  objectFit?: AssetImageObjectFit
  /** Show recommended square size helper. Defaults to true for square aspect. */
  showSquareHint?: boolean
  /** Allow clearing the current image URL. */
  clearable?: boolean
}

const ACCEPTED_TYPES = 'image/png,image/jpeg,image/webp,image/gif,image/svg+xml'

/** Fixed square edge — same size for upload zone + preview so the row never misaligns. */
const SQUARE_EDGE: Record<AssetImagePreviewSize, string> = {
  md: 'h-[8.5rem] w-[8.5rem]',
  lg: 'h-[10rem] w-[10rem]',
}

const UPLOAD_EDGE: Record<AssetImagePreviewSize, string> = {
  md: 'min-h-[8.5rem] sm:h-[8.5rem]',
  lg: 'min-h-[10rem] sm:h-[10rem]',
}

/**
 * Stable asset editor for admin drawers.
 * Square: [ upload zone | preview ]
 * Wide: single 16:9 frame that is both drop target and live preview.
 * Binds uploaded URL into form state without exposing a raw URL field.
 */
export function AssetImageFieldCard({
  id,
  title,
  value,
  onChange,
  error,
  required,
  hint,
  help,
  purpose = 'GENERIC',
  entityId,
  formDirty = false,
  showUrlInput = false,
  previewSize = 'md',
  previewAspect = 'square',
  objectFit = 'cover',
  showSquareHint,
  clearable = false,
}: AssetImageFieldCardProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [dragActive, setDragActive] = useState(false)
  const [uploadedThisSession, setUploadedThisSession] = useState(false)
  const uploadMutation = useUploadPublicAsset()
  const isWide = previewAspect === 'wide'
  const resolveSquareHint = showSquareHint ?? !isWide
  const hasImage = Boolean(value)
  const helperText = help ?? hint

  const processFile = useCallback(
    async (file: File) => {
      const result = await uploadMutation.mutateAsync({ file, purpose, entityId })
      onChange(result.url)
      setUploadedThisSession(true)
    },
    [entityId, onChange, purpose, uploadMutation],
  )

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (file) {
      void processFile(file)
    }
    event.target.value = ''
  }

  function handleDrop(event: DragEvent<HTMLDivElement>) {
    event.preventDefault()
    setDragActive(false)
    const file = event.dataTransfer.files?.[0]
    if (file) {
      void processFile(file)
    }
  }

  function handleClear() {
    setUploadedThisSession(false)
    onChange('')
  }

  function openFilePicker() {
    fileInputRef.current?.click()
  }

  const showUnsavedHint = formDirty && uploadedThisSession && Boolean(value)
  const previewFrame = SQUARE_EDGE[previewSize]
  const uploadEdge = UPLOAD_EDGE[previewSize]
  const objectFitClass = objectFit === 'contain' ? 'object-contain' : 'object-cover'
  const isPending = uploadMutation.isPending

  const fileInput = (
    <input
      ref={fileInputRef}
      id={`${id}-file`}
      type="file"
      accept={ACCEPTED_TYPES}
      className="hidden"
      onChange={handleFileChange}
    />
  )

  return (
    <div className="space-y-3">
      <div className="space-y-1.5">
        <div className="flex items-start justify-between gap-3">
          <label
            htmlFor={showUrlInput ? id : `${id}-file`}
            className="flex items-center gap-1 text-sm font-medium text-foreground"
          >
            {title}
            {required ? <span className="text-destructive">*</span> : null}
          </label>
          {clearable && hasImage && !isWide ? (
            <Button
              type="button"
              variant="ghost"
              size="sm"
              className="h-auto shrink-0 px-2 py-1 text-xs text-muted-foreground"
              disabled={isPending}
              onClick={handleClear}
              aria-label={`Remove ${title}`}
            >
              <X className="h-3.5 w-3.5" aria-hidden="true" />
              Remove
            </Button>
          ) : null}
        </div>
        {helperText && !error ? <p className="text-xs leading-relaxed text-muted-foreground">{helperText}</p> : null}
        {resolveSquareHint && !error ? (
          <p className="text-[11px] leading-relaxed text-muted-foreground">
            Recommended square image size for the preview.
          </p>
        ) : null}
        <InlineFieldError id={`${id}-error`} message={error} />
      </div>

      {showUrlInput ? (
        <input
          id={id}
          type="url"
          value={value}
          placeholder="https://… or upload an image"
          className="w-full rounded-lg border border-border bg-input-background px-3 py-2.5 text-sm outline-none transition-colors focus:border-primary focus:ring-2 focus:ring-ring/20"
          onChange={(e) => {
            setUploadedThisSession(false)
            onChange(e.target.value)
          }}
        />
      ) : null}

      {isWide ? (
        <div className="space-y-2">
          <div
            role="button"
            tabIndex={0}
            aria-label={hasImage ? `Replace ${title}` : `Upload ${title}`}
            className={cn(
              'group relative w-full overflow-hidden rounded-xl border-2 border-dashed transition-colors outline-none focus-visible:ring-2 focus-visible:ring-ring/30',
              'aspect-video',
              dragActive
                ? 'border-primary bg-primary/5'
                : hasImage
                  ? 'border-border border-solid bg-secondary/30 hover:border-primary/50'
                  : 'border-border/80 bg-secondary/70 hover:border-primary/45 hover:bg-secondary',
              isPending && 'pointer-events-none opacity-70',
              error && 'border-destructive/50',
            )}
            onDragOver={(e) => {
              e.preventDefault()
              setDragActive(true)
            }}
            onDragLeave={() => setDragActive(false)}
            onDrop={handleDrop}
            onClick={() => {
              if (!isPending) openFilePicker()
            }}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault()
                if (!isPending) openFilePicker()
              }
            }}
          >
            {hasImage ? (
              <>
                <ImageWithFallback
                  src={value}
                  alt={`${title} preview`}
                  className={cn(
                    'h-full w-full rounded-none border-0 bg-secondary/40',
                    objectFitClass,
                  )}
                  fallbackClassName="h-full w-full rounded-none border-0"
                />
                <div
                  className={cn(
                    'absolute inset-0 flex items-end justify-between gap-2 bg-gradient-to-t from-black/55 via-black/15 to-transparent p-3',
                    'opacity-100 sm:opacity-0 sm:transition-opacity sm:group-hover:opacity-100 sm:group-focus-within:opacity-100',
                    dragActive && 'opacity-100',
                  )}
                >
                  <div className="flex min-w-0 items-center gap-2 text-white">
                    {isPending ? (
                      <Loader2 className="h-4 w-4 shrink-0 animate-spin" aria-hidden="true" />
                    ) : (
                      <Upload className="h-4 w-4 shrink-0" aria-hidden="true" />
                    )}
                    <span className="truncate text-xs font-medium">
                      {isPending ? 'Uploading…' : 'Drop to replace, or click to choose'}
                    </span>
                  </div>
                  {clearable ? (
                    <Button
                      type="button"
                      variant="secondary"
                      size="sm"
                      className="h-7 shrink-0 border-0 bg-white/95 px-2 text-xs text-foreground shadow-sm hover:bg-white"
                      disabled={isPending}
                      onClick={(e) => {
                        e.stopPropagation()
                        handleClear()
                      }}
                      aria-label={`Remove ${title}`}
                    >
                      <X className="h-3.5 w-3.5" aria-hidden="true" />
                      Remove
                    </Button>
                  ) : null}
                </div>
              </>
            ) : (
              <div className="flex h-full w-full flex-col items-center justify-center gap-2.5 px-4 text-center">
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary ring-1 ring-primary/15">
                  {isPending ? (
                    <Loader2 className="h-5 w-5 animate-spin" aria-hidden="true" />
                  ) : (
                    <Upload className="h-5 w-5" aria-hidden="true" />
                  )}
                </div>
                <div className="space-y-1">
                  <p className="text-sm font-medium text-foreground">
                    {isPending ? 'Uploading…' : 'Drop a landscape banner here'}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    PNG, JPG, WebP, GIF, or SVG · ≈16:9
                  </p>
                </div>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  className="mt-0.5"
                  disabled={isPending}
                  onClick={(e) => {
                    e.stopPropagation()
                    openFilePicker()
                  }}
                >
                  {isPending ? 'Uploading…' : 'Choose file'}
                </Button>
              </div>
            )}

            {!hasImage ? (
              <span className="pointer-events-none absolute right-3 top-3 rounded-md bg-background/85 px-2 py-0.5 text-[10px] font-medium tracking-wide text-muted-foreground ring-1 ring-border/80">
                16:9
              </span>
            ) : null}

            {fileInput}
          </div>
        </div>
      ) : (
        <div className="flex flex-col gap-3 sm:flex-row sm:items-start">
          <div
            className={cn(
              'flex w-full min-w-0 flex-1 flex-col items-center justify-center gap-1.5 overflow-hidden rounded-xl border border-dashed px-3 py-3 transition-colors',
              uploadEdge,
              dragActive
                ? 'border-primary bg-primary/5'
                : 'border-border bg-secondary/50 hover:border-primary/40 hover:bg-secondary',
              isPending && 'pointer-events-none opacity-70',
              error && 'border-destructive/40',
            )}
            onDragOver={(e) => {
              e.preventDefault()
              setDragActive(true)
            }}
            onDragLeave={() => setDragActive(false)}
            onDrop={handleDrop}
          >
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary">
              {isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />
              ) : (
                <Upload className="h-4 w-4" aria-hidden="true" />
              )}
            </div>
            <div className="min-w-0 space-y-0.5 text-center">
              <p className="text-sm font-medium leading-tight text-foreground">
                {isPending ? 'Uploading…' : 'Drag and drop an image'}
              </p>
              <p className="text-[11px] leading-tight text-muted-foreground">
                PNG, JPG, WebP, GIF, or SVG
              </p>
            </div>
            <Button
              type="button"
              variant="secondary"
              size="sm"
              className="shrink-0"
              disabled={isPending}
              onClick={openFilePicker}
            >
              {isPending ? 'Uploading…' : 'Choose file'}
            </Button>
            {fileInput}
          </div>

          <div
            className={cn(
              'relative mx-auto shrink-0 overflow-hidden rounded-xl border border-border bg-card sm:mx-0',
              previewFrame,
            )}
          >
            {hasImage ? (
              <ImageWithFallback
                src={value}
                alt={`${title} preview`}
                className={cn(
                  'h-full w-full rounded-none border-0 bg-secondary/40',
                  objectFitClass,
                )}
                fallbackClassName="h-full w-full rounded-none border-0"
              />
            ) : (
              <div className="flex h-full w-full flex-col items-center justify-center gap-1.5 bg-secondary/60 px-3 text-muted-foreground">
                <ImageIcon className="h-5 w-5 opacity-60" aria-hidden="true" />
                <span className="text-[11px] font-medium">No preview</span>
              </div>
            )}
          </div>
        </div>
      )}

      {uploadMutation.isError ? (
        <p className="text-xs text-destructive" role="alert">
          {getErrorMessage(uploadMutation.error)}
        </p>
      ) : null}

      {uploadMutation.isSuccess && uploadedThisSession && !isPending ? (
        <p className="flex items-center gap-1.5 text-xs text-emerald-700">
          <Check className="h-3.5 w-3.5" aria-hidden="true" />
          Upload complete — ready to save
        </p>
      ) : null}

      {showUnsavedHint ? (
        <p className="text-xs text-amber-700">
          Uploaded but not saved yet — submit the form to persist this asset.
        </p>
      ) : null}
    </div>
  )
}
