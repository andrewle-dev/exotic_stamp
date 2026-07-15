import { Search } from 'lucide-react'
import { Button } from '../ui/Button'
import { Input } from '../ui/FormField'

export interface SearchFilterCardProps {
  id: string
  value: string
  onChange: (value: string) => void
  onSubmit: () => void
  placeholder?: string
  label?: string
  /** When true, hides the visible label (placeholder + icon still convey purpose). */
  hideLabel?: boolean
}

/**
 * Standalone Search card — kept outside the Advanced filters panel.
 * Commits on Enter or the Search button. Search participates in applied filter tags.
 */
export function SearchFilterCard({
  id,
  value,
  onChange,
  onSubmit,
  placeholder = 'Search…',
  label = 'Search',
  hideLabel = false,
}: SearchFilterCardProps) {
  return (
    <div className="flex flex-col gap-2 rounded-2xl border border-border bg-card p-4 shadow-sm sm:flex-row sm:items-end">
      <div className="min-w-0 flex-1 space-y-1.5">
        {hideLabel ? (
          <span className="sr-only">{label}</span>
        ) : (
          <label htmlFor={id} className="text-xs font-medium text-muted-foreground">
            {label}
          </label>
        )}
        <div className="relative">
          <Search
            className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
            aria-hidden
          />
          <Input
            id={id}
            value={value}
            placeholder={placeholder}
            className="pl-9"
            onChange={(e) => onChange(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                onSubmit()
              }
            }}
          />
        </div>
      </div>
      <Button type="button" variant="secondary" size="md" onClick={onSubmit}>
        Search
      </Button>
    </div>
  )
}

/** @deprecated Prefer SearchFilterCard — same component, legacy name. */
export const ListSearchField = SearchFilterCard
