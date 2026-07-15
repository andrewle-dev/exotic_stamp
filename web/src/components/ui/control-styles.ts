import { cn } from '../../lib/utils/cn'

/** Shared visual language for text inputs, selects, and textareas in admin forms. */
export const controlClassName =
  'w-full rounded-lg border border-border bg-input-background px-3 py-2.5 text-sm text-foreground outline-none transition-colors placeholder:text-muted-foreground focus:border-primary focus:ring-2 focus:ring-ring/20 disabled:cursor-not-allowed disabled:opacity-60'

export function controlClass(className?: string) {
  return cn(controlClassName, className)
}
