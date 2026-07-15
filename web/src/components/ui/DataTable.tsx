import { useMemo, type ReactNode } from 'react'
import { cn } from '../../lib/utils/cn'
import { EmptyState } from './EmptyState'
import { ErrorState } from './ErrorState'
import { LoadingSkeleton } from './LoadingSkeleton'
import { ColumnResizeHandle } from './table/ColumnResizeHandle'
import { DEFAULT_ACTIONS_WIDTH } from './table/columnWidthPresets'
import {
  ACTIONS_COLUMN_ID,
  useResizableColumns,
  type ResizableColumnConfig,
} from './table/useResizableColumns'

const stickyActionsHeaderClass =
  'sticky right-0 z-[1] bg-secondary/95 shadow-[-6px_0_8px_-6px_rgba(15,23,42,0.12)]'
const stickyActionsCellClass = 'sticky right-0 z-[1] shadow-[-6px_0_8px_-6px_rgba(15,23,42,0.12)]'

export type ColumnAlign = 'left' | 'center' | 'right'

export interface DataTableColumn<T> {
  /** Stable column id, also used as React key and width persistence key. */
  id: string
  header: ReactNode
  /** Cell renderer. Keep presentation here; no data fetching. */
  cell: (row: T) => ReactNode
  align?: ColumnAlign
  /**
   * Legacy Tailwind width utility on the header only (e.g. 'w-40').
   * Prefer `defaultWidth` (px) for resizable tables.
   */
  width?: string
  /** Default column width in pixels. Enables the shared resize system when set. */
  defaultWidth?: number
  minWidth?: number
  maxWidth?: number
  /** Defaults to true when `defaultWidth` is set. Set false for fixed columns. */
  resizable?: boolean
  /**
   * Truncate overflowing cell content with ellipsis.
   * Defaults to true when the table uses pixel column widths.
   */
  truncate?: boolean
  headerClassName?: string
  cellClassName?: string
}

