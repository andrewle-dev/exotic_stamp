import { useCallback, useRef, useState } from 'react'

export function useCopyToClipboard(resetAfterMs = 1500): {
  copied: boolean
  copy: (value: string) => Promise<boolean>
} {
  const [copied, setCopied] = useState(false)
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const copy = useCallback(
    async (value: string) => {
      try {
        await navigator.clipboard.writeText(value)
        setCopied(true)
        if (timeoutRef.current) {
          clearTimeout(timeoutRef.current)
        }
        timeoutRef.current = setTimeout(() => setCopied(false), resetAfterMs)
        return true
      } catch {
        setCopied(false)
        return false
      }
    },
    [resetAfterMs],
  )

  return { copied, copy }
}
