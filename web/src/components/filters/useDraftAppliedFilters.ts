import { useCallback, useState } from 'react'

export interface UseDraftAppliedFiltersOptions<T> {
  /** Stable empty advanced-filter object (module-level const recommended). */
  emptyFilters: T
  initialPage?: number
  initialSize?: number
}

/**
 * Shared draft vs applied filter state for admin list pages.
 *
 * - `draftFilters` = values being edited in the Filter panel (popover)
 * - `appliedFilters` = values driving the list query / client filter
 * - `search` / `searchInput` = independent toolbar search (still part of applied tags)
 * - Reset / Clear filters clears advanced filters only; Clear all / search-tag remove clears search too
 * - Any change to applied filters or search resets page to 0
 */
export function useDraftAppliedFilters<T extends object>({
  emptyFilters,
  initialPage = 0,
  initialSize = 20,
}: UseDraftAppliedFiltersOptions<T>) {
  const [draftFilters, setDraftFilters] = useState<T>(emptyFilters)
  const [appliedFilters, setAppliedFilters] = useState<T>(emptyFilters)
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(initialPage)
  const [size, setSizeState] = useState(initialSize)

  const applyFilters = useCallback(() => {
    setAppliedFilters(draftFilters)
    setPage(0)
  }, [draftFilters])

  /** Clears advanced filters only — search has its own clear path. */
  const resetFilters = useCallback(() => {
    setDraftFilters(emptyFilters)
    setAppliedFilters(emptyFilters)
    setPage(0)
  }, [emptyFilters])

  const applySearch = useCallback((value?: string) => {
    const next = (value ?? searchInput).trim()
    setSearchInput(next)
    setSearch(next)
    setPage(0)
  }, [searchInput])

  const clearSearch = useCallback(() => {
    setSearchInput('')
    setSearch('')
    setPage(0)
  }, [])

  const removeFilter = useCallback(<K extends keyof T>(key: K, emptyValue: T[K]) => {
    setAppliedFilters((prev) => ({ ...prev, [key]: emptyValue }))
    setDraftFilters((prev) => ({ ...prev, [key]: emptyValue }))
    setPage(0)
  }, [])

  /** Clears search + all advanced filters and resets page. */
  const clearAllFilters = useCallback(() => {
    setSearchInput('')
    setSearch('')
    setDraftFilters(emptyFilters)
    setAppliedFilters(emptyFilters)
    setPage(0)
  }, [emptyFilters])

  const setSize = useCallback((next: number) => {
    setSizeState(next)
    setPage(0)
  }, [])

  return {
    draftFilters,
    setDraftFilters,
    appliedFilters,
    search,
    searchInput,
    setSearchInput,
    applySearch,
    clearSearch,
    applyFilters,
    resetFilters,
    removeFilter,
    clearAllFilters,
    page,
    setPage,
    size,
    setSize,
  }
}