export interface DataTableProps<T> {
  columns: DataTableColumn<T>[]
  data: T[] | undefined
  /** Unique, stable row id used as React key. */
  getRowId: (row: T) => string
  /**
   * Stable table id used for localStorage width persistence.
   * Example: `stations`, `stamp-designs`.
   * Required for persistence; resize still works without it when columns have defaultWidth.
   */
  tableId?: string
  /** Fixed actions column width in px when `rowActions` is provided. */
  actionsWidth?: number
  isLoading?: boolean
  error?: unknown
  /** Highlight a row that needs attention (e.g. not-ready station). */
  rowWarning?: (row: T) => boolean
  /** Right-aligned per-row actions slot. Adds a fixed actions column when provided. */
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

function headerLabel(header: ReactNode, fallbackId: string): string {
  if (typeof header === 'string' || typeof header === 'number') {
    return String(header)
  }
  return fallbackId
}

export function DataTable<T>({
  columns,
  data,
  getRowId,
  tableId,
  actionsWidth = DEFAULT_ACTIONS_WIDTH,
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
  const hasActions = Boolean(rowActions)
  const usesPixelWidths = columns.some((column) => typeof column.defaultWidth === 'number')

  const resizeConfigs: ResizableColumnConfig[] = useMemo(
    () =>
      columns.map((column) => ({
        id: column.id,
        defaultWidth: column.defaultWidth ?? 140,
        minWidth: column.minWidth,
        maxWidth: column.maxWidth,
        resizable: column.resizable,
      })),
    [columns],
  )

  const {
    getWidth,
    totalWidth,
    resizingId,
    isResizing,
    isColumnResizable,
    startResize,
    nudgeWidth,
  } = useResizableColumns({
    tableId,
    columns: usesPixelWidths ? resizeConfigs : [],
    hasActions: usesPixelWidths && hasActions,
    actionsWidth,
  })

  const totalColumns = columns.length + (hasActions ? 1 : 0)

  const headerCells = (
    <tr className="border-b border-border">
      {columns.map((column) => {
        const resizable = usesPixelWidths && isColumnResizable(column.id)
        return (
          <th
            key={column.id}
            scope="col"
            style={usesPixelWidths ? { width: getWidth(column.id) } : undefined}
            className={cn(
              'relative px-4 py-2.5 text-[11px] font-semibold uppercase tracking-wide text-muted-foreground align-middle',
              usesPixelWidths && 'overflow-hidden',
              alignClass[column.align ?? 'left'],
              !usesPixelWidths && column.width,
              column.headerClassName,
            )}
          >
            <span className="block truncate pr-2">{column.header}</span>
            {resizable ? (
              <ColumnResizeHandle
                columnId={column.id}
                label={headerLabel(column.header, column.id)}
                isActive={resizingId === column.id}
                onResizeStart={startResize}
                onNudge={nudgeWidth}
              />
            ) : null}
          </th>
        )
      })}
      {hasActions ? (
        <th
          scope="col"
          style={usesPixelWidths ? { width: getWidth(ACTIONS_COLUMN_ID) } : undefined}
          className={cn(
            'relative px-3 py-2.5 text-right text-[11px] font-semibold uppercase tracking-wide text-muted-foreground align-middle',
            usesPixelWidths && stickyActionsHeaderClass,
          )}
        >
          <span className="sr-only">Actions</span>
        </th>
      ) : null}
    </tr>
  )

  function cellTruncate(column: DataTableColumn<T>): boolean {
    if (column.truncate !== undefined) return column.truncate
    return usesPixelWidths
  }

  function renderBody() {
    if (isLoading) {
      return Array.from({ length: skeletonRows }).map((_, rowIndex) => (
        <tr key={`skeleton-${rowIndex}`} className="border-b border-border">
          {columns.map((column) => (
            <td
              key={column.id}
              style={usesPixelWidths ? { width: getWidth(column.id) } : undefined}
              className={cn(
                'px-4 py-3 align-middle',
                usesPixelWidths && 'overflow-hidden',
                alignClass[column.align ?? 'left'],
              )}
            >
              <LoadingSkeleton className="h-4 w-full max-w-[160px]" />
            </td>
          ))}
          {hasActions ? (
            <td
              style={usesPixelWidths ? { width: getWidth(ACTIONS_COLUMN_ID) } : undefined}
              className={cn(
                'px-3 py-3 text-right align-middle bg-card',
                usesPixelWidths && stickyActionsCellClass,
              )}
            >
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
            'group border-b border-border last:border-0',
            warned ? 'bg-accent/40' : 'bg-card',
            clickable ? 'cursor-pointer hover:bg-secondary' : 'hover:bg-secondary/60',
          )}
        >
          {columns.map((column) => (
            <td
              key={column.id}
              style={usesPixelWidths ? { width: getWidth(column.id) } : undefined}
              className={cn(
                'px-4 py-3 text-sm text-foreground align-middle',
                usesPixelWidths && 'overflow-hidden',
                alignClass[column.align ?? 'left'],
                column.cellClassName,
              )}
            >
              <div className={cn('min-w-0', cellTruncate(column) && 'truncate')}>
                {column.cell(row)}
              </div>
            </td>
          ))}
          {hasActions ? (
            <td
              style={usesPixelWidths ? { width: getWidth(ACTIONS_COLUMN_ID) } : undefined}
              className={cn(
                'px-3 py-3 text-right align-middle',
                usesPixelWidths && stickyActionsCellClass,
                usesPixelWidths && (warned ? 'bg-accent/40' : 'bg-card'),
                usesPixelWidths &&
                  (clickable ? 'group-hover:bg-secondary' : 'group-hover:bg-secondary/60'),
              )}
              onClick={(event) => event.stopPropagation()}
            >
              <div className="flex items-center justify-end gap-0.5 whitespace-nowrap">
                {rowActions?.(row)}
              </div>
            </td>
          ) : null}
        </tr>
      )
    })
  }

  return (
    <div
      className={cn(
        'table-scroll max-w-full overflow-x-auto rounded-lg border border-border bg-card',
        isResizing && 'select-none',
        className,
      )}
    >
      <table
        className={cn('border-collapse text-left', usesPixelWidths ? 'table-fixed' : 'w-full')}
        style={usesPixelWidths ? { width: totalWidth } : undefined}
      >
        {caption ? <caption className="sr-only">{caption}</caption> : null}
        {usesPixelWidths ? (
          <colgroup>
            {columns.map((column) => (
              <col key={column.id} style={{ width: getWidth(column.id) }} />
            ))}
            {hasActions ? <col style={{ width: getWidth(ACTIONS_COLUMN_ID) }} /> : null}
          </colgroup>
        ) : null}
        <thead className="bg-secondary/50">{headerCells}</thead>
        <tbody>{renderBody()}</tbody>
      </table>
    </div>
  )
}
