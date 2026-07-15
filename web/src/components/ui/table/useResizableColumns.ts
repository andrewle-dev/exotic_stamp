import { useCallback, useMemo, useRef, useState } from 'react'
import {
  loadTableColumnWidths,
  resetTableColumnWidths,
  saveTableColumnWidths,
} from './columnWidthStorage'

export const ACTIONS_COLUMN_ID = '__actions'

export interface ResizableColumnConfig {
  id: string
  defaultWidth: number
  minWidth?: number
  maxWidth?: number
  /** Defaults to true. */
  resizable?: boolean
}

export interface UseResizableColumnsOptions {
  tableId?: string
  columns: ResizableColumnConfig[]
  hasActions?: boolean
  actionsWidth?: number
  actionsMinWidth?: number
}

function clamp(value: number, min: number, max?: number): number {
  const upper = max ?? Number.POSITIVE_INFINITY
  return Math.min(upper, Math.max(min, Math.round(value)))
}

function buildDefaults(
  columns: ResizableColumnConfig[],
  hasActions: boolean,
  actionsWidth: number,
): Record<string, number> {
  const widths: Record<string, number> = {}
  for (const column of columns) {
    widths[column.id] = clamp(column.defaultWidth, column.minWidth ?? 48, column.maxWidth)
  }
  if (hasActions) {
    widths[ACTIONS_COLUMN_ID] = actionsWidth
  }
  return widths
}

function mergeStoredWidths(
  defaults: Record<string, number>,
  stored: Record<string, number> | null,
  columns: ResizableColumnConfig[],
  hasActions: boolean,
  actionsWidth: number,
  actionsMinWidth: number,
): Record<string, number> {
  if (!stored) return defaults
  const next = { ...defaults }
  for (const column of columns) {
    const saved = stored[column.id]
    if (typeof saved === 'number') {
      next[column.id] = clamp(saved, column.minWidth ?? 48, column.maxWidth)
    }
  }
  if (hasActions) {
    // Actions column is not user-resizable — always honor the page-level width prop
    // so bumping actionsWidth (e.g. for text labels) is not stuck on a stale localStorage value.
    next[ACTIONS_COLUMN_ID] = Math.max(actionsWidth, actionsMinWidth)
  }
  return next
}

function resolveInitialWidths(
  tableId: string | undefined,
  columns: ResizableColumnConfig[],
  hasActions: boolean,
  actionsWidth: number,
  actionsMinWidth: number,
): Record<string, number> {
  const defaults = buildDefaults(columns, hasActions, actionsWidth)
  const stored = tableId ? loadTableColumnWidths(tableId) : null
  return mergeStoredWidths(
    defaults,
    stored,
    columns,
    hasActions,
    actionsWidth,
    actionsMinWidth,
  )
}

