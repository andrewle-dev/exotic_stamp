import { useState } from 'react'
import { ImageOff } from 'lucide-react'
import { cn } from '../../lib/utils/cn'

interface ImageWithFallbackProps {
  src?: string | null
  alt: string
  className?: string
  fallbackClassName?: string
}

export function ImageWithFallback({
  src,
  alt,
  className,
  fallbackClassName,
}: ImageWithFallbackProps) {
  const [brokenSrc, setBrokenSrc] = useState<string | null>(null)
  const broken = Boolean(src) && brokenSrc === src

  if (!src || broken) {
    return (
      <div
        className={cn(
          'flex items-center justify-center rounded-md border border-dashed border-border bg-secondary text-muted-foreground',
          fallbackClassName ?? className,
        )}
        aria-label={alt}
      >
        <ImageOff className="h-5 w-5" aria-hidden="true" />
      </div>
    )
  }

  return (
    <img
      src={src}
      alt={alt}
      className={cn('rounded-md border border-border object-cover', className)}
      onError={() => setBrokenSrc(src)}
    />
  )
}
