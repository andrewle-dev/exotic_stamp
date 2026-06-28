import { useCallback, useRef, useState, type ChangeEvent, type DragEvent } from 'react'
import { Check, Copy, Loader2, Upload } from 'lucide-react'
import { FormField, Input } from '../../../components/ui/FormField'
import { Button } from '../../../components/ui/Button'
import { ImageWithFallback } from '../../../components/ui/ImageWithFallback'
import { useCopyToClipboard } from '../../../lib/utils/useCopyToClipboard'
import { getErrorMessage } from '../../../lib/api/errors'
import { cn } from '../../../lib/utils/cn'
import { useUploadPublicAsset } from '../hooks'

interface PublicAssetUploadFieldProps {
  id: string
  label: string
  value: string
  onChange: (url: string) => void
  error?: string
  required?: boolean
  /** When true and a file was uploaded this session, show unsaved upload hint. */
  formDirty?: boolean
}

const ACCEPTED_TYPES = 'image/png,image/jpeg,image/webp,image/gif,image/svg+xml'

export function PublicAssetUploadField({
  id,
  label,
  value,
  onChange,
  error,
  required,
  formDirty = false,
}: PublicAssetUploadFieldProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [dragActive, setDragActive] = useState(false)
  const [uploadedThisSession, setUploadedThisSession] = useState(false)
  const { copied, copy } = useCopyToClipboard()
  const uploadMutation = useUploadPublicAsset()

  const processFile = useCallback(
    async (file: File) => {
      const result = await uploadMutation.mutateAsync(file)
      onChange(result.url)
      setUploadedThisSession(true)
    },
    [onChange, uploadMutation],
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

  const showUnsavedHint = formDirty && uploadedThisSession && Boolean(value)

  return (
    <div className="space-y-3">
      <FormField label={label} htmlFor={id} required={required} error={error}>
        <Input
          id={id}
          type="url"
          value={value}
          placeholder="https://… or upload an image"
          onChange={(e) => {
            setUploadedThisSession(false)
            onChange(e.target.value)
          }}
        />
      </FormField>

      <div
        className={cn(
          'flex flex-col items-center justify-center gap-2 rounded-lg border-2 border-dashed px-4 py-6 transition-colors',
          dragActive ? 'border-primary bg-secondary' : 'border-border bg-card',
          uploadMutation.isPending && 'opacity-70',
        )}
        onDragOver={(e) => {
          e.preventDefault()
          setDragActive(true)
        }}
        onDragLeave={() => setDragActive(false)}
        onDrop={handleDrop}
      >
        <Upload className="h-6 w-6 text-muted-foreground" aria-hidden="true" />
        <p className="text-center text-xs text-muted-foreground">
          Drag and drop an image, or choose a file
        </p>
        <Button
          type="button"
          variant="secondary"
          size="sm"
          disabled={uploadMutation.isPending}
          onClick={() => fileInputRef.current?.click()}
        >
          {uploadMutation.isPending ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Uploading…
            </>
          ) : (
            'Upload file'
          )}
        </Button>
        <input
          ref={fileInputRef}
          type="file"
          accept={ACCEPTED_TYPES}
          className="hidden"
          onChange={handleFileChange}
        />
      </div>

      {uploadMutation.isError ? (
        <p className="text-xs text-destructive" role="alert">
          {getErrorMessage(uploadMutation.error)}
        </p>
      ) : null}

      {uploadMutation.isSuccess && uploadedThisSession ? (
        <p className="flex items-center gap-1.5 text-xs text-emerald-700">
          <Check className="h-3.5 w-3.5" aria-hidden="true" />
          Upload complete — URL applied to field
        </p>
      ) : null}

      {showUnsavedHint ? (
        <p className="text-xs text-amber-700">
          Uploaded but not saved yet — submit the form to persist this URL.
        </p>
      ) : null}

      {value ? (
        <div className="space-y-2">
          <div className="flex items-center justify-between gap-2">
            <span className="text-xs font-medium text-muted-foreground">Preview</span>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={() => void copy(value)}
              aria-label="Copy image URL"
            >
              {copied ? (
                <>
                  <Check className="h-3.5 w-3.5" />
                  Copied
                </>
              ) : (
                <>
                  <Copy className="h-3.5 w-3.5" />
                  Copy URL
                </>
              )}
            </Button>
          </div>
          <ImageWithFallback
            src={value}
            alt={`${label} preview`}
            className="h-32 w-full max-w-xs"
            fallbackClassName="h-32 w-full max-w-xs"
          />
        </div>
      ) : null}
    </div>
  )
}
