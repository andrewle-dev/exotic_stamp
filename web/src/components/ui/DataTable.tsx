import type { ReactNode } from 'react'
import { cn } from '../../lib/utils/cn'
import { EmptyState } from './EmptyState'
import { ErrorState } from './ErrorState'
import { LoadingSkeleton } from './LoadingSkeleton'

export type ColumnAlign = 'left' | 'center' | 'right'

export interface DataTableColumn<T> {
  /** Stable column id, also used as React key. */
  id: string
  header: ReactNode
  /** Cell renderer. Keep presentation here; no data fetching. */
  cell: (row: T) => ReactNode
  align?: ColumnAlign
  /** Tailwind width utility, e.g. 'w-40'. */
  width?: string
  headerClassName?: string
  cellClassName?: string
}

export interface DataTableProps<T> {
  columns: DataTableColumn<T>[]
  data: T[] | undefined
  /** Unique, stable row id used as React key. */
  getRowId: (row: T) => string
  isLoading?: boolean
  error?: unknown
  /** Highlight a row that needs attention (e.g. not-ready station). */
  rowWarning?: (row: T) => boolean
  /** Right-aligned per-row actions slot. Adds an actions column when provided. */
  rowActions?: (row: T) => ReactNode
  /** Custom empty state. Falls back to a generic EmptyState. */
  emptyState?: ReactNode
  emptyTitle?: string
  emptyDescription?: string
  onRetry?: () => void
  onRowClick?: (row: T) => void
  /** Number of skeleton rows while loading. */
  skeletonRows?: number
  /** Accessible caption for screen readers. */
  caption?: string
  className?: string
}

const alignClass: Record<ColumnAlign, string> = {
  left: 'text-left',
  center: 'text-center',
  right: 'text-right',
}

export function DataTable<T>({
  columns,
  data,
  getRowId,
  isLoading = false,
  error,
  rowWarning,
  rowActions,
  emptyState,
  emptyTitle = 'No records found',
  emptyDescription,
  onRetry,
  onRowClick,
  skeletonRows = 6,
  caption,
  className,
}: DataTableProps<T>) {
  const totalColumns = columns.length + (rowActions ? 1 : 0)

  const headerCells = (
    <tr className="border-b border-border">
      {columns.map((column) => (
        <th
          key={column.id}
          scope="col"
          className={cn(
            'px-4 py-2.5 text-[11px] font-semibold uppercase tracking-wide text-muted-foreground',
            alignClass[column.align ?? 'left'],
            column.width,
            column.headerClassName,
          )}
        >
          {column.header}
        </th>
      ))}
      {rowActions ? (
        <th scope="col" className="px-4 py-2.5 text-right text-[11px] font-semibold uppercase tracking-wide text-muted-foreground">
          <span className="sr-only">Actions</span>
        </th>
      ) : null}
    </tr>
  )

  function renderBody() {
    if (isLoading) {
      return Array.from({ length: skeletonRows }).map((_, rowIndex) => (
        <tr key={`skeleton-${rowIndex}`} className="border-b border-border">
          {columns.map((column) => (
            <td key={column.id} className={cn('px-4 py-3', alignClass[column.align ?? 'left'])}>
              <LoadingSkeleton className="h-4 w-full max-w-[160px]" />
            </td>
          ))}
          {rowActions ? (
            <td className="px-4 py-3 text-right">
              <LoadingSkeleton className="ml-auto h-4 w-12" />
            </td>
          ) : null}
        </tr>
      ))
    }

    if (error) {
      return (
        <tr>
          <td colSpan={totalColumns} className="px-4 py-6">
            <ErrorState
              error={error}
              action={
                onRetry ? (
                  <button
                    type="button"
                    onClick={onRetry}
                    className="rounded-md border border-border px-3 py-1.5 text-sm font-medium text-foreground hover:bg-secondary"
                  >
                    Retry
                  </button>
                ) : undefined
              }
            />
          </td>
        </tr>
      )
    }

    if (!data || data.length === 0) {
      return (
        <tr>
          <td colSpan={totalColumns} className="px-4 py-6">
            {emptyState ?? <EmptyState title={emptyTitle} description={emptyDescription} />}
          </td>
        </tr>
      )
    }

    return data.map((row) => {
      const warned = rowWarning?.(row) ?? false
      const clickable = Boolean(onRowClick)
      return (
        <tr
          key={getRowId(row)}
          onClick={clickable ? () => onRowClick?.(row) : undefined}
          className={cn(
            'border-b border-border last:border-0',
            warned ? 'bg-accent/40' : 'bg-card',
            clickable ? 'cursor-pointer hover:bg-secondary' : 'hover:bg-secondary/60',
          )}
        >
          {columns.map((column) => (
            <td
              key={column.id}
              className={cn(
                'px-4 py-3 text-sm text-foreground',
                alignClass[column.align ?? 'left'],
                column.cellClassName,
              )}
            >
              {column.cell(row)}
            </td>
          ))}
          {rowActions ? (
            <td
              className="px-4 py-3 text-right"
              onClick={(event) => event.stopPropagation()}
            >
              <div className="flex items-center justify-end gap-1">{rowActions(row)}</div>
            </td>
          ) : null}
        </tr>
      )
    })
  }

  return (
    <div className={cn('overflow-x-auto rounded-lg border border-border bg-card', className)}>
      <table className="w-full border-collapse text-left">
        {caption ? <caption className="sr-only">{caption}</caption> : null}
        <thead className="bg-secondary/50">{headerCells}</thead>
        <tbody>{renderBody()}</tbody>
      </table>
    </div>
  )
}