export function useResizableColumns({
  tableId,
  columns,
  hasActions = false,
  actionsWidth = 128,
  actionsMinWidth = 72,
}: UseResizableColumnsOptions) {
  const columnKey = columns.map((c) => c.id).join('|')
  const syncKey = `${tableId ?? ''}::${columnKey}::${hasActions ? 1 : 0}::${actionsWidth}`

  const defaults = useMemo(
    () => buildDefaults(columns, hasActions, actionsWidth),
    // columnKey encodes column id identity for stable defaults rebuild
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [columnKey, hasActions, actionsWidth],
  )

  const [widths, setWidths] = useState(() =>
    resolveInitialWidths(tableId, columns, hasActions, actionsWidth, actionsMinWidth),
  )
  const [appliedSyncKey, setAppliedSyncKey] = useState(syncKey)

  // Reset when the table identity / column set changes (React-recommended render-time adjust).
  if (syncKey !== appliedSyncKey) {
    setAppliedSyncKey(syncKey)
    setWidths(resolveInitialWidths(tableId, columns, hasActions, actionsWidth, actionsMinWidth))
  }

  const [resizingId, setResizingId] = useState<string | null>(null)
  const dragWidthsRef = useRef(widths)

  const persist = useCallback(
    (next: Record<string, number>) => {
      if (!tableId) return
      saveTableColumnWidths(tableId, next)
    },
    [tableId],
  )

  const getWidth = useCallback(
    (id: string) => widths[id] ?? defaults[id] ?? 120,
    [widths, defaults],
  )

  const isColumnResizable = useCallback(
    (id: string) => {
      if (id === ACTIONS_COLUMN_ID) return false
      const column = columns.find((c) => c.id === id)
      return column?.resizable !== false
    },
    [columns],
  )

  const getConstraints = useCallback(
    (id: string) => {
      if (id === ACTIONS_COLUMN_ID) {
        return { min: actionsMinWidth, max: undefined as number | undefined }
      }
      const column = columns.find((c) => c.id === id)
      return {
        min: column?.minWidth ?? 48,
        max: column?.maxWidth,
      }
    },
    [actionsMinWidth, columns],
  )

  const setColumnWidth = useCallback(
    (id: string, width: number) => {
      const { min, max } = getConstraints(id)
      const clamped = clamp(width, min, max)
      setWidths((prev) => {
        if (prev[id] === clamped) return prev
        const next = { ...prev, [id]: clamped }
        persist(next)
        return next
      })
    },
    [getConstraints, persist],
  )

  const startResize = useCallback(
    (columnId: string, clientX: number) => {
      if (!isColumnResizable(columnId)) return

      const startWidth = dragWidthsRef.current[columnId] ?? defaults[columnId] ?? 120
      dragWidthsRef.current = { ...widths, [columnId]: startWidth }
      setResizingId(columnId)

      const previousUserSelect = document.body.style.userSelect
      const previousCursor = document.body.style.cursor
      document.body.style.userSelect = 'none'
      document.body.style.cursor = 'col-resize'

      const onPointerMove = (event: PointerEvent) => {
        const delta = event.clientX - clientX
        const { min, max } = getConstraints(columnId)
        const nextWidth = clamp(startWidth + delta, min, max)
        const next = { ...dragWidthsRef.current, [columnId]: nextWidth }
        dragWidthsRef.current = next
        setWidths(next)
      }

      const finish = () => {
        document.removeEventListener('pointermove', onPointerMove)
        document.removeEventListener('pointerup', finish)
        document.removeEventListener('pointercancel', finish)
        document.body.style.userSelect = previousUserSelect
        document.body.style.cursor = previousCursor
        setResizingId(null)
        persist(dragWidthsRef.current)
      }

      document.addEventListener('pointermove', onPointerMove)
      document.addEventListener('pointerup', finish)
      document.addEventListener('pointercancel', finish)
    },
    [defaults, getConstraints, isColumnResizable, persist, widths],
  )

  const nudgeWidth = useCallback(
    (columnId: string, delta: number) => {
      if (!isColumnResizable(columnId)) return
      const current = widths[columnId] ?? defaults[columnId] ?? 120
      setColumnWidth(columnId, current + delta)
    },
    [defaults, isColumnResizable, setColumnWidth, widths],
  )

  const resetWidths = useCallback(() => {
    if (tableId) {
      resetTableColumnWidths(tableId)
    }
    setWidths(defaults)
    dragWidthsRef.current = defaults
  }, [defaults, tableId])

  const totalWidth = useMemo(() => {
    let sum = 0
    for (const column of columns) {
      sum += getWidth(column.id)
    }
    if (hasActions) {
      sum += getWidth(ACTIONS_COLUMN_ID)
    }
    return sum
  }, [columns, getWidth, hasActions])

  return {
    widths,
    getWidth,
    totalWidth,
    resizingId,
    isResizing: resizingId !== null,
    isColumnResizable,
    startResize,
    nudgeWidth,
    setColumnWidth,
    resetWidths,
    actionsColumnId: ACTIONS_COLUMN_ID,
  }
}
