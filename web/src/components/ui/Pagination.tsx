import { ChevronLeft, ChevronRight } from 'lucide-react'
import { cn } from '../../lib/utils/cn'
import { formatNumber } from '../../lib/formatting/number'
import { toUiPage } from '../../lib/api/pagination'

interface PaginationProps {
  /** Backend 0-based page index. */
  page: number
  /** Page size. */
  size: number
  totalPages: number
  totalElements: number
  /** Receives the next backend 0-based page index. */
  onPageChange: (backendPage: number) => void
  /** Optional page size selector. */
  onSizeChange?: (size: number) => void
  pageSizeOptions?: number[]
  disabled?: boolean
  className?: string
}

export function Pagination({
  page,
  size,
  totalPages,
  totalElements,
  onPageChange,
  onSizeChange,
  pageSizeOptions = [10, 20, 50],
  disabled = false,
  className,
}: PaginationProps) {
  const safeTotalPages = Math.max(totalPages, 1)
  const uiPage = toUiPage(page)
  const isFirst = page <= 0
  const isLast = page >= safeTotalPages - 1

  const rangeStart = totalElements === 0 ? 0 : page * size + 1
  const rangeEnd = Math.min(totalElements, page * size + size)

  return (
    <div
      className={cn(
        'flex flex-col items-center justify-between gap-3 px-1 py-2 text-sm text-muted-foreground sm:flex-row',
        className,
      )}
    >
      <div className="flex items-center gap-3">
        <span>
          Showing <span className="font-medium text-foreground">{formatNumber(rangeStart)}</span>–
          <span className="font-medium text-foreground">{formatNumber(rangeEnd)}</span> of{' '}
          <span className="font-medium text-foreground">{formatNumber(totalElements)}</span>
        </span>

        {onSizeChange ? (
          <label className="flex items-center gap-1.5">
            <span className="sr-only">Rows per page</span>
            <select
              value={size}
              disabled={disabled}
              onChange={(event) => onSizeChange(Number(event.target.value))}
              className="rounded-md border border-border bg-card px-2 py-1 text-sm text-foreground disabled:cursor-not-allowed disabled:opacity-50"
            >
              {pageSizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option} / page
                </option>
              ))}
            </select>
          </label>
        ) : null}
      </div>

      <div className="flex items-center gap-2">
        <span aria-live="polite">
          Page <span className="font-medium text-foreground">{uiPage}</span> of{' '}
          <span className="font-medium text-foreground">{safeTotalPages}</span>
        </span>

        <div className="flex items-center gap-1">
          <button
            type="button"
            onClick={() => onPageChange(page - 1)}
            disabled={disabled || isFirst}
            className="inline-flex items-center gap-1 rounded-md border border-border bg-card px-2.5 py-1.5 text-sm font-medium text-foreground hover:bg-secondary disabled:cursor-not-allowed disabled:opacity-50"
            aria-label="Previous page"
          >
            <ChevronLeft className="h-4 w-4" />
            Prev
          </button>
          <button
            type="button"
            onClick={() => onPageChange(page + 1)}
            disabled={disabled || isLast}
            className="inline-flex items-center gap-1 rounded-md border border-border bg-card px-2.5 py-1.5 text-sm font-medium text-foreground hover:bg-secondary disabled:cursor-not-allowed disabled:opacity-50"
            aria-label="Next page"
          >
            Next
            <ChevronRight className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  )
}
