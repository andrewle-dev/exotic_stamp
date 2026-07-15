import { useId, useState, type ReactNode } from 'react'
import { Search } from 'lucide-react'
import { Button } from '../ui/Button'
import { Input } from '../ui/FormField'
import { FilterPanel, FilterPanelTrigger } from './FilterPanel'

export interface ListFilterToolbarProps {
  searchId: string
  searchValue: string
  onSearchChange: (value: string) => void
  onSearchSubmit: () => void
  searchPlaceholder?: string
  searchLabel?: string
  /** Applied advanced filters count (excludes search) — badge on Filter button. */
  activeAdvancedFilterCount?: number
  filterTitle?: string
  filterSubtitle?: string
  onApplyFilters: () => void
  onClearFilters: () => void
  /** Advanced filter field groups (FilterGroup + FilterSelect, etc.). */
  children: ReactNode
  className?: string
}

/**
 * Compact list-page filter toolbar:
 * left Filter button (opens anchored panel) · right Search + Search button.
 */
export function ListFilterToolbar({
  searchId,
  searchValue,
  onSearchChange,
  onSearchSubmit,
  searchPlaceholder = 'Search…',
  searchLabel = 'Search',
  activeAdvancedFilterCount = 0,
  filterTitle = 'Filters',
  filterSubtitle,
  onApplyFilters,
  onClearFilters,
  children,
  className,
}: ListFilterToolbarProps) {
  const triggerId = useId()
  const [panelOpen, setPanelOpen] = useState(false)

  return (
    <div className={className}>
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between sm:gap-4">
        <div className="relative shrink-0">
          <FilterPanelTrigger
            id={triggerId}
            open={panelOpen}
            activeCount={activeAdvancedFilterCount}
            onClick={() => setPanelOpen((prev) => !prev)}
          />
          <FilterPanel
            open={panelOpen}
            triggerId={triggerId}
            title={filterTitle}
            subtitle={filterSubtitle}
            onClose={() => setPanelOpen(false)}
            onApply={onApplyFilters}
            onClear={onClearFilters}
          >
            {children}
          </FilterPanel>
        </div>

        <div className="flex min-w-0 flex-1 items-center gap-2 sm:max-w-xl sm:justify-end lg:max-w-2xl">
          <label htmlFor={searchId} className="sr-only">
            {searchLabel}
          </label>
          <div className="relative min-w-0 flex-1">
            <Search
              className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
              aria-hidden
            />
            <Input
              id={searchId}
              value={searchValue}
              placeholder={searchPlaceholder}
              className="h-10 bg-card pl-9"
              onChange={(e) => onSearchChange(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  onSearchSubmit()
                }
              }}
            />
          </div>
          <Button
            type="button"
            variant="secondary"
            size="md"
            className="h-10 shrink-0"
            onClick={onSearchSubmit}
          >
            Search
          </Button>
        </div>
      </div>
    </div>
  )
}
